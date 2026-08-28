package com.optiplant.inventario.compras.repository;

import com.optiplant.inventario.compras.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}
