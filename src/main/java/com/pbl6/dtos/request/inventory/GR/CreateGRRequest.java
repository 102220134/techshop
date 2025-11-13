package com.pbl6.dtos.request.inventory.GR;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateGRRequest {
    private Long supplierId;
    private Long locationId;
    private String note;

    private List<CreateGRItemRequest> items;

    @Data
    static public class CreateGRItemRequest {
        private Long variantId;
        private BigDecimal unitCost;
        @NotEmpty
        @NotNull
        private List<String> serials;
    }
}


