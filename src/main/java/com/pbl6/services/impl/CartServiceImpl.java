package com.pbl6.services.impl;

import com.pbl6.dtos.request.product.AddToCartRequest;
import com.pbl6.entities.CartItemEntity;
import com.pbl6.entities.InventoryEntity;
import com.pbl6.entities.UserEntity;
import com.pbl6.entities.VariantEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.CartItemRepository;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.CartService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    private final EntityUtil entityUtil;
    private final CartItemRepository cartItemRepository;
    private final VariantRepository variantRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

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
}
