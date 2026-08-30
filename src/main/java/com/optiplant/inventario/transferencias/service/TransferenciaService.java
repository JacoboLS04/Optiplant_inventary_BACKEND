package com.optiplant.inventario.transferencias.service;

import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioRequest;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.service.MovimientoInventarioService;
import com.optiplant.inventario.transferencias.dto.TransferenciaAprobacionRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaDespachoRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaRecepcionRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaRequest;
import com.optiplant.inventario.transferencias.dto.TransferenciaResponse;
import com.optiplant.inventario.transferencias.entity.DecisionAprobacion;
import com.optiplant.inventario.transferencias.entity.EstadoReserva;
import com.optiplant.inventario.transferencias.entity.EstadoTransferencia;
import com.optiplant.inventario.transferencias.entity.ReservaStock;
import com.optiplant.inventario.transferencias.entity.RolAprobacion;
import com.optiplant.inventario.transferencias.entity.Transferencia;
import com.optiplant.inventario.transferencias.entity.TransferenciaAprobacion;
import com.optiplant.inventario.transferencias.entity.TransferenciaFaltante;
import com.optiplant.inventario.transferencias.entity.TransferenciaLinea;
import com.optiplant.inventario.transferencias.entity.TratamientoFaltante;
import com.optiplant.inventario.transferencias.entity.UrgenciaTransferencia;
import com.optiplant.inventario.transferencias.mapper.TransferenciaMapper;
import com.optiplant.inventario.transferencias.repository.ReservaStockRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaAprobacionRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaFaltanteRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaLineaRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private static final String PREFIJO_CODIGO = "TR-";

    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaLineaRepository lineaRepository;
    private final TransferenciaAprobacionRepository aprobacionRepository;
    private final ReservaStockRepository reservaRepository;
    private final TransferenciaFaltanteRepository faltanteRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExistenciaRepository existenciaRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final TransferenciaMapper mapper;

    @Transactional
    public TransferenciaResponse crear(TransferenciaRequest request) {
        if (request.getSucursalOrigenId().equals(request.getSucursalDestinoId())) {
            throw new BusinessRuleException(
                    "La sucursal de origen no puede ser igual a la sucursal de destino.");
        }

        Sucursal origen = sucursalRepository.findById(request.getSucursalOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal origen no encontrada: " + request.getSucursalOrigenId()));
        Sucursal destino = sucursalRepository.findById(request.getSucursalDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal destino no encontrada: " + request.getSucursalDestinoId()));

        Set<Long> productosVistos = new HashSet<>();
        List<TransferenciaLinea> lineas = new ArrayList<>();
        for (TransferenciaRequest.LineaRequest lineaReq : request.getLineas()) {
            if (!productosVistos.add(lineaReq.getProductoId())) {
                throw new BusinessRuleException(
                        "El producto " + lineaReq.getProductoId()
                                + " aparece más de una vez en la transferencia.");
            }
            Producto producto = productoRepository.findById(lineaReq.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: " + lineaReq.getProductoId()));
            lineas.add(TransferenciaLinea.builder()
                    .producto(producto)
                    .cantidadSolicitada(lineaReq.getCantidadSolicitada())
                    .build());
        }

        Transferencia transferencia = Transferencia.builder()
                .codigo(generarCodigo())
                .sucursalOrigen(origen)
                .sucursalDestino(destino)
                .usuarioSolicitante(resolveUsuario())
                .urgencia(request.getUrgencia() != null
                        ? request.getUrgencia() : UrgenciaTransferencia.NORMAL)
                .transportista(request.getTransportista())
                .guia(request.getGuia())
                .fechaEstimadaLlegada(request.getFechaEstimadaLlegada())
                .estado(EstadoTransferencia.SOLICITADA)
                .fechaSolicitud(LocalDateTime.now())
                .build();

        lineas.forEach(linea -> linea.setTransferencia(transferencia));
        transferencia.setLineas(lineas);

        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<TransferenciaResponse> buscar(
            Long sucursalOrigenId, Long sucursalDestinoId,
            EstadoTransferencia estado, String busqueda, int page, int size) {

        Specification<Transferencia> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.isBlank()) {
                String patron = "%" + busqueda.trim().toLowerCase() + "%";
                var joinLineas = root.join("lineas");
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), patron),
                        cb.like(cb.lower(root.get("sucursalOrigen").get("nombre")), patron),
                        cb.like(cb.lower(root.get("sucursalDestino").get("nombre")), patron),
                        cb.like(cb.lower(joinLineas.get("producto").get("nombre")), patron),
                        cb.like(cb.lower(joinLineas.get("producto").get("sku")), patron)
                ));
                query.distinct(true);
            }

            if (sucursalOrigenId != null) {
                predicates.add(cb.equal(root.get("sucursalOrigen").get("id"), sucursalOrigenId));
            }
            if (sucursalDestinoId != null) {
                predicates.add(cb.equal(root.get("sucursalDestino").get("id"), sucursalDestinoId));
            }
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transferencia> transferencias = transferenciaRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaSolicitud")));

        return new PaginatedResponse<>(
                transferencias.getContent().stream().map(this::toResponse).toList(),
                page, size, transferencias.getTotalElements(), transferencias.getTotalPages());
    }

    @Transactional(readOnly = true)
    public TransferenciaResponse obtener(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public TransferenciaResponse aprobar(Long id, TransferenciaAprobacionRequest request) {
        // RF-064: solo el Gerente o el Administrador pueden aprobar/rechazar.
        Usuario actual = resolveUsuario();
        String rolActual = actual != null ? actual.getRol() : "";
        if (!rolActual.equals("GERENTE") && !rolActual.equals("ADMINISTRADOR")) {
            throw new AccessDeniedException(
                    "Solo el Gerente o el Administrador pueden aprobar/rechazar "
                            + "una transferencia (RF-064)");
        }

        Transferencia transferencia = findOrThrow(id);

        if (request.getDecision() != DecisionAprobacion.APROBADO
                && request.getDecision() != DecisionAprobacion.RECHAZADO) {
            throw new BusinessRuleException("Decisión inválida: " + request.getDecision());
        }

        if (transferencia.getEstado() != EstadoTransferencia.SOLICITADA) {
            throw new BusinessRuleException(
                    "Solo se puede aprobar/rechazar una transferencia en estado SOLICITADA. "
                            + "Estado actual: " + transferencia.getEstado());
        }

        if (aprobacionRepository.existsByTransferenciaIdAndRolAprobacion(
                id, request.getRolAprobacion())) {
            throw new BusinessRuleException(
                    "El rol " + request.getRolAprobacion()
                            + " ya registró su decisión para esta transferencia.");
        }

        Usuario gerente = usuarioRepository.findById(request.getGerenteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Gerente no encontrado: " + request.getGerenteId()));

        TransferenciaAprobacion aprobacion = TransferenciaAprobacion.builder()
                .transferencia(transferencia)
                .gerente(gerente)
                .rolAprobacion(request.getRolAprobacion())
                .decision(request.getDecision())
                .observacion(request.getObservacion())
                .fecha(LocalDateTime.now())
                .build();
        aprobacionRepository.save(aprobacion);

        if (request.getDecision() == DecisionAprobacion.RECHAZADO) {
            transferencia.setEstado(EstadoTransferencia.RECHAZADA);
            transferenciaRepository.save(transferencia);
            return toResponse(transferencia);
        }

        List<TransferenciaAprobacion> aprobaciones = aprobacionRepository
                .findByTransferenciaId(id);
        boolean tieneOrigen = aprobaciones.stream()
                .anyMatch(a -> a.getRolAprobacion() == RolAprobacion.ORIGEN
                        && a.getDecision() == DecisionAprobacion.APROBADO);
        boolean tieneDestino = aprobaciones.stream()
                .anyMatch(a -> a.getRolAprobacion() == RolAprobacion.DESTINO
                        && a.getDecision() == DecisionAprobacion.APROBADO);

        if (tieneOrigen && tieneDestino) {
            validarDisponibilidadOrigen(transferencia);
            crearReservas(transferencia);
            transferencia.setEstado(EstadoTransferencia.APROBADA);
        }

        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse preparar(Long id) {
        Transferencia transferencia = findOrThrow(id);
        if (transferencia.getEstado() != EstadoTransferencia.APROBADA) {
            throw new BusinessRuleException(
                    "Solo se puede preparar una transferencia APROBADA. Estado actual: "
                            + transferencia.getEstado());
        }
        transferencia.setEstado(EstadoTransferencia.EN_PREPARACION);
        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse despachar(Long id, TransferenciaDespachoRequest request) {
        Transferencia transferencia = findOrThrow(id);

        if (transferencia.getEstado() != EstadoTransferencia.EN_PREPARACION) {
            throw new BusinessRuleException(
                    "Solo se puede despachar una transferencia EN_PREPARACION. Estado actual: "
                            + transferencia.getEstado());
        }

        List<TransferenciaLinea> lineas = lineaRepository.findByTransferenciaId(id);
        for (TransferenciaDespachoRequest.LineaDespacho item : request.getLineas()) {
            TransferenciaLinea linea = lineas.stream()
                    .filter(l -> l.getId().equals(item.getTransferenciaLineaId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "La línea " + item.getTransferenciaLineaId()
                                    + " no pertenece a la transferencia " + id));

            ReservaStock reserva = reservaRepository.findByLineaId(linea.getId())
                    .orElseThrow(() -> new BusinessRuleException(
                            "No existe reserva de stock para la línea " + linea.getId()));
            if (reserva.getEstado() != EstadoReserva.ACTIVA) {
                throw new BusinessRuleException(
                        "La reserva no está ACTIVA; no se puede despachar. Estado: "
                                + reserva.getEstado());
            }

            BigDecimal cantidad = item.getCantidadDespachada();
            Existencia existencia = getOrCreateExistencia(linea.getProducto(),
                    transferencia.getSucursalOrigen());
            BigDecimal disponibleReal = existencia.getCantidadFisica()
                    .subtract(existencia.getCantidadReservada());
            if (cantidad.compareTo(disponibleReal) > 0) {
                throw new BusinessRuleException(
                        "Cantidad a despachar excede la disponible real de origen. Disponible: "
                                + disponibleReal + ", solicitado: " + cantidad);
            }

            BigDecimal desplazar = cantidad.min(reserva.getCantidad());

            movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                    linea.getProducto().getId(),
                    transferencia.getSucursalOrigen().getId(),
                    "retiro",
                    "transferencia - " + transferencia.getCodigo(),
                    desplazar,
                    resolverIdUsuario()));

            reserva.setEstado(EstadoReserva.CONSUMIDA);
            reservaRepository.save(reserva);

            Existencia existenciaActualizada = getOrCreateExistencia(linea.getProducto(),
                    transferencia.getSucursalOrigen());
            existenciaActualizada.setCantidadReservada(
                    existenciaActualizada.getCantidadReservada().subtract(desplazar));
            existenciaRepository.save(existenciaActualizada);

            linea.setCantidadDespachada(cantidad);
            lineaRepository.save(linea);
        }

        transferencia.setTransportista(request.getTransportista() != null
                ? request.getTransportista() : transferencia.getTransportista());
        transferencia.setGuia(request.getGuia() != null
                ? request.getGuia() : transferencia.getGuia());
        transferencia.setFechaDespacho(LocalDateTime.now());
        transferencia.setEstado(EstadoTransferencia.EN_TRANSITO);

        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse recibir(Long id, TransferenciaRecepcionRequest request) {
        Transferencia transferencia = findOrThrow(id);

        if (transferencia.getEstado() != EstadoTransferencia.EN_TRANSITO) {
            throw new BusinessRuleException(
                    "Solo se puede recibir una transferencia EN_TRANSITO. Estado actual: "
                            + transferencia.getEstado());
        }

        List<TransferenciaLinea> lineas = lineaRepository.findByTransferenciaId(id);
        boolean algunFaltante = false;

        for (TransferenciaRecepcionRequest.LineaRecepcion item : request.getLineas()) {
            TransferenciaLinea linea = lineas.stream()
                    .filter(l -> l.getId().equals(item.getTransferenciaLineaId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "La línea " + item.getTransferenciaLineaId()
                                    + " no pertenece a la transferencia " + id));

            BigDecimal cantidadRecibida = item.getCantidadRecibida();
            BigDecimal despachada = linea.getCantidadDespachada() != null
                    ? linea.getCantidadDespachada() : linea.getCantidadSolicitada();
            if (cantidadRecibida.compareTo(despachada) > 0) {
                throw new BusinessRuleException(
                        "No puede recibir más de lo despachado. Despachado: " + despachada);
            }

            Existencia existenciaDestino = getOrCreateExistencia(linea.getProducto(),
                    transferencia.getSucursalDestino());
            movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                    linea.getProducto().getId(),
                    transferencia.getSucursalDestino().getId(),
                    "ingreso",
                    "transferencia - " + transferencia.getCodigo(),
                    cantidadRecibida,
                    resolverIdUsuario()));
            existenciaRepository.save(existenciaDestino);

            linea.setCantidadRecibida(cantidadRecibida);
            lineaRepository.save(linea);

            boolean completo = cantidadRecibida.compareTo(despachada) >= 0;
            if (!completo) {
                algunFaltante = true;
                TratamientoFaltante tratamiento = item.getTratamiento() != null
                        ? item.getTratamiento() : TratamientoFaltante.RECLAMACION;
                BigDecimal faltante = despachada.subtract(cantidadRecibida);
                TransferenciaFaltante faltanteEntity = TransferenciaFaltante.builder()
                        .linea(linea)
                        .cantidadFaltante(faltante)
                        .tratamiento(tratamiento)
                        .usuario(resolveUsuario())
                        .fecha(LocalDateTime.now())
                        .build();
                faltanteRepository.save(faltanteEntity);
            }
        }

        transferencia.setFechaRecepcion(LocalDateTime.now());
        transferencia.setEstado(algunFaltante
                ? EstadoTransferencia.CON_FALTANTES : EstadoTransferencia.RECIBIDA);

        return toResponse(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public TransferenciaResponse cancelar(Long id) {
        Transferencia transferencia = findOrThrow(id);

        if (transferencia.getEstado() != EstadoTransferencia.SOLICITADA
                && transferencia.getEstado() != EstadoTransferencia.APROBADA) {
            throw new BusinessRuleException(
                    "Solo se puede cancelar una transferencia SOLICITADA o APROBADA. "
                            + "Estado actual: " + transferencia.getEstado()
                            + ". No es cancelable desde EN_TRANSITO/RECIBIDA/CON_FALTANTES.");
        }

        if (transferencia.getEstado() == EstadoTransferencia.APROBADA) {
            for (ReservaStock reserva : reservaRepository
                    .findByLineaTransferenciaId(id)) {
                if (reserva.getEstado() == EstadoReserva.ACTIVA) {
                    reserva.setEstado(EstadoReserva.LIBERADA);
                    reserva.setFechaLiberacion(LocalDateTime.now());
                    reservaRepository.save(reserva);

                    Existencia existencia = getOrCreateExistencia(
                            reserva.getProducto(), transferencia.getSucursalOrigen());
                    existencia.setCantidadReservada(
                            existencia.getCantidadReservada().subtract(reserva.getCantidad()));
                    existenciaRepository.save(existencia);
                }
            }
        }

        transferencia.setEstado(EstadoTransferencia.CANCELADA);
        return toResponse(transferenciaRepository.save(transferencia));
    }

    private void validarDisponibilidadOrigen(Transferencia transferencia) {
        for (TransferenciaLinea linea : transferencia.getLineas()) {
            Existencia existencia = getOrCreateExistencia(linea.getProducto(),
                    transferencia.getSucursalOrigen());
            BigDecimal disponible = existencia.getCantidadFisica()
                    .subtract(existencia.getCantidadReservada());
            if (disponible.compareTo(linea.getCantidadSolicitada()) < 0) {
                throw new BusinessRuleException(
                        "Cantidad solicitada excede la cantidad disponible de origen para "
                                + linea.getProducto().getNombre() + ". Disponible: "
                                + disponible + ", solicitada: " + linea.getCantidadSolicitada());
            }
        }
    }

    private void crearReservas(Transferencia transferencia) {
        for (TransferenciaLinea linea : transferencia.getLineas()) {
            ReservaStock reserva = ReservaStock.builder()
                    .linea(linea)
                    .producto(linea.getProducto())
                    .sucursal(transferencia.getSucursalOrigen())
                    .cantidad(linea.getCantidadSolicitada())
                    .estado(EstadoReserva.ACTIVA)
                    .fechaCreacion(LocalDateTime.now())
                    .build();
            reservaRepository.save(reserva);

            Existencia existencia = getOrCreateExistencia(linea.getProducto(),
                    transferencia.getSucursalOrigen());
            existencia.setCantidadReservada(
                    existencia.getCantidadReservada().add(linea.getCantidadSolicitada()));
            existenciaRepository.save(existencia);
        }
    }

    private Existencia getOrCreateExistencia(Producto producto, Sucursal sucursal) {
        Optional<Existencia> opt = existenciaRepository
                .findByProductoIdAndSucursalId(producto.getId(), sucursal.getId());
        if (opt.isPresent()) {
            return opt.get();
        }
        Existencia exist = Existencia.builder()
                .producto(producto)
                .sucursal(sucursal)
                .cantidadFisica(BigDecimal.ZERO)
                .cantidadReservada(BigDecimal.ZERO)
                .stockMinimo(BigDecimal.ZERO)
                .build();
        return existenciaRepository.save(exist);
    }

    private TransferenciaResponse toResponse(Transferencia transferencia) {
        TransferenciaResponse response = mapper.toResponse(transferencia);

        List<TransferenciaResponse.LineaResponse> lineasRespuesta = new ArrayList<>();
        BigDecimal totalUnidades = BigDecimal.ZERO;
        for (TransferenciaLinea linea : transferencia.getLineas()) {
            TransferenciaResponse.LineaResponse lineaResponse = mapper.toLineaResponse(linea);

            BigDecimal despachada = linea.getCantidadDespachada() != null
                    ? linea.getCantidadDespachada() : linea.getCantidadSolicitada();
            BigDecimal medida = linea.getCantidadRecibida() != null
                    ? linea.getCantidadRecibida() : despachada;
            totalUnidades = totalUnidades.add(medida);

            Existencia existencia = existenciaRepository
                    .findByProductoIdAndSucursalId(
                            linea.getProducto().getId(),
                            transferencia.getSucursalOrigen().getId()).orElse(null);
            if (existencia != null) {
                lineaResponse.setCantidadDisponibleOrigen(existencia.getCantidadFisica()
                        .subtract(existencia.getCantidadReservada()));
            }

            reservaRepository.findByLineaId(linea.getId()).ifPresent(r ->
                    lineaResponse.setReserva(TransferenciaResponse.ReservaResponse.builder()
                            .id(r.getId())
                            .productoId(r.getProducto().getId())
                            .sucursalId(r.getSucursal().getId())
                            .cantidad(r.getCantidad())
                            .estado(r.getEstado())
                            .fechaCreacion(r.getFechaCreacion())
                            .fechaLiberacion(r.getFechaLiberacion())
                            .build()));

            lineaResponse.setFaltantes(faltanteRepository
                    .findByLineaTransferenciaId(transferencia.getId())
                    .stream()
                    .filter(f -> f.getLinea().getId().equals(linea.getId()))
                    .map(mapper::toFaltanteResponse)
                    .toList());

            lineasRespuesta.add(lineaResponse);
        }

        response.setLineas(lineasRespuesta);
        response.setTotalUnidades(totalUnidades);
        response.setAprobaciones(aprobacionRepository.findByTransferenciaId(transferencia.getId())
                .stream().map(a -> TransferenciaResponse.AprobacionResponse.builder()
                        .id(a.getId())
                        .gerenteId(a.getGerente().getId())
                        .nombreGerente(a.getGerente().getNombre())
                        .rolAprobacion(a.getRolAprobacion().name())
                        .decision(a.getDecision().name())
                        .fecha(a.getFecha())
                        .observacion(a.getObservacion())
                        .build()).toList());

        return response;
    }

    private Transferencia findOrThrow(Long id) {
        return transferenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transferencia no encontrada: " + id));
    }

    private String generarCodigo() {
        long secuencia = transferenciaRepository.countByCodigoStartingWith(PREFIJO_CODIGO) + 1;
        String codigo;
        while (true) {
            codigo = PREFIJO_CODIGO + String.format("%03d", secuencia);
            if (transferenciaRepository.findByCodigo(codigo).isEmpty()) {
                return codigo;
            }
            secuencia++;
        }
    }

    private Usuario resolveUsuario() {
        Long id = resolverIdUsuario();
        if (id != null) {
            return usuarioRepository.findById(id).orElse(null);
        }
        return null;
    }

    private Long resolverIdUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            return usuarioRepository.findByEmail(email)
                    .map(Usuario::getId)
                    .orElse(null);
        }
        return null;
    }
}
