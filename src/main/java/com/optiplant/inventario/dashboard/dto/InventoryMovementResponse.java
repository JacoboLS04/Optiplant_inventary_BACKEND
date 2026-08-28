package com.optiplant.inventario.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class InventoryMovementResponse {

    private Long id;
    private String product;
    private String sku;
    private String type;
    private String branch;
    private java.math.BigDecimal quantity;
    private LocalDateTime date;
}
