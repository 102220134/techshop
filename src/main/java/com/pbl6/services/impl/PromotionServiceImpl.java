package com.pbl6.services.impl;

import com.pbl6.entities.PromotionEntity;
import com.pbl6.entities.PromotionTargetEntity;
import com.pbl6.enums.TargetType;
import com.pbl6.mapper.PromotionMapper;
import com.pbl6.repositories.PromotionRepository;
import com.pbl6.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    @Override
    public Map<Long, List<PromotionEntity>> getActivePromotionsGroupedByProduct(List<Long> productIds) {
        List<PromotionEntity> entities = promotionRepository.findActivePromotionsForProducts(productIds);

        // Map productId -> promotions áp dụng
        Map<Long, List<PromotionEntity>> result = new HashMap<>();

        for (PromotionEntity promo : entities) {
            for (PromotionTargetEntity target : promo.getTargets()) {

                // Nếu là GLOBAL → áp dụng cho tất cả sản phẩm
                if (target.getTargetType() == TargetType.GLOBAL) {
                    for (Long pid : productIds) {
                        result.computeIfAbsent(pid, k -> new ArrayList<>())
                                .add(promo);
                    }
                }

                // Nếu target là PRODUCT
                else if (target.getTargetType() == TargetType.PRODUCT
                         && productIds.contains(target.getTargetId())) {
                    result.computeIfAbsent(target.getTargetId(), k -> new ArrayList<>())
                            .add(promo);
                }
            }
        }

        return result;
    }

    @Override
    public BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PromotionEntity> promotions) {
        if (basePrice == null || promotions == null || promotions.isEmpty()) return basePrice;

        List<PromotionEntity> sortedPromos = promotions.stream()
                .sorted(Comparator.comparingInt(p -> Optional.ofNullable(p.getPriority()).orElse(0)))
                .toList();

        BigDecimal currentPrice = basePrice;

        for (PromotionEntity promo : sortedPromos) {
            currentPrice = applyPromotion(currentPrice, promo);
            if (Boolean.TRUE.equals(promo.getExclusive())) break;
        }

        return currentPrice.max(BigDecimal.ZERO);
    }

    private BigDecimal applyPromotion(BigDecimal basePrice, PromotionEntity promo) {
        if (promo == null || basePrice == null) return basePrice;

        BigDecimal discount = BigDecimal.ZERO;

        switch (promo.getDiscountType()) {
            case PERCENTAGE -> discount = basePrice
                    .multiply(promo.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            case AMOUNT-> discount = promo.getDiscountValue();
        }

        if (promo.getMaxDiscountValue() != null &&
            discount.compareTo(promo.getMaxDiscountValue()) > 0) {
            discount = promo.getMaxDiscountValue();
        }

        return basePrice.subtract(discount).max(BigDecimal.ZERO);
    }



}
