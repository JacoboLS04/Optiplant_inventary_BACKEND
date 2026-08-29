package com.optiplant.inventario.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentasMensualesResponse {

    /** Número de meses considerados (incluye el mes en curso). */
    private int mesesConsiderados;
    /** Total de ventas de todo el periodo considerado. */
    private BigDecimal totalPeriodo;
    private List<MesVentas> meses;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MesVentas {
        private int anio;
        private int mes;
        /** Etiqueta legible, p. ej. "Ago 2026". */
        private String etiqueta;
        private BigDecimal total;
    }
}
