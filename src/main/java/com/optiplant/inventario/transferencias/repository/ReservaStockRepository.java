package com.optiplant.inventario.transferencias.repository;

import com.optiplant.inventario.transferencias.entity.ReservaStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservaStockRepository extends JpaRepository<ReservaStock, Long> {

    Optional<ReservaStock> findByLineaId(Long transferenciaLineaId);

    List<ReservaStock> findByLineaTransferenciaId(Long transferenciaId);
}
