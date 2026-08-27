package com.optiplant.inventario.identidad.repository;

import com.optiplant.inventario.identidad.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
