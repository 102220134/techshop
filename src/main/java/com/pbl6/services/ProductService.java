package com.pbl6.services;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.request.product.ProductSearchRequest;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductDto> getFeaturedProducts(String slug, int size);
    List<ProductDto> getBestSellerProducts(String slug, int size);
    Page<ProductDto> filterProduct(String slugPath, ProductFilterRequest req, boolean includeInactive);
    Page<ProductDto> searchProduct(ProductSearchRequest req, boolean includeInactive);
    ProductDetailDto getProductDetail(String slug,  boolean includeInactive);
}
