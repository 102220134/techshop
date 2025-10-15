package com.pbl6.repositories;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.request.product.ProductSearchRequest;
import com.pbl6.dtos.response.ProductDetailDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryCustom {
    Page<ProductProjection> filterProducts(Long categoryId, ProductFilterRequest req, boolean includeInactive, boolean isOnlyInStock);
    List<ProductProjection> findAllByCategoryId(Long categoryId, boolean includeInactive, boolean isOnlyInStock);
    Optional<ProductProjection> findBySlug(String slug, boolean includeInactive);
    public Page<ProductProjection> searchProductByKeyword(ProductSearchRequest req, boolean includeInactive, boolean isOnlyInStock);
}