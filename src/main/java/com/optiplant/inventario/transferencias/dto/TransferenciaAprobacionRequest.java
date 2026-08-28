package com.optiplant.inventario.transferencias.dto;

import com.optiplant.inventario.transferencias.entity.DecisionAprobacion;
import com.optiplant.inventario.transferencias.entity.RolAprobacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaAprobacionRequest {

    @NotNull(message = "El gerente es obligatorio")
    private Long gerenteId;

    @NotNull(message = "El rol de aprobación es obligatorio")
    private RolAprobacion rolAprobacion;

    @NotNull(message = "La decisión es obligatoria")
    private DecisionAprobacion decision;

    @Size(max = 500)
    private String observacion;
}
