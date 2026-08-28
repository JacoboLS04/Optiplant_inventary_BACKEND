package com.optiplant.inventario.compras.repository;

import com.optiplant.inventario.compras.entity.OrdenCompraLinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCompraLineaRepository extends JpaRepository<OrdenCompraLinea, Long> {

    List<OrdenCompraLinea> findByOrdenCompraId(Long ordenCompraId);
}
