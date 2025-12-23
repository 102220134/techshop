package com.pbl6.dtos.response.promotion;

import com.pbl6.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionTargetDto {
    private TargetType targetType; // "PRODUCT", "CATEGORY", "GLOBAL"
    private Long targetId;
}
