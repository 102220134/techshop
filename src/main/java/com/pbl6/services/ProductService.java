package com.pbl6.services;

import com.pbl6.dtos.request.product.*;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.ProductEntity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    List<ProductDto> getFeaturedProducts(String slug, Integer size);
    List<ProductDto> getBestSellerProducts(String slug, Integer size);
    Page<ProductDto> filterProduct(String slugPath, ProductFilterRequest req);
    Page<ProductDto> searchProduct(ProductSearchRequest req);
    ProductDetailDto getProductDetail(String slug);
    ProductDetailDto getProductDetail(Long id);
    Page<ProductDto> filterProducts(AdminSearchProductRequest req);
    ProductDetailDto createProduct(CreateProductRequest request);
    ProductDetailDto updateProduct(Long id, UpdateProductRequest request);
}
