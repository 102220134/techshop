package com.pbl6.services.impl;

import com.pbl6.dtos.request.checkout.OrderItemRequest;
import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.*;
import com.pbl6.enums.OrderStatus;
import com.pbl6.mapper.OrderMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.OrderService;
import com.pbl6.services.PromotionService;
import com.pbl6.utils.EntityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderServiceImpl implements OrderService {

    final OrderRepository orderRepository;
    final OrderItemRepository orderItemRepository;
    final VariantRepository variantRepository;
    final StoreRepository storeRepository;
    final PromotionService promotionService;
    final EntityUtil entityUtil;
    final OrderMapper orderMapper;

    // ---------------------- CREATE ORDER ----------------------

    @Override
    @Transactional
    public OrderEntity createOrder(OrderRequest req) {
        StoreEntity store = null;
        if (req.getStoreId() != null) {
            store = new StoreEntity();
            store.setId(req.getStoreId());
        }

        // 1️⃣ Tạo Order base
        OrderEntity order = orderMapper.toEntity(
                UserEntity.builder().id(req.getUserId()).build(), req, store
        );

        // 2️⃣ Lấy toàn bộ variantId → productId map
        Map<Long, VariantEntity> variantMap = variantRepository.findAllById(
                req.getItems().stream().map(OrderItemRequest::getVariantId).toList()
        ).stream().collect(Collectors.toMap(VariantEntity::getId, v -> v));

        List<Long> productIds = variantMap.values().stream()
                .map(v -> v.getProduct().getId())
                .distinct()
                .toList();

        // 3️⃣ Lấy toàn bộ promotions áp dụng theo product
        Map<Long, List<PromotionEntity>> promotionMap =
                promotionService.getActivePromotionsGroupedByProduct(productIds);

        // 4️⃣ Tính toán từng item
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : req.getItems()) {
            VariantEntity variant = variantMap.get(itemReq.getVariantId());
            entityUtil.ensureActive(variant, false);

            List<PromotionEntity> promos = promotionMap.getOrDefault(
                    variant.getProduct().getId(), List.of()
            );

            BigDecimal basePrice = variant.getPrice();
            BigDecimal finalPrice = promotionService.calculateFinalPrice(basePrice, promos);

            BigDecimal lineTotal = finalPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            orderItems.add(OrderItemEntity.builder()
                    .order(order)
                    .variant(variant)
                    .productName(variant.getProduct().getName())
                    .sku(variant.getSku())
                    .price(basePrice)
                    .quantity(itemReq.getQuantity())
                    .discountAmount(basePrice.subtract(finalPrice))
                    .promotions(promos)
                    .build());
        }

        // 5️⃣ Cập nhật tổng tiền và lưu
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);
        order.setOrderItems(orderItems);
        orderItemRepository.saveAll(orderItems);

        return order;
    }

    // ---------------------- GET USER ORDERS ----------------------

    @Override
    public List<OrderDto> getOrderByUser(Long userID) {
        return orderRepository.findByUserId(userID).stream()
                .map(orderMapper::toOrderDto)
                .toList();
    }

    // ---------------------- MARK STATUS ----------------------

    @Override
    public void markOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }

    // ---------------------- PRICE CALCULATION ----------------------
//
//    private BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PromotionEntity> promotions) {
//        if (basePrice == null || promotions == null || promotions.isEmpty()) return basePrice;
//
//        List<PromotionEntity> sortedPromos = promotions.stream()
//                .sorted(Comparator.comparingInt(p -> Optional.ofNullable(p.getPriority()).orElse(0)))
//                .toList();
//
//        BigDecimal currentPrice = basePrice;
//
//        for (PromotionEntity promo : sortedPromos) {
//            currentPrice = applyPromotion(currentPrice, promo);
//            if (Boolean.TRUE.equals(promo.getExclusive())) break;
//        }
//
//        return currentPrice.max(BigDecimal.ZERO);
//    }
//
//    private BigDecimal applyPromotion(BigDecimal basePrice, PromotionEntity promo) {
//        if (promo == null || basePrice == null) return basePrice;
//
//        BigDecimal discount = BigDecimal.ZERO;
//
//        switch (promo.getDiscountType()) {
//            case PERCENTAGE -> discount = basePrice
//                    .multiply(promo.getDiscountValue())
//                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
//            case AMOUNT-> discount = promo.getDiscountValue();
//        }
//
//        if (promo.getMaxDiscountValue() != null &&
//            discount.compareTo(promo.getMaxDiscountValue()) > 0) {
//            discount = promo.getMaxDiscountValue();
//        }
//
//        return basePrice.subtract(discount).max(BigDecimal.ZERO);
//    }
}
