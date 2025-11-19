package com.pbl6.dtos.request.inventory.delivery;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateTrackingRequest {
    @NotEmpty
    private String carrierName; // VD: "GHTK", "ViettelPost"
    @NotEmpty
    private String trackingCode; // VD: "GHTK123456"
    private BigDecimal shippingFee;
}