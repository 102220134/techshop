package com.pbl6.services;

import com.pbl6.entities.PromotionEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PromotionService {
    //    PromotionDto findBestPromotion(Long productId, BigDecimal basePrice);
    Map<Long, List<PromotionEntity>> getActivePromotionsGroupedByProduct(List<Long> productIds);

//    Map<Long, List<PromotionEntity>> getActivePromotionsGroupedByProduct(Long productId);

    BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PromotionEntity> promotions);

}
