package com.optiplant.inventario.inventario.service;

import com.optiplant.inventario.common.dto.PaginatedResponse;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.entity.Sucursal;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.catalogo.repository.SucursalRepository;
import com.optiplant.inventario.identidad.entity.Usuario;
import com.optiplant.inventario.identidad.repository.UsuarioRepository;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioRequest;
import com.optiplant.inventario.inventario.dto.MovimientoInventarioResponse;
import com.optiplant.inventario.inventario.entity.Existencia;
import com.optiplant.inventario.inventario.entity.MovimientoInventario;
import com.optiplant.inventario.inventario.mapper.MovimientoInventarioMapper;
import com.optiplant.inventario.inventario.repository.ExistenciaRepository;
import com.optiplant.inventario.inventario.repository.MovimientoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository repository;
    private final ExistenciaRepository existenciaRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioMapper mapper;

    @Transactional
    public MovimientoInventarioResponse registrar(MovimientoInventarioRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado: " + request.getProductoId()));

        Sucursal sucursal = sucursalRepository.findById(request.getSucursalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sucursal no encontrada: " + request.getSucursalId()));

        if (!"ingreso".equals(request.getTipo()) && !"retiro".equals(request.getTipo())) {
            throw new BusinessRuleException(
                    "Tipo de movimiento inválido. Use 'ingreso' o 'retiro'.");
        }

        Usuario usuario = resolveUsuario(request.getUsuarioId());

        Optional<Existencia> existenciaOpt = existenciaRepository
                .findByProductoIdAndSucursalId(request.getProductoId(), request.getSucursalId());

        Existencia existencia;
        if (existenciaOpt.isPresent()) {
            existencia = existenciaOpt.get();
        } else {
            existencia = Existencia.builder()
                    .producto(producto)
                    .sucursal(sucursal)
                    .cantidadFisica(BigDecimal.ZERO)
                    .cantidadReservada(BigDecimal.ZERO)
                    .stockMinimo(BigDecimal.ZERO)
                    .build();
        }

        if ("retiro".equals(request.getTipo())) {
            BigDecimal nuevaCantidad = existencia.getCantidadFisica()
                    .subtract(request.getCantidad());
            if (nuevaCantidad.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException(
                        "Stock insuficiente. Disponible: " + existencia.getCantidadFisica()
                                + ", solicitado: " + request.getCantidad());
            }
            existencia.setCantidadFisica(nuevaCantidad);
        } else {
            existencia.setCantidadFisica(
                    existencia.getCantidadFisica().add(request.getCantidad()));
        }

        existencia.setUpdatedAt(LocalDateTime.now());
        existenciaRepository.save(existencia);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipo(request.getTipo())
                .motivo(request.getMotivo())
                .cantidad(request.getCantidad())
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .build();

        return mapper.toResponse(repository.save(movimiento));
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<MovimientoInventarioResponse> search(
            Long productoId, Long sucursalId, int page, int size) {

        Page<MovimientoInventario> movimientos = repository
                .search(productoId, sucursalId, PageRequest.of(page, size));

        return new PaginatedResponse<>(
                movimientos.getContent().stream().map(mapper::toResponse).toList(),
                page, size,
                movimientos.getTotalElements(), movimientos.getTotalPages());
    }

    private Usuario resolveUsuario(Long explicitUsuarioId) {
        if (explicitUsuarioId != null) {
            return usuarioRepository.findById(explicitUsuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado: " + explicitUsuarioId));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            return usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario autenticado no encontrado: " + email));
        }

        return null;
    }
}
