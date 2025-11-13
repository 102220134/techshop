package com.pbl6.dtos.response.inventory.GR;

import com.pbl6.dtos.response.inventory.SupplierDto;
import com.pbl6.enums.GRStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GRDto {
    private long id;
    private SupplierDto supplier;
    private GRStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
