package com.pbl6.services;

import com.pbl6.dtos.request.product.AddToCartRequest;

public interface CartService {
    void addToCart(Long userId,AddToCartRequest request);
}
