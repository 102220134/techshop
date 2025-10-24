package com.pbl6.services;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.product.ProductDto;

public interface LikeProductService {
    public void likeProduct(Long userId, Long productId);
    public void unlikeProduct(Long userId, Long productId);
    PageDto<ProductDto> getLikedByUser(Long userId, int page, int size);
}
