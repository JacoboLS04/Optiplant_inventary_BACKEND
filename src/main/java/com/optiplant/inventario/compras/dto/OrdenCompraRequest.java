package com.optiplant.inventario.compras.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraRequest {

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    @NotNull(message = "La sucursal destino es obligatoria")
    private Long sucursalDestinoId;

    private LocalDate fechaEntregaEstimada;

    @Size(max = 200)
    private String transportista;

    @Size(max = 100)
    private String guia;

    @Size(max = 300)
    private String condicionesPago;

    @Valid
    @NotNull(message = "Las líneas son obligatorias")
    @Size(min = 1, message = "Debe incluir al menos una línea")
    @Builder.Default
    private List<OrdenCompraLineaRequest> lineas = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdenCompraLineaRequest {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad ordenada es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        private BigDecimal cantidadOrdenada;

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        private BigDecimal precioUnitario;

        @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
        private BigDecimal descuento;
    }
}
