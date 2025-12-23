package com.pbl6.dtos.request.promotion;

import com.pbl6.dtos.response.promotion.PromotionTargetDto;
import com.pbl6.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromotionRequest {
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;
    private Integer priority;
    private Boolean exclusive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private List<PromotionTargetDto> targets;
}
