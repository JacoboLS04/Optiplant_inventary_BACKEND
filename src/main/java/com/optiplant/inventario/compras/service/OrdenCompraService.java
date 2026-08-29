package com.optiplant.inventario.compras.service;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.compras.dto.OrdenCompraEstadoRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraRecepcionRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraRequest;
import com.optiplant.inventario.compras.dto.OrdenCompraResponse;
import com.optiplant.inventario.compras.entity.EstadoOrdenCompra;
import com.optiplant.inventario.compras.entity.OrdenCompra;
import com.optiplant.inventario.compras.entity.OrdenCompraLinea;
import com.optiplant.inventario.compras.entity.Proveedor;
import com.optiplant.inventario.compras.mapper.ComprasMapper;
import com.optiplant.inventario.compras.repository.OrdenCompraRepository;
import com.optiplant.inventario.compras.repository.ProveedorRepository;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioRequest;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.service.MovimientoInventarioService;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private static final String PREFIJO_CODIGO = "PO-";

    private static final Map<EstadoOrdenCompra, List<EstadoOrdenCompra>> TRANSICIONES;

    static {
        TRANSICIONES = new EnumMap<>(EstadoOrdenCompra.class);
        TRANSICIONES.put(EstadoOrdenCompra.BORRADOR,
                List.of(EstadoOrdenCompra.ENVIADA, EstadoOrdenCompra.CANCELADA));
        TRANSICIONES.put(EstadoOrdenCompra.ENVIADA,
                List.of(EstadoOrdenCompra.EN_TRANSITO, EstadoOrdenCompra.CANCELADA));
        TRANSICIONES.put(EstadoOrdenCompra.EN_TRANSITO,
                List.of(EstadoOrdenCompra.RECIBIDA));
        TRANSICIONES.put(EstadoOrdenCompra.RECIBIDA, List.of());
        TRANSICIONES.put(EstadoOrdenCompra.CANCELADA, List.of());
    }

    private static final List<EstadoOrdenCompra> ESTADOS_RECEPCIONABLES =
            List.of(EstadoOrdenCompra.ENVIADA, EstadoOrdenCompra.EN_TRANSITO);

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExistenciaRepository existenciaRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final ComprasMapper mapper;

    @Transactional
    public OrdenCompraResponse crear(OrdenCompraRequest request) {
        OrdenCompra orden = mapper.toEntity(request);

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor no encontrado: " + request.getProveedorId()));
        Sucursal sucursal = sucursalRepository.findById(request.getSucursalDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada: " + request.getSucursalDestinoId()));

        orden.setProveedor(proveedor);
        orden.setSucursalDestino(sucursal);
        orden.setUsuario(resolveUsuario());
        orden.setEstado(EstadoOrdenCompra.BORRADOR);
        orden.setCodigo(generarCodigo());

        request.getLineas().forEach(lr -> {
            Producto producto = productoRepository.findById(lr.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: " + lr.getProductoId()));
            OrdenCompraLinea linea = OrdenCompraLinea.builder()
                    .producto(producto)
                    .cantidadOrdenada(lr.getCantidadOrdenada())
                    .cantidadRecibida(BigDecimal.ZERO)
                    .precioUnitario(lr.getPrecioUnitario())
                    .descuento(lr.getDescuento() != null ? lr.getDescuento() : BigDecimal.ZERO)
                    .build();
            linea.setOrdenCompra(orden);
            orden.getLineas().add(linea);
        });

        return mapper.toResponse(ordenCompraRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<OrdenCompraResponse> buscar(
            Long sucursalId, EstadoOrdenCompra estado, String busqueda,
            Long productoId, int page, int size) {

        Specification<OrdenCompra> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.isBlank()) {
                String patron = "%" + busqueda.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), patron),
                        cb.like(cb.lower(root.get("proveedor").get("nombre")), patron)
                ));
            }

            if (sucursalId != null) {
                predicates.add(cb.equal(root.get("sucursalDestino").get("id"), sucursalId));
            }

            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            if (productoId != null) {
                query.distinct(true);
                predicates.add(cb.equal(
                        root.join("lineas").get("producto").get("id"), productoId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<OrdenCompra> ordenes = ordenCompraRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaEmision")));

        return new PaginatedResponse<>(
                ordenes.getContent().stream().map(mapper::toResponse).toList(),
                page, size, ordenes.getTotalElements(), ordenes.getTotalPages());
    }

    @Transactional(readOnly = true)
    public OrdenCompraResponse obtener(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public OrdenCompraResponse cambiarEstado(Long id, OrdenCompraEstadoRequest request) {
        OrdenCompra orden = findOrThrow(id);
        EstadoOrdenCompra destino = request.getEstado();
        validarTransicion(orden.getEstado(), destino);
        orden.setEstado(destino);
        return mapper.toResponse(ordenCompraRepository.save(orden));
    }

    @Transactional
    public OrdenCompraResponse recibir(Long id, OrdenCompraRecepcionRequest request) {
        OrdenCompra orden = findOrThrow(id);

        if (!ESTADOS_RECEPCIONABLES.contains(orden.getEstado())) {
            throw new BusinessRuleException(
                    "La orden solo puede recibirse desde los estados: ENVIADA o EN_TRANSITO. "
                            + "Estado actual: " + orden.getEstado());
        }

        boolean algunaRecibida = false;
        for (OrdenCompraRecepcionRequest.LineaRecepcion recepcion : request.getLineas()) {
            OrdenCompraLinea linea = orden.getLineas().stream()
                    .filter(l -> l.getId().equals(recepcion.getLineaId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Línea no encontrada: " + recepcion.getLineaId()));

            BigDecimal cantidad = recepcion.getCantidadRecibida();
            BigDecimal pendiente = linea.getCantidadPendiente();
            if (cantidad.compareTo(pendiente) > 0) {
                throw new BusinessRuleException(
                        "No puede recibir más de lo pendiente para la línea " + linea.getId()
                                + ". Pendiente: " + pendiente);
            }
            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            linea.setCantidadRecibida(linea.getCantidadRecibida().add(cantidad));
            algunaRecibida = true;

            MovimientoInventarioRequest movimiento = new MovimientoInventarioRequest(
                    linea.getProducto().getId(),
                    orden.getSucursalDestino().getId(),
                    "ingreso",
                    "compra - " + orden.getCodigo(),
                    cantidad,
                    resolverIdUsuario());
            movimientoInventarioService.registrar(movimiento);

            actualizarCostoPromedio(linea, cantidad);
        }

        if (!algunaRecibida) {
            throw new BusinessRuleException(
                    "No se indicó ninguna línea con cantidad a recibir.");
        }

        boolean completa = orden.getLineas().stream()
                .allMatch(l -> l.getCantidadPendiente().compareTo(BigDecimal.ZERO) <= 0);
        if (completa) {
            orden.setEstado(EstadoOrdenCompra.RECIBIDA);
        }

        return mapper.toResponse(ordenCompraRepository.save(orden));
    }

    private void actualizarCostoPromedio(OrdenCompraLinea linea, BigDecimal cantidadRecibida) {
        Optional<Existencia> existOpt = existenciaRepository
                .findByProductoIdAndSucursalId(
                        linea.getProducto().getId(), linea.getOrdenCompra().getSucursalDestino().getId());
        if (existOpt.isEmpty()) {
            return;
        }
        Existencia exist = existOpt.get();
        BigDecimal stockActual = exist.getCantidadFisica().subtract(cantidadRecibida);
        BigDecimal costoActual = exist.getCostoPromedio();
        BigDecimal costoUnitarioNeto = linea.getPrecioUnitario()
                .subtract(linea.getDescuento());
        if (costoUnitarioNeto.compareTo(BigDecimal.ZERO) < 0) {
            costoUnitarioNeto = BigDecimal.ZERO;
        }

        BigDecimal nuevoCosto;
        if (stockActual.compareTo(BigDecimal.ZERO) <= 0) {
            nuevoCosto = costoUnitarioNeto;
        } else {
            BigDecimal numerador = stockActual.multiply(costoActual)
                    .add(cantidadRecibida.multiply(costoUnitarioNeto));
            BigDecimal denominador = stockActual.add(cantidadRecibida);
            if (denominador.compareTo(BigDecimal.ZERO) <= 0) {
                nuevoCosto = costoUnitarioNeto;
            } else {
                nuevoCosto = numerador.divide(denominador, 4, RoundingMode.HALF_UP);
            }
        }
        exist.setCostoPromedio(nuevoCosto);
        existenciaRepository.save(exist);
    }

    private void validarTransicion(EstadoOrdenCompra actual, EstadoOrdenCompra destino) {
        if (actual == destino) {
            throw new BusinessRuleException("La orden ya se encuentra en estado " + actual);
        }
        List<EstadoOrdenCompra> permitidos = TRANSICIONES.get(actual);
        if (permitidos == null || !permitidos.contains(destino)) {
            throw new BusinessRuleException(
                    "Transición no permitida de " + actual + " a " + destino);
        }
    }

    private String generarCodigo() {
        long secuencia = ordenCompraRepository.countByCodigoStartingWith(PREFIJO_CODIGO) + 1;
        String codigo;
        while (true) {
            codigo = PREFIJO_CODIGO + String.format("%03d", secuencia);
            if (ordenCompraRepository.findByCodigo(codigo).isEmpty()) {
                return codigo;
            }
            secuencia++;
        }
    }

    private OrdenCompra findOrThrow(Long id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra no encontrada: " + id));
        orden.getLineas().size();
        return orden;
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
