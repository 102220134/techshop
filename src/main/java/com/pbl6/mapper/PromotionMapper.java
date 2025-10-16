package com.pbl6.mapper;

import com.pbl6.dtos.response.promotion.PromotionDto;
import com.pbl6.dtos.response.promotion.PromotionTargetDto;
import com.pbl6.entities.PromotionEntity;
import com.pbl6.entities.PromotionTargetEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
public class PromotionMapper {
    public PromotionDto toDto(PromotionEntity entity) {
        if (entity == null) return null;

        return PromotionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .discountType(entity.getDiscountType().name())
                .discountValue(entity.getDiscountValue())
                .maxDiscountValue(entity.getMaxDiscountValue())
                .priority(entity.getPriority())
                .exclusive(entity.getExclusive())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .isActive(entity.isActive())
                .targets(toTargetDtos(entity.getTargets()))
                .build();
    }

    private List<PromotionTargetDto> toTargetDtos(List<PromotionTargetEntity> targets) {
        if (targets == null) return List.of();
        return targets.stream()
                .map(t -> PromotionTargetDto.builder()
                        .targetType(t.getTargetType().name())
                        .targetId(t.getTargetId())
                        .build())
                .toList();
    }
}
