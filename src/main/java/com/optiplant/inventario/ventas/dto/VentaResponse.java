package com.optiplant.inventario.ventas.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VentaResponse {

    private Long id;
    private String codigo;
    private Long sucursalId;
    private String nombreSucursal;
    private Long usuarioId;
    private String nombreUsuario;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal subtotal;
    private BigDecimal total;
    private BigDecimal unidades;
    private LocalDateTime fecha;
    private List<LineaResponse> lineas;

    @Data
    @Builder
    public static class LineaResponse {
        private Long id;
        private Long productoId;
        private String sku;
        private String nombreProducto;
        private BigDecimal cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal descuento;
        private BigDecimal subtotal;
    }
}
