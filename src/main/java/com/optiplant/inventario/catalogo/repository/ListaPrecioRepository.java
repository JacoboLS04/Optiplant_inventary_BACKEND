package com.optiplant.inventario.catalogo.repository;

import com.optiplant.inventario.catalogo.entity.ListaPrecio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListaPrecioRepository extends JpaRepository<ListaPrecio, Long> {

    Optional<ListaPrecio> findBySucursalIsNull();
}
