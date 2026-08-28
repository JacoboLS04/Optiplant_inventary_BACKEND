package com.optiplant.inventario.transferencias.repository;

import com.optiplant.inventario.transferencias.entity.TransferenciaLinea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaLineaRepository extends JpaRepository<TransferenciaLinea, Long> {

    List<TransferenciaLinea> findByTransferenciaId(Long transferenciaId);
}
