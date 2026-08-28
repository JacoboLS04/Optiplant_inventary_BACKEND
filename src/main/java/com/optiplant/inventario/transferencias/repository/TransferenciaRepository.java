package com.optiplant.inventario.transferencias.repository;

import com.optiplant.inventario.transferencias.entity.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long>,
        JpaSpecificationExecutor<Transferencia> {

    Optional<Transferencia> findByCodigo(String codigo);

    long countByCodigoStartingWith(String prefijo);
}
