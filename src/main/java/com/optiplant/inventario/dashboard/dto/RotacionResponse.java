package com.optiplant.inventario.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotacionResponse {

    private Integer periodoDias;
    private BigDecimal totalUnidades;
    private java.util.List<ProductoRotacion> altaDemanda;
    private java.util.List<ProductoRotacion> bajaDemanda;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoRotacion {
        private Long productoId;
        private String sku;
        private String nombre;
        /** Unidades despachadas en el periodo (salidas). */
        private BigDecimal unidades;
        /** Stock físico actual disponible en toda la red. */
        private BigDecimal stockActual;
        /** Veces que "rota" el stock en el periodo: unidades / stock actual. */
        private BigDecimal rotacion;
    }
}
