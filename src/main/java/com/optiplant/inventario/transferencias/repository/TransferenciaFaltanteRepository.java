package com.optiplant.inventario.transferencias.repository;

import com.optiplant.inventario.transferencias.entity.TransferenciaFaltante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaFaltanteRepository extends JpaRepository<TransferenciaFaltante, Long> {

    List<TransferenciaFaltante> findByTransferenciaId(Long transferenciaId);
}
