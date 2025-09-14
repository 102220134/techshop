package com.pbl6.repositories;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.ProductDto;
import org.springframework.data.domain.Page;

public interface ProductRepositoryCustom {
    Page<ProductDto> searchProducts(Long categoryId, ProductFilterRequest req,boolean includeInactive, boolean isOnlyInStock);
}