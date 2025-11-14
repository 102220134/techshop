package com.pbl6.dtos.request.inventory.transfer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateTransferRequest {
    private Long sourceLocationId;
    private Long targetLocationId;
    private List<Item> items;

    @Data
    public static class Item {
        private Long variantId;
        @NotNull
        @NotEmpty
        private List<String> serials; // nếu có serial
    }
}

