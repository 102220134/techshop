package com.pbl6.services;

import com.pbl6.dtos.response.PromotionDto;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {
    PromotionDto findBestPromotion(Long productId, BigDecimal basePrice);
}
