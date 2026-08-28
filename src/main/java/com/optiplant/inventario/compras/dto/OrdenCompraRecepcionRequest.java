package com.optiplant.inventario.compras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraRecepcionRequest {

    @NotNull(message = "Debe indicar las líneas a recibir")
    @Builder.Default
    private List<LineaRecepcion> lineas = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaRecepcion {

        @NotNull(message = "El id de línea es obligatorio")
        private Long lineaId;

        @NotNull(message = "La cantidad recibida es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        private BigDecimal cantidadRecibida;
    }
}
