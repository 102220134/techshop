package com.pbl6.controllers.customers;

import com.pbl6.dtos.request.product.AddToCartRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.cart.CartItemDto;
import com.pbl6.entities.UserEntity;
import com.pbl6.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/cart")
@RequiredArgsConstructor
@Tag(name = "Giỏ hàng")
public class CartController {

    private final CartService cartService;

    @PostMapping("add")
    @Operation(summary = "Thêm vào giỏ", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<String> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long userId = getCurrentUserId();
        cartService.addToCart(userId,request);
        ApiResponseDto<String> response = new ApiResponseDto<>();
        response.setData("Add to cart success");
        return response;
    }

    @GetMapping("my-cart")
    @Operation(summary = "Danh sách sản phẩm trong giỏ hàng  ", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<List<CartItemDto>> getCartItems() {
        Long userId = getCurrentUserId();
        return new ApiResponseDto<>(cartService.getCartItems(userId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = (UserEntity) authentication.getPrincipal();
        return user.getId();
    }
}
