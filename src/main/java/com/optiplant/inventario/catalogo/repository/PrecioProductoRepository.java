package com.optiplant.inventario.catalogo.repository;

import com.optiplant.inventario.catalogo.entity.PrecioProducto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrecioProductoRepository extends JpaRepository<PrecioProducto, Long> {

    Optional<PrecioProducto> findByListaPrecioIdAndProductoId(Long listaPrecioId, Long productoId);
}
