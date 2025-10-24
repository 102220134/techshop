package com.pbl6.services.impl;

import com.pbl6.dtos.request.product.AddToCartRequest;
import com.pbl6.dtos.response.cart.CartItemDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.CartItemRepository;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.CartService;
import com.pbl6.services.PromotionService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final EntityUtil entityUtil;
    private final CartItemRepository cartItemRepository;
    private final VariantRepository variantRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final PromotionService promotionService;

    @Override
    public void addToCart(Long userId, AddToCartRequest request) {
        VariantEntity variant = entityUtil.ensureExists(
                variantRepository.findById(request.getVariantId()));
        entityUtil.ensureActive(variant, false);

        if (variant.getAvailableStock() < request.getQuantity()) {
            log.error("Thêm vào giỏ hàng vượt quá số lượng hàng có sẵn");
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"stock not available");
        }

        CartItemEntity cartItem = cartItemRepository
                .findByUserIdAndVariantId(userId, request.getVariantId())
                .orElseGet(() -> {
                    UserEntity userRef = new UserEntity();
                    userRef.setId(userId);

                    CartItemEntity newItem = new CartItemEntity();
                    newItem.setUser(userRef);
                    newItem.setVariant(variant);
                    newItem.setQuantity(0);
                    return newItem;
                });


        int newQuantity = request.getQuantity();

        // ✅ Nếu số lượng <= 0 thì xóa khỏi giỏ
        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Đã xoá sản phẩm {} khỏi giỏ hàng của user {}", variant.getId(), userId);
            return;
        }

        // ✅ Ngược lại thì cập nhật
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }


    @Override
    public List<CartItemDto> getCartItems(Long userId) {
        UserEntity user = entityUtil.ensureExists(userRepository.findById(userId));
        List<CartItemEntity> cartItems = user.getCartItems();
        List<Long> productIds = cartItems.stream()
                .map(ci -> ci.getVariant().getProduct().getId())
                .distinct()
                .toList();
        return cartItems.stream()
                .map(cartItem -> {
                    VariantEntity variant = cartItem.getVariant();
                    Long productId = variant.getProduct().getId();
                    VariantDto variantDto = VariantDto.builder()
                            .id(variant.getId())
                            .sku(variant.getSku())
                            .thumbnail(variant.getThumbnail())
                            .price(variant.getPrice())
                            .specialPrice(variant.getDiscountedPrice())
                            .attributes(
                                    variant.getVariantAttributeValues().stream()
                                            .map(vav -> VariantDto.AttributeDto.builder()
                                                    .code(vav.getAttribute().getCode())
                                                    .label(vav.getAttribute().getLabel())
                                                    .value(vav.getAttributeValue().getLabel())
                                                    .build())
                                            .toList()
                            )
                            .availableStock(variant.getAvailableStock())
                            .build();

                    return CartItemDto.builder()
                            .id(cartItem.getId())
                            .item(variantDto)
                            .quantity(cartItem.getQuantity())
                            .build();
                })
                .toList();
    }

    @Override
    public void deleteCartItem(Long userId, Long cartId) {
        CartItemEntity cartItem = entityUtil.ensureExists(cartItemRepository.findById(cartId),"cart item not found");
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        cartItemRepository.delete(cartItem);
    }

}
