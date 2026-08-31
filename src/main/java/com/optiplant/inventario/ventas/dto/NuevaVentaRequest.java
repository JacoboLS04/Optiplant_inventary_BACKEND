package com.optiplant.inventario.ventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
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
public class NuevaVentaRequest {

    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;

    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin(value = "0", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100", message = "El descuento no puede superar 100%")
    private BigDecimal descuentoPorcentaje;

    @NotEmpty(message = "La venta debe incluir al menos una línea")
    @Valid
    private List<LineaRequest> lineas;

    private String medioPago;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaRequest {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        private BigDecimal cantidad;
    }
}
