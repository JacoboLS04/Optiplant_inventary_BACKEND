package com.optiplant.inventario.catalogo.service;

import com.optiplant.inventario.catalogo.dto.PrecioProductoRequest;
import com.optiplant.inventario.catalogo.dto.PrecioProductoResponse;
import com.optiplant.inventario.catalogo.entity.ListaPrecio;
import com.optiplant.inventario.catalogo.entity.PrecioProducto;
import com.optiplant.inventario.catalogo.entity.Producto;
import com.optiplant.inventario.catalogo.repository.ListaPrecioRepository;
import com.optiplant.inventario.catalogo.repository.PrecioProductoRepository;
import com.optiplant.inventario.catalogo.repository.ProductoRepository;
import com.optiplant.inventario.common.exception.BusinessRuleException;
import com.optiplant.inventario.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrecioService {

    private final PrecioProductoRepository precioProductoRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public PrecioProductoResponse setPrecio(PrecioProductoRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado: " + request.getProductoId()));

        ListaPrecio listaGlobal = obtenerListaGlobal();

        Optional<PrecioProducto> existente = precioProductoRepository
                .findByListaPrecioIdAndProductoId(listaGlobal.getId(), producto.getId());

        PrecioProducto precioProducto;
        if (existente.isPresent()) {
            precioProducto = existente.get();
            precioProducto.setPrecio(request.getPrecio());
        } else {
            precioProducto = PrecioProducto.builder()
                    .listaPrecio(listaGlobal)
                    .producto(producto)
                    .precio(request.getPrecio())
                    .build();
        }

        precioProductoRepository.save(precioProducto);

        return PrecioProductoResponse.builder()
                .id(precioProducto.getId())
                .productoId(producto.getId())
                .sku(producto.getSku())
                .nombreProducto(producto.getNombre())
                .precio(request.getPrecio())
                .build();
    }

    @Transactional(readOnly = true)
    public BigDecimal obtenerPrecioGlobal(Long productoId) {
        ListaPrecio listaGlobal = listaPrecioRepository.findBySucursalIsNull()
                .orElse(null);
        if (listaGlobal == null) {
            return BigDecimal.ZERO;
        }
        return precioProductoRepository
                .findByListaPrecioIdAndProductoId(listaGlobal.getId(), productoId)
                .map(PrecioProducto::getPrecio)
                .orElse(BigDecimal.ZERO);
    }

    private ListaPrecio obtenerListaGlobal() {
        return listaPrecioRepository.findBySucursalIsNull()
                .orElseThrow(() -> new BusinessRuleException(
                        "No existe una lista de precios global configurada"));
    }
}
