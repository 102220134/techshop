package com.pbl6.dtos.response.inventory.movement;

import com.pbl6.dtos.response.inventory.InventoryLocationDto;
import com.pbl6.dtos.response.product.VariantDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockMovementDto {
    private Long id;
    private Integer quantityDelta;
    private String reason;
    private String refType;
    private Long refId;
    private LocalDateTime createdAt;

    // Nested DTOs
    private VariantDto variant;
    private InventoryLocationDto inventoryLocation;
}
