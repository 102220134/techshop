package com.pbl6.services;

import java.math.BigDecimal;

public interface VariantEffectivePriceService {
    BigDecimal getDiscountedPrice(Long variantId);
}
