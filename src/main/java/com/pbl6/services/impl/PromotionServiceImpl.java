package com.pbl6.services.impl;

import com.pbl6.dtos.response.PromotionDto;
import com.pbl6.enums.PromotionType;
import com.pbl6.repositories.PromotionRepository;
import com.pbl6.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;

    @Override
    public PromotionDto findBestPromotion(Long productId, BigDecimal basePrice) {
        return promotionRepository.findActiveByProductId(productId).stream()
                .map(promo -> PromotionType.fromCode(promo.getDiscountType())
                        .map(type -> {
                            BigDecimal candidate = switch (type) {
                                case PERCENTAGE -> basePrice.subtract(
                                        basePrice.multiply(promo.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                                );
                                case AMOUNT -> basePrice.subtract(promo.getDiscountValue());
                            };
                            return Map.entry(promo, candidate);
                        })
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .min(Map.Entry.comparingByValue(BigDecimal::compareTo))
                .map(entry -> PromotionDto.builder()
                        .id(entry.getKey().getId())
                        .name(entry.getKey().getName())
                        .description(entry.getKey().getDescription())
                        .discountType(entry.getKey().getDiscountType())
                        .discountValue(entry.getKey().getDiscountValue())
                        .startDate(entry.getKey().getStartDate().toString())
                        .endDate(entry.getKey().getEndDate().toString())
                        .status(entry.getKey().getStatus())
                        .scope(entry.getKey().getScope().name())
                        .specialPrice(entry.getValue())
                        .build())
                .orElse(null);
    }


}
