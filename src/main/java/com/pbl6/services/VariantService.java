package com.pbl6.services;

import com.pbl6.dtos.response.product.VariantDto;

import java.util.List;

public interface VariantService {
    List<VariantDto> getVariantsByProduct(Long productId);
}
