package com.optiplant.inventario.ventas.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductoVentaResponse {

    private Long productoId;
    private String sku;
    private String nombre;
    private Long categoriaId;
    private String categoria;
    private Long sucursalId;
    private String sucursal;
    private BigDecimal precioUnitario;
    private BigDecimal stockDisponible;
}
