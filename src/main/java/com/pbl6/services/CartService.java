package com.pbl6.services;

import com.pbl6.dtos.request.product.AddToCartRequest;
import com.pbl6.dtos.response.cart.CartItemDto;

import java.util.List;

public interface CartService {
    void addToCart(Long userId,AddToCartRequest request);
    List<CartItemDto> getCartItems(Long userId);
    void deleteCartItem(Long userId,Long cartId);
}
