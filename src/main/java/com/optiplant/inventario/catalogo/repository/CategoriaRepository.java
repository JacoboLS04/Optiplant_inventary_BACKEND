package com.optiplant.inventario.catalogo.repository;

import com.optiplant.inventario.catalogo.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
