package com.optiplant.inventario.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExistenciaResponse {
    private Long id;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private Long sucursalId;
    private String nombreSucursal;
    private BigDecimal cantidadFisica;
    private BigDecimal cantidadReservada;
    private BigDecimal cantidadDisponible;
    private BigDecimal stockMinimo;
    private String estadoStock;
    private BigDecimal precio;
    private LocalDateTime updatedAt;
}
