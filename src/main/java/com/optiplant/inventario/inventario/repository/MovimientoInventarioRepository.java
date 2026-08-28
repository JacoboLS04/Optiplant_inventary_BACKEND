package com.optiplant.inventario.inventario.repository;

import com.optiplant.inventario.inventario.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("""
        SELECT m FROM MovimientoInventario m
        LEFT JOIN FETCH m.producto
        LEFT JOIN FETCH m.sucursal
        LEFT JOIN FETCH m.usuario
        WHERE (:productoId IS NULL OR m.producto.id = :productoId)
          AND (:sucursalId IS NULL OR m.sucursal.id = :sucursalId)
        ORDER BY m.fecha DESC
    """)
    Page<MovimientoInventario> search(@Param("productoId") Long productoId,
                                      @Param("sucursalId") Long sucursalId,
                                      Pageable pageable);

    List<MovimientoInventario> findTop20ByOrderByFechaDesc();
}
