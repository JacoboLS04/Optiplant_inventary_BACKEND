package com.optiplant.inventario.transferencias.repository;

import com.optiplant.inventario.transferencias.entity.RolAprobacion;
import com.optiplant.inventario.transferencias.entity.TransferenciaAprobacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferenciaAprobacionRepository extends JpaRepository<TransferenciaAprobacion, Long> {

    List<TransferenciaAprobacion> findByTransferenciaId(Long transferenciaId);

    boolean existsByTransferenciaIdAndRolAprobacion(Long transferenciaId, RolAprobacion rolAprobacion);
}
