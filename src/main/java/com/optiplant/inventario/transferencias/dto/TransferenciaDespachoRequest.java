package com.optiplant.inventario.transferencias.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TransferenciaDespachoRequest {

    @NotEmpty(message = "Debe indicarse al menos una cantidad despachada")
    @Valid
    private List<LineaDespacho> lineas;

    @Size(max = 200)
    private String transportista;

    @Size(max = 100)
    private String guia;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaDespacho {

        @NotNull(message = "La línea de transferencia es obligatoria")
        private Long transferenciaLineaId;

        @NotNull(message = "La cantidad despachada es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        private BigDecimal cantidadDespachada;
    }
}
