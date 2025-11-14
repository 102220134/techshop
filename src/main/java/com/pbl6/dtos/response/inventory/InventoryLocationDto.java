package com.pbl6.dtos.response.inventory;

import com.pbl6.enums.InventoryLocationType;
import lombok.Data;

@Data
public class InventoryLocationDto {
    private long id;
    private InventoryLocationType type;
    private String name;
}
