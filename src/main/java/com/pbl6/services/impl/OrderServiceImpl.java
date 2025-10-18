package com.pbl6.services.impl;

import com.pbl6.dtos.request.checkout.OrderItemRequest;
import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.entities.*;
import com.pbl6.enums.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.OrderMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.OrderService;
import com.pbl6.services.PromotionService;
import com.pbl6.utils.EntityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class OrderServiceImpl implements OrderService {

    final OrderRepository orderRepository;
    final OrderItemRepository orderItemRepository;
    final VariantRepository variantRepository;
    final StoreRepository storeRepository;
    final PromotionService promotionService;
    final EntityUtil entityUtil;
    final OrderMapper orderMapper;
    private final PaymentRepository paymentRepository;
    private final ProductSerialRepository productSerialRepository;
    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

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

    @Override
    @Transactional
    public void cancelOrderPaymentTimeout() {
        Duration paymentTimeout = Duration.ofMinutes(5);
        LocalDateTime timeoutThreshold = LocalDateTime.now().minus(paymentTimeout);

        List<OrderEntity> ordersToCancel = orderRepository.findByStatusAndPaymentMethodAndCreatedAtBefore(
                OrderStatus.PENDING,
                PaymentMethod.BANK,
                timeoutThreshold
        );

        if (ordersToCancel.isEmpty()) {
            log.info("No PENDING orders with BANK payment method found that have exceeded the payment timeout.");
            return;
        }

        log.info("Found {} PENDING orders with BANK payment method exceeding payment timeout. Preparing to cancel them.", ordersToCancel.size());

        List<PaymentEntity> paymentsToUpdate = new ArrayList<>();
        List<InventoryEntity> inventoriesToUpdate = new ArrayList<>();
        List<ProductSerialEntity> productSerialsToUpdate = new ArrayList<>();
        List<ReservationEntity> reservationsToDelete = new ArrayList<>();

        for (OrderEntity order : ordersToCancel) {
            log.debug("Processing order ID {} for cancellation.", order.getId());

            // Update order status to CANCELLED
            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());

            // Mark payments as CANCELED
            order.getPayments().stream()
                    .peek(payment -> payment.setStatus(PaymentStatus.CANCELED))
                    .forEach(paymentsToUpdate::add);

            // Release reserved stock, update product serials, and mark reservations for deletion
            order.getReservations().forEach(reservation -> {
                int quantity = reservation.getQuantity();
                OrderItemEntity orderItem = reservation.getOrderItem();
                InventoryLocationEntity location = reservation.getLocation();

                InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(location.getId(), orderItem.getVariant().getId())
                        .orElseThrow(() -> {
                            log.error("Inventory not found for location ID {} and variant ID {} during order cancellation (order ID {}). This indicates a data inconsistency.",
                                    location.getId(), orderItem.getVariant().getId(), order.getId());
                            return new AppException(ErrorCode.DATA_NOT_FOUND);
                        });

                // Defensive check: reserved stock should be sufficient to unreserve
                if (inventory.getReservedStock() < quantity) {
                    log.error("Data inconsistency: Attempted to unreserve {} units, but only {} units were reserved for inventory ID {} (order ID {}).",
                            quantity, inventory.getReservedStock(), inventory.getId(), order.getId());
                    throw new AppException(ErrorCode.OVERSELL_PRODUCT_SERIAL);
                }

                inventory.unReservedStock(quantity);
                inventoriesToUpdate.add(inventory);

                reservation.getProductSerials().stream()
                        .peek(productSerial -> {
                            productSerial.setReservation(null);
                            productSerial.setStatus(ProductSerialStatus.IN_STOCK);
                        })
                        .forEach(productSerialsToUpdate::add);

                reservationsToDelete.add(reservation);
            });
        }

        // Perform all batch updates and deletions
        orderRepository.saveAll(ordersToCancel);
        paymentRepository.saveAll(paymentsToUpdate);
        inventoryRepository.saveAll(inventoriesToUpdate);
        productSerialRepository.saveAll(productSerialsToUpdate);
        reservationRepository.deleteAll(reservationsToDelete);

        log.info("Successfully cancelled {} timed out orders.", ordersToCancel.size());
    }

//    @Override
//    public void cancelOrder(Long orderId) {
//        // Ghi log yêu cầu hủy đơn hàng
//        log.info("Attempting to cancel order with ID: {}", orderId);
//
//        // Tìm đơn hàng theo ID, nếu không tìm thấy sẽ ném ngoại lệ
//        // Find order by ID, throw exception if not found
//        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), ErrorCode.ORDER_NOT_FOUND);
//
//        // Kiểm tra trạng thái hiện tại của đơn hàng
//        // Check current status of the order
//        if (order.getStatus() == OrderStatus.CANCELLED) {
//            log.warn("Order with ID {} is already CANCELLED. No action taken.", orderId);
//            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
//        }
//        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.COMPLETED) {
//            log.warn("Order with ID {} cannot be cancelled as it is already {}.", orderId, order.getStatus());
//            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
//        }
//
//        // Cập nhật trạng thái đơn hàng thành CANCELLED
//        // Update order status to CANCELLED
//        order.setStatus(OrderStatus.CANCELLED);
//        order.setUpdatedAt(LocalDateTime.now()); // Cập nhật thời gian cập nhật
//        orderRepository.save(order);
//
//        // Ghi log đơn hàng đã được hủy thành công
//        log.info("Order with ID {} has been successfully cancelled.", orderId);
//    }

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
