package com.optiplant.inventario.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BranchNetworkResponse {

    private List<BranchNode> nodes;
    private List<BranchLink> links;
    private List<StockAlert> alerts;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class BranchNode {
        private Long id;
        private String name;
        private String kind;
        private String status;
        private BigDecimal units;
        private long skuCount;
        private long lowStockCount;
        private BigDecimal x;
        private BigDecimal y;
    }

    @Data
    @Builder
    public static class BranchLink {
        private Long from;
        private Long to;
        private String status;
    }

    @Data
    @Builder
    public static class StockAlert {
        private Long id;
        private String product;
        private Long branchId;
        private String branchName;
        private BigDecimal currentUnits;
        private BigDecimal minUnits;
        private String severity;
    }
}
