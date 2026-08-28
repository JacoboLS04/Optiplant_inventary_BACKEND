package com.optiplant.inventario.transferencias.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaDespachoRequest {

    @NotNull(message = "La cantidad despachada es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidadDespachada;

    @Size(max = 200)
    private String transportista;

    @Size(max = 100)
    private String guia;
}
