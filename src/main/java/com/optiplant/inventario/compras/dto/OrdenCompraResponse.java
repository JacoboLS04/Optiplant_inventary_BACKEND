package com.optiplant.inventario.compras.dto;

import com.optiplant.inventario.compras.entity.EstadoOrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraResponse {

    private Long id;
    private String codigo;
    private Long proveedorId;
    private String nombreProveedor;
    private Long sucursalDestinoId;
    private String nombreSucursal;
    private Long usuarioId;
    private String nombreUsuario;
    private EstadoOrdenCompra estado;
    private LocalDateTime fechaEmision;
    private LocalDate fechaEntregaEstimada;
    private String transportista;
    private String guia;
    private String condicionesPago;
    private BigDecimal total;
    private List<LineaResponse> lineas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineaResponse {
        private Long id;
        private Long productoId;
        private String sku;
        private String nombreProducto;
        private BigDecimal cantidadOrdenada;
        private BigDecimal cantidadRecibida;
        private BigDecimal cantidadPendiente;
        private BigDecimal precioUnitario;
        private BigDecimal descuento;
        private BigDecimal subtotal;
    }
}
