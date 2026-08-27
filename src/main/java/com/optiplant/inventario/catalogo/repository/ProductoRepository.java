package com.optiplant.inventario.catalogo.repository;

import com.optiplant.inventario.catalogo.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsBySku(String sku);

    @Query("""
        SELECT p FROM Producto p
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.unidadBase
        WHERE (:pattern IS NULL OR LOWER(p.nombre) LIKE :pattern
                              OR LOWER(p.sku)  LIKE :pattern)
          AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
    """)
    Page<Producto> search(@Param("pattern") String pattern,
                          @Param("categoriaId") Long categoriaId,
                          Pageable pageable);
}
