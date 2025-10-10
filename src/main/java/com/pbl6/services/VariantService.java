package com.pbl6.services;

import com.pbl6.dtos.response.VariantDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VariantService {
    List<VariantDto> getVariantsByProduct(Long productId);
    VariantDto getVariantById(Long id);
}
