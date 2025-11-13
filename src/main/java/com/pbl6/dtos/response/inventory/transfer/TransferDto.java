package com.pbl6.dtos.response.inventory.transfer;

import com.pbl6.dtos.response.inventory.InventoryLocationDto;
import com.pbl6.dtos.response.inventory.SupplierDto;
import com.pbl6.enums.GRStatus;
import com.pbl6.enums.TransferStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransferDto {
    private long id;
    private InventoryLocationDto source;
    private InventoryLocationDto destination;
    private TransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}