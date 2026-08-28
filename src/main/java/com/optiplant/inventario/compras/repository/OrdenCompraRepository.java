package com.optiplant.inventario.compras.repository;

import com.optiplant.inventario.compras.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long>,
        JpaSpecificationExecutor<OrdenCompra> {

    Optional<OrdenCompra> findByCodigo(String codigo);

    long countByCodigoStartingWith(String prefix);
}
