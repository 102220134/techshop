package com.pbl6.dtos.response.promotion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionTargetDto {
    private String targetType; // "PRODUCT", "CATEGORY", "GLOBAL"
    private Long targetId;
}
