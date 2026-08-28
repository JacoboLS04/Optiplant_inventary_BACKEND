package com.optiplant.inventario.ventas.repository;

import com.optiplant.inventario.ventas.entity.VentaLinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaLineaRepository extends JpaRepository<VentaLinea, Long> {

    List<VentaLinea> findByVentaId(Long ventaId);
}
