package com.optiplant.inventario.ventas.repository;

import com.optiplant.inventario.ventas.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long>,
        JpaSpecificationExecutor<Venta> {

    Optional<Venta> findByCodigo(String codigo);

    long countByCodigoStartingWith(String prefijo);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha >= :desde")
    BigDecimal sumTotalDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v "
            + "WHERE v.fecha >= :desde AND v.fecha < :hasta")
    BigDecimal sumTotalEntre(@Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta);
}
