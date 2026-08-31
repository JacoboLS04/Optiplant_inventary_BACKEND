package com.optiplant.inventario.catalogo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrecioProductoRequest {

    /**
     * En POST llega en el cuerpo; en PUT lo inyecta el controller desde la
     * ruta, por eso no se valida con @NotNull aquí (permitiendo body {precio}).
     */
    private Long productoId;

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser negativo")
    private BigDecimal precio;
}
