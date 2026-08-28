package com.optiplant.inventario.transferencias.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaRecepcionRequest {

    @NotEmpty(message = "Debe indicarse al menos una cantidad recibida")
    @Valid
    private List<LineaRecepcion> lineas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaRecepcion {

        @NotNull(message = "La línea de transferencia es obligatoria")
        private Long transferenciaLineaId;

        @NotNull(message = "La cantidad recibida es obligatoria")
        @DecimalMin(value = "0.00", message = "La cantidad recibida no puede ser negativa")
        private BigDecimal cantidadRecibida;

        private com.optiplant.inventario.transferencias.entity.TratamientoFaltante tratamiento;
    }
}
