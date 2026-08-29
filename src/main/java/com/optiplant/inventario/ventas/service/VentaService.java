package com.optiplant.inventario.ventas.service;

import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.catalogo.service.PrecioService;
import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.common.security.UsuarioActualService;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioRequest;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.service.MovimientoInventarioService;
import com.optiplant.inventario.ventas.dto.NuevaVentaRequest;
import com.optiplant.inventario.ventas.dto.ProductoVentaResponse;
import com.optiplant.inventario.ventas.dto.VentaResponse;
import com.optiplant.inventario.ventas.entity.Venta;
import com.optiplant.inventario.ventas.entity.VentaLinea;
import com.optiplant.inventario.ventas.mapper.VentaMapper;
import com.optiplant.inventario.ventas.repository.VentaRepository;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private static final String PREFIJO_CODIGO = "VENTA-";

    private final VentaRepository ventaRepository;
    private final ExistenciaRepository existenciaRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final PrecioService precioService;
    private final VentaMapper mapper;
    private final UsuarioActualService usuarioActualService;

    @Transactional(readOnly = true)
    public List<ProductoVentaResponse> catalogo() {
        return existenciaRepository.findAll().stream()
                .filter(e -> e.getCantidadFisica()
                        .subtract(e.getCantidadReservada()).compareTo(BigDecimal.ZERO) > 0)
                .map(this::toProductoVenta)
                .toList();
    }

    @Transactional
    public VentaResponse registrar(NuevaVentaRequest request) {
        Sucursal sucursal = sucursalRepository.findById(request.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada: " + request.getSucursalId()));

        usuarioActualService.validarAccesoSucursal(sucursal.getId());

        BigDecimal descuento = request.getDescuentoPorcentaje() != null
                ? request.getDescuentoPorcentaje() : BigDecimal.ZERO;

        Venta venta = Venta.builder()
                .codigo(generarCodigo())
                .sucursal(sucursal)
                .usuario(resolveUsuario())
                .descuentoPorcentaje(descuento)
                .fecha(LocalDateTime.now())
                .build();

        BigDecimal subtotalTotal = BigDecimal.ZERO;
        BigDecimal unidadesTotal = BigDecimal.ZERO;
        List<VentaLinea> lineas = new ArrayList<>();

        for (NuevaVentaRequest.LineaRequest lineaReq : request.getLineas()) {
            Existencia existencia = existenciaRepository
                    .findByProductoIdAndSucursalId(lineaReq.getProductoId(), request.getSucursalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe existencia del producto "
                                    + lineaReq.getProductoId() + " en la sucursal "
                                    + request.getSucursalId()));

            BigDecimal disponible = existencia.getCantidadFisica()
                    .subtract(existencia.getCantidadReservada());
            if (lineaReq.getCantidad().compareTo(disponible) > 0) {
                throw new BusinessRuleException(
                        "Stock insuficiente para el producto "
                                + existencia.getProducto().getNombre()
                                + ". Disponible: " + disponible
                                + ", solicitado: " + lineaReq.getCantidad());
            }

            BigDecimal precio = precioService.obtenerPrecioGlobal(lineaReq.getProductoId());
            BigDecimal subtotalLinea = lineaReq.getCantidad().multiply(precio);

            VentaLinea linea = VentaLinea.builder()
                    .venta(venta)
                    .producto(existencia.getProducto())
                    .cantidad(lineaReq.getCantidad())
                    .precioUnitario(precio)
                    .descuento(BigDecimal.ZERO)
                    .subtotal(subtotalLinea)
                    .build();
            lineas.add(linea);

            subtotalTotal = subtotalTotal.add(subtotalLinea);
            unidadesTotal = unidadesTotal.add(lineaReq.getCantidad());

            movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                    lineaReq.getProductoId(),
                    request.getSucursalId(),
                    "retiro",
                    "venta - " + venta.getCodigo(),
                    lineaReq.getCantidad(),
                    resolverIdUsuario()));
        }

        venta.setLineas(lineas);
        venta.setSubtotal(subtotalTotal);
        venta.setUnidades(unidadesTotal);

        BigDecimal factor = BigDecimal.ONE
                .subtract(descuento.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        venta.setTotal(subtotalTotal.multiply(factor).setScale(2, RoundingMode.HALF_UP));

        return mapper.toResponse(ventaRepository.save(venta));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<VentaResponse> buscar(
            Long sucursalId, LocalDateTime fechaDesde,
            LocalDateTime fechaHasta, String busqueda, int page, int size) {

        Specification<Venta> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (busqueda != null && !busqueda.isBlank()) {
                String patron = "%" + busqueda.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), patron),
                        cb.like(cb.lower(root.get("sucursal").get("nombre")), patron),
                        cb.like(cb.lower(root.get("usuario").get("nombre")), patron)
                ));
            }
            if (sucursalId != null) {
                predicates.add(cb.equal(root.get("sucursal").get("id"), sucursalId));
            }
            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Venta> ventas = ventaRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha")));

        return new PaginatedResponse<>(
                ventas.getContent().stream().map(mapper::toResponse).toList(),
                page, size, ventas.getTotalElements(), ventas.getTotalPages());
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        return mapper.toResponse(ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + id)));
    }

    private ProductoVentaResponse toProductoVenta(Existencia existencia) {
        Producto producto = existencia.getProducto();
        return ProductoVentaResponse.builder()
                .productoId(producto.getId())
                .sku(producto.getSku())
                .nombre(producto.getNombre())
                .categoriaId(producto.getCategoria() != null
                        ? producto.getCategoria().getId() : null)
                .categoria(producto.getCategoria() != null
                        ? producto.getCategoria().getNombre() : null)
                .sucursalId(existencia.getSucursal().getId())
                .sucursal(existencia.getSucursal().getNombre())
                .precioUnitario(precioService.obtenerPrecioGlobal(producto.getId()))
                .stockDisponible(existencia.getCantidadFisica()
                        .subtract(existencia.getCantidadReservada()))
                .build();
    }

    private String generarCodigo() {
        long secuencia = ventaRepository.countByCodigoStartingWith(PREFIJO_CODIGO) + 1;
        String codigo;
        while (true) {
            codigo = PREFIJO_CODIGO + String.format("%04d", secuencia);
            if (ventaRepository.findByCodigo(codigo).isEmpty()) {
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
