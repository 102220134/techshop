package com.pbl6.services;

import com.pbl6.dtos.request.product.CreateVariantRequest;
import com.pbl6.dtos.request.product.UpdateVariantRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.product.VariantDto;
import jakarta.validation.Valid;

import java.util.List;

public interface VariantService {
    VariantDto getVariantById(long variantId);

    VariantDto createVariant(@Valid CreateVariantRequest request);

    VariantDto editVariant(long variantId, @Valid UpdateVariantRequest request);

    PageDto<VariantDto> searchVariant(int page, int size, String keyword);
}
