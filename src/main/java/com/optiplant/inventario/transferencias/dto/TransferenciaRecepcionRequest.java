package com.optiplant.inventario.transferencias.dto;

import com.optiplant.inventario.transferencias.entity.TratamientoFaltante;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaRecepcionRequest {

    @NotNull(message = "La cantidad recibida es obligatoria")
    @DecimalMin(value = "0.00", message = "La cantidad recibida no puede ser negativa")
    private BigDecimal cantidadRecibida;

    private TratamientoFaltante tratamiento;
}
