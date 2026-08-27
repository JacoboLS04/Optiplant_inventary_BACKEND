package com.optiplant.inventario.inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioResponse {
    private Long id;
    private Long productoId;
    private String sku;
    private String nombreProducto;
    private Long sucursalId;
    private String nombreSucursal;
    private String tipo;
    private String motivo;
    private BigDecimal cantidad;
    private Long usuarioId;
    private String nombreUsuario;
    private LocalDateTime fecha;
}
