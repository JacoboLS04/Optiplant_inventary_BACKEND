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
import com.optiplant.inventario.transferencias.entity.TratamientoFaltante;
import com.optiplant.inventario.transferencias.mapper.TransferenciaMapper;
import com.optiplant.inventario.transferencias.repository.ReservaStockRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaAprobacionRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaFaltanteRepository;
import com.optiplant.inventario.transferencias.repository.TransferenciaRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private static final String PREFIJO_CODIGO = "TR-";

    private final TransferenciaRepository transferenciaRepository;
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

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado: " + request.getProductoId()));
        Sucursal origen = sucursalRepository.findById(request.getSucursalOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal origen no encontrada: " + request.getSucursalOrigenId()));
        Sucursal destino = sucursalRepository.findById(request.getSucursalDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal destino no encontrada: " + request.getSucursalDestinoId()));

        Transferencia transferencia = Transferencia.builder()
                .codigo(generarCodigo())
                .producto(producto)
                .sucursalOrigen(origen)
                .sucursalDestino(destino)
                .usuarioSolicitante(resolveUsuario())
                .cantidadSolicitada(request.getCantidadSolicitada())
                .urgencia(request.getUrgencia() != null
                        ? request.getUrgencia()
                        : com.optiplant.inventario.transferencias.entity.UrgenciaTransferencia.NORMAL)
                .transportista(request.getTransportista())
                .guia(request.getGuia())
                .fechaEstimadaLlegada(request.getFechaEstimadaLlegada())
                .estado(EstadoTransferencia.SOLICITADA)
                .fechaSolicitud(LocalDateTime.now())
                .build();

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
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), patron),
                        cb.like(cb.lower(root.get("producto").get("nombre")), patron),
                        cb.like(cb.lower(root.get("producto").get("sku")), patron)
                ));
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
            crearReserva(transferencia);
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

        BigDecimal cantidad = request.getCantidadDespachada();
        ReservaStock reserva = reservaRepository.findByTransferenciaId(id)
                .orElseThrow(() -> new BusinessRuleException(
                        "No existe reserva de stock para esta transferencia."));
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            throw new BusinessRuleException(
                    "La reserva no está ACTIVA; no se puede despachar. Estado: " + reserva.getEstado());
        }

        Existencia existencia = getExistenciaOrigen(transferencia);
        BigDecimal disponibleReal = existencia.getCantidadFisica()
                .subtract(existencia.getCantidadReservada());
        if (cantidad.compareTo(disponibleReal) > 0) {
            throw new BusinessRuleException(
                    "Cantidad a despachar excede la disponible real de origen. Disponible: "
                            + disponibleReal + ", solicitado: " + cantidad);
        }

        BigDecimal desplazar = cantidad.min(reserva.getCantidad());

        movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                transferencia.getProducto().getId(),
                transferencia.getSucursalOrigen().getId(),
                "retiro",
                "transferencia - " + transferencia.getCodigo(),
                desplazar,
                resolverIdUsuario()));

        reserva.setEstado(EstadoReserva.CONSUMIDA);
        reservaRepository.save(reserva);

        Existencia existenciaActualizada = getExistenciaOrigen(transferencia);
        existenciaActualizada.setCantidadReservada(
                existenciaActualizada.getCantidadReservada().subtract(desplazar));
        existenciaRepository.save(existenciaActualizada);

        transferencia.setCantidadDespachada(cantidad);
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

        BigDecimal cantidadRecibida = request.getCantidadRecibida();
        BigDecimal despachada = transferencia.getCantidadDespachada() != null
                ? transferencia.getCantidadDespachada()
                : transferencia.getCantidadSolicitada();
        if (cantidadRecibida.compareTo(despachada) > 0) {
            throw new BusinessRuleException(
                    "No puede recibir más de lo despachado. Despachado: " + despachada);
        }

        Existencia existenciaDestino = getExistenciaDestino(transferencia);
        movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                transferencia.getProducto().getId(),
                transferencia.getSucursalDestino().getId(),
                "ingreso",
                "transferencia - " + transferencia.getCodigo(),
                cantidadRecibida,
                resolverIdUsuario()));
        existenciaRepository.save(existenciaDestino);

        transferencia.setCantidadRecibida(cantidadRecibida);
        transferencia.setFechaRecepcion(LocalDateTime.now());

        boolean completo = cantidadRecibida.compareTo(despachada) >= 0;
        if (completo) {
            transferencia.setEstado(EstadoTransferencia.RECIBIDA);
        } else {
            transferencia.setEstado(EstadoTransferencia.CON_FALTANTES);
            TratamientoFaltante tratamiento = request.getTratamiento() != null
                    ? request.getTratamiento() : TratamientoFaltante.RECLAMACION;
            BigDecimal faltante = despachada.subtract(cantidadRecibida);
            TransferenciaFaltante faltanteEntity = TransferenciaFaltante.builder()
                    .transferencia(transferencia)
                    .cantidadFaltante(faltante)
                    .tratamiento(tratamiento)
                    .usuario(resolveUsuario())
                    .fecha(LocalDateTime.now())
                    .build();
            faltanteRepository.save(faltanteEntity);
        }

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
            ReservaStock reserva = reservaRepository.findByTransferenciaId(id)
                    .orElseThrow(() -> new BusinessRuleException(
                            "No existe reserva de stock para esta transferencia."));
            if (reserva.getEstado() == EstadoReserva.ACTIVA) {
                reserva.setEstado(EstadoReserva.LIBERADA);
                reserva.setFechaLiberacion(LocalDateTime.now());
                reservaRepository.save(reserva);

                Existencia existencia = getExistenciaOrigen(transferencia);
                existencia.setCantidadReservada(
                        existencia.getCantidadReservada().subtract(reserva.getCantidad()));
                existenciaRepository.save(existencia);
            }
        }

        transferencia.setEstado(EstadoTransferencia.CANCELADA);
        return toResponse(transferenciaRepository.save(transferencia));
    }

    private void validarDisponibilidadOrigen(Transferencia transferencia) {
        Existencia existencia = getExistenciaOrigen(transferencia);
        BigDecimal disponible = existencia.getCantidadFisica()
                .subtract(existencia.getCantidadReservada());
        if (disponible.compareTo(transferencia.getCantidadSolicitada()) < 0) {
            throw new BusinessRuleException(
                    "Cantidad solicitada excede la cantidad disponible de origen. "
                            + "Disponible: " + disponible + ", solicitada: "
                            + transferencia.getCantidadSolicitada());
        }
    }

    private void crearReserva(Transferencia transferencia) {
        ReservaStock reserva = ReservaStock.builder()
                .transferencia(transferencia)
                .producto(transferencia.getProducto())
                .sucursal(transferencia.getSucursalOrigen())
                .cantidad(transferencia.getCantidadSolicitada())
                .estado(EstadoReserva.ACTIVA)
                .fechaCreacion(LocalDateTime.now())
                .build();
        reservaRepository.save(reserva);

        Existencia existencia = getExistenciaOrigen(transferencia);
        existencia.setCantidadReservada(
                existencia.getCantidadReservada().add(transferencia.getCantidadSolicitada()));
        existenciaRepository.save(existencia);
    }

    private Existencia getExistenciaOrigen(Transferencia transferencia) {
        return getOrCreateExistencia(transferencia.getProducto(), transferencia.getSucursalOrigen());
    }

    private Existencia getExistenciaDestino(Transferencia transferencia) {
        return getOrCreateExistencia(transferencia.getProducto(), transferencia.getSucursalDestino());
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

        BigDecimal disponible = null;
        Existencia existenciaOpt = existenciaRepository
                .findByProductoIdAndSucursalId(
                        transferencia.getProducto().getId(),
                        transferencia.getSucursalOrigen().getId()).orElse(null);
        if (existenciaOpt != null) {
            disponible = existenciaOpt.getCantidadFisica()
                    .subtract(existenciaOpt.getCantidadReservada());
        }
        response.setCantidadDisponibleOrigen(disponible);

        reservaRepository.findByTransferenciaId(transferencia.getId()).ifPresent(r ->
                response.setReserva(TransferenciaResponse.ReservaResponse.builder()
                        .id(r.getId())
                        .productoId(r.getProducto().getId())
                        .sucursalId(r.getSucursal().getId())
                        .cantidad(r.getCantidad())
                        .estado(r.getEstado())
                        .fechaCreacion(r.getFechaCreacion())
                        .fechaLiberacion(r.getFechaLiberacion())
                        .build()));

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

        response.setFaltantes(faltanteRepository.findByTransferenciaId(transferencia.getId())
                .stream().map(mapper::toFaltanteResponse).toList());

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
