package com.pbl6.services.impl;

import com.pbl6.dtos.request.checkout.OrderItemRequest;
import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.dtos.response.PromotionDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.response.order.OrderItemDto;
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
import java.util.ArrayList;
import java.util.List;
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

    @Override
    @Transactional
    public OrderEntity createOrder(OrderRequest req) {
        StoreEntity store = null;
        if (req.getStoreId() != null) {
            store = new StoreEntity();
            store.setId(req.getStoreId());
        }

        // Dùng mapper tạo entity base
        OrderEntity order = orderMapper.toEntity(UserEntity.builder().id(req.getUserId()).build(), req, store);

        // Tính toán tổng tiền và item
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : req.getItems()) {
            VariantEntity variant = entityUtil.ensureExists(variantRepository.findById(itemReq.getVariantId()));
            entityUtil.ensureActive(variant,false);
            PromotionDto promotion = promotionService.findBestPromotion(
                    variant.getProduct().getId(), variant.getPrice());

            BigDecimal specialPrice = promotion == null ? variant.getPrice() : promotion.getSpecialPrice();
            BigDecimal itemPrice = specialPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemPrice);

            orderItems.add(OrderItemEntity.builder()
                            .order(order)
                    .variant(variant)
                    .productName(variant.getProduct().getName())
                    .sku(variant.getSku())
                    .price(variant.getPrice())
                    .quantity(itemReq.getQuantity())
                    .discountAmount(variant.getPrice().subtract(specialPrice))
                    .promotion(promotion == null ? null :
                            PromotionEntity.builder().id(promotion.getId()).build())
                    .build());
        }

        order.setTotalAmount(totalAmount);

        // Lưu order + items
        order = orderRepository.save(order);
        order.setOrderItems(orderItems);
        orderItemRepository.saveAll(orderItems);

        return order;
    }

    @Override
    public List<OrderDto> getOrderByUser(Long userID) {
        return orderRepository.findByUserId(userID).stream()
                .map(orderMapper::toOrderDto)
                .toList();
    }

    @Override
    public void markOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }
}

