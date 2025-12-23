package com.pbl6.services;

import com.pbl6.dtos.request.promotion.PromotionRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.promotion.PromotionResponse;
import com.pbl6.entities.PromotionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PromotionService {
    //    PromotionDto findBestPromotion(Long productId, BigDecimal basePrice);
    Map<Long, List<PromotionEntity>> getActivePromotionsGroupedByProduct(List<Long> productIds);

//    Map<Long, List<PromotionEntity>> getActivePromotionsGroupedByProduct(Long productId);

    BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PromotionEntity> promotions);

    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse updatePromotion(Long id, PromotionRequest request);
    void deletePromotion(Long id);
    PromotionResponse getById(Long id);
    PageDto<PromotionResponse> getAll(Pageable pageable);

}
