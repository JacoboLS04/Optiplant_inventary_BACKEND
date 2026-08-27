package com.optiplant.inventario.inventario.repository;

import com.optiplant.inventario.inventario.entity.Existencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExistenciaRepository extends JpaRepository<Existencia, Long>,
        JpaSpecificationExecutor<Existencia> {

    Optional<Existencia> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

    @Query("""
        SELECT COALESCE(SUM(e.cantidadFisica), 0) FROM Existencia e
        WHERE e.producto.id = :productoId
    """)
    java.math.BigDecimal sumCantidadFisicaByProductoId(@Param("productoId") Long productoId);
}
