package com.pbl6.services.impl;

import com.pbl6.dtos.request.checkout.OrderItemRequest;
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
import java.util.stream.Collectors;

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
        VariantEntity variant = entityUtil.ensureExists(variantRepository.findById(request.getVariantId()));
        entityUtil.ensureActive(variant, false);

         int availableStock = inventoryRepository.findByVariantId(request.getVariantId()).stream()
                .mapToInt(InventoryEntity::getAvailableStock)
                .sum();
         if (availableStock<request.getQuantity()) {
             log.error("Thêm vào giỏ hàng vượt quá số lượng hàng có sẵn");
             throw new AppException(ErrorCode.STOCK_NOT_AVAILABLE);
         }

        CartItemEntity cartItem = cartItemRepository.findByUserIdAndVariantId(userId, request.getVariantId())
                .orElseGet(() -> {
                    UserEntity userRef = new UserEntity();
                    userRef.setId(userId);

                    CartItemEntity newItem = new CartItemEntity();
                    newItem.setUser(userRef);
                    newItem.setVariant(variant);
                    newItem.setQuantity(0);
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItemRepository.save(cartItem);
    }

    @Override
    public List<CartItemDto> getCartItems(Long userId) {
        UserEntity user = entityUtil.ensureExists(userRepository.findById(userId));

        // 1️⃣ Lấy danh sách sản phẩm trong giỏ
        List<CartItemEntity> cartItems = user.getCartItems();
        List<Long> productIds = cartItems.stream()
                .map(ci -> ci.getVariant().getProduct().getId())
                .distinct()
                .toList();

        // 2️⃣ Lấy toàn bộ promotion theo productId
        Map<Long, List<PromotionEntity>> promoMap =
                promotionService.getActivePromotionsGroupedByProduct(productIds);

        // 3️⃣ Tính giá từng item
        return cartItems.stream()
                .map(cartItem -> {
                    VariantEntity variant = cartItem.getVariant();
                    Long productId = variant.getProduct().getId();
                    List<PromotionEntity> promos = promoMap.getOrDefault(productId, List.of());

                    BigDecimal basePrice = variant.getPrice();
                    BigDecimal finalPrice = promotionService.calculateFinalPrice(basePrice, promos);


                    VariantDto variantDto = VariantDto.builder()
                            .id(variant.getId())
                            .sku(variant.getSku())
                            .thumbnail(variant.getThumbnail())
                            .price(variant.getPrice())
                            .specialPrice(finalPrice)
                            .attributes(
                                    variant.getVariantAttributeValues().stream()
                                            .map(vav -> VariantDto.AttributeDto.builder()
                                                    .code(vav.getAttribute().getCode())
                                                    .label(vav.getAttribute().getLabel())
                                                    .value(vav.getAttributeValue().getLabel())
                                                    .build())
                                            .toList()
                            )
                            .availableStock(inventoryRepository.getAvailableStockByVariantId(variant.getId()))
                            .build();

                    return CartItemDto.builder()
                            .id(cartItem.getId())
                            .item(variantDto)
                            .quantity(cartItem.getQuantity())
                            .build();
                })
                .toList();
    }

}
