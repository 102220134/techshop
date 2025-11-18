package com.pbl6.dtos.response.inventory.delivery;

import com.pbl6.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DeliveryDto {
    private Long id;
    private Long orderId;
    private String carrierName;
    private String trackingCode;
    private DeliveryStatus status;
    private BigDecimal codAmount;
}