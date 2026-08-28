package com.optiplant.inventario.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InventorySummaryResponse {

    private Summary summary;
    private List<CategoryDistribution> distribution;

    @Data
    @Builder
    public static class Summary {
        private BigDecimal totalValue;
        private BigDecimal totalUnits;
        private long skuCount;
        private long branchCount;
        private BigDecimal inflowValue30d;
        private BigDecimal outflowValue30d;
        private BigDecimal changePercent;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class CategoryDistribution {
        private String category;
        private BigDecimal units;
        private BigDecimal value;
    }
}
