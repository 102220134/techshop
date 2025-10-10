package com.pbl6.services;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.ProductDetailDto;
import com.pbl6.dtos.response.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductDto> getFeaturedProducts(String slug, int size);
    List<ProductDto> getBestSellerProducts(String slug, int size);
    Page<ProductDto> searchProduct(String slugPath, ProductFilterRequest req, boolean includeInactive);
    ProductDetailDto getProductDetail(String slug,  boolean includeInactive);
}
