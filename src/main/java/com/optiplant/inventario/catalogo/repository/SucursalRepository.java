package com.optiplant.inventario.catalogo.repository;

import com.optiplant.inventario.catalogo.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByEstado(String estado);
}
