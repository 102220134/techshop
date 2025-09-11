package com.pbl6.services;

import com.pbl6.dtos.request.ProductRequest;
import com.pbl6.dtos.response.ProductListItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductListItemDto> getProductsByCategory(ProductRequest req, Pageable pageable);
}
