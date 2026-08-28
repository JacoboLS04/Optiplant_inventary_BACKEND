package com.optiplant.inventario.catalogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecioProductoResponse {

    private Long id;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private BigDecimal precio;
}
