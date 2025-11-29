package com.pbl6.services.impl;

import com.pbl6.dtos.request.order.CreateOrderRequest;
import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.order.OrderItemRequest;
import com.pbl6.dtos.request.order.SearchOrderRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDetailDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.response.order.UserOrderDetailDto;
import com.pbl6.entities.*;
import com.pbl6.enums.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.OrderMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.OrderService;
import com.pbl6.services.PromotionService;
import com.pbl6.specifications.OrderSpecification;
import com.pbl6.utils.AuthenticationUtil;
import com.pbl6.utils.EntityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    VariantRepository variantRepository;
    StoreRepository storeRepository;
    PromotionService promotionService;
    EntityUtil entityUtil;
    OrderMapper orderMapper;
    PaymentRepository paymentRepository;
    ProductSerialRepository productSerialRepository;
    InventoryRepository inventoryRepository;
    ReservationRepository reservationRepository;
    AuthenticationUtil authenticationUtil;
    WareHouseRepository wareHouseRepository;
    UserRepository userRepository;
    DebtRepository debtRepository;

    // ========================================================================
    // CREATE ORDER
    // ========================================================================
    @Override

    @Transactional

    public OrderEntity createOrder(CreateOrderRequest req) {
        UserEntity buyer = userRepository.findById(req.getUserId()).get();
        // Chuẩn bị store (nếu có)
        StoreEntity store = null;
        if (req.getStoreId() != null) {
            store = storeRepository.findById(req.getStoreId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Store not found"));
        }
        // Snapshot địa chỉ nhận hàng
        AddressSnapshot snapshot = AddressSnapshot.builder()
                .name(req.getFullName())
                .phone(req.getPhone())
                .line(req.getLine())
                .ward(req.getWard())
                .district(req.getDistrict())
                .province(req.getProvince())
                .build();
        // Map variants
        Map<Long, VariantEntity> variantMap = variantRepository.findAllById(
                req.getItems().stream().map(OrderItemRequest::getVariantId).toList()
        ).stream().collect(Collectors.toMap(VariantEntity::getId, v -> v));
        // Lấy danh sách productId để áp khuyến mãi
        List<Long> productIds = variantMap.values().stream()
                .map(v -> v.getProduct().getId())
                .distinct()
                .toList();
        Map<Long, List<PromotionEntity>> promotionMap =
                promotionService.getActivePromotionsGroupedByProduct(productIds);
        // ----------------- Tính toán tổng tiền -----------------
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (OrderItemRequest itemReq : req.getItems()) {
            VariantEntity variant = variantMap.get(itemReq.getVariantId());
            entityUtil.ensureActive(variant, false);
            List<PromotionEntity> promos = promotionMap.getOrDefault(
                    variant.getProduct().getId(), List.of()
            );
            BigDecimal basePrice = variant.getPrice();
            BigDecimal discountedPrice = variant.getDiscountedPrice();
            BigDecimal itemSubtotal = discountedPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderSubtotal = orderSubtotal.add(itemSubtotal);
            orderItems.add(OrderItemEntity.builder()
                    .order(null) // gán sau khi order được save
                    .variant(variant)
                    .productName(variant.getProduct().getName())
                    .sku(variant.getSku())
                    .price(basePrice)
                    .quantity(itemReq.getQuantity())
                    .discountAmount(basePrice.subtract(discountedPrice))
                    .finalPrice(discountedPrice)
                    .promotions(promos)
                    .subtotal(itemSubtotal)
                    .build());
        }


        // ----------------- Khởi tạo entity -----------------

        OrderEntity order = OrderEntity.builder()
                .user(buyer)
                .store(store)
                .status(OrderStatus.PENDING)
                .paymentMethod(req.getPaymentMethod())
                .receiveMethod(req.getReceiveMethod())
                .snapshot(snapshot)
                .note(req.getNote())
                .isOnline(req.getIsOnline())
                .subtotal(orderSubtotal)
                .voucherDiscount(BigDecimal.ZERO)
                .totalAmount(orderSubtotal) // có thể trừ voucher sau này
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(orderSubtotal)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // Lưu đơn hàng
        order = orderRepository.save(order);
        // Gán lại order cho từng item và lưu
        for (OrderItemEntity item : orderItems) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(orderItems);
        order.setOrderItems(orderItems);
        return order;
    }


    @Override
    @Transactional
    public OrderEntity createOrderManual(CreateOrderRequest req) {
        UserEntity currentUser = authenticationUtil.getCurrentUser();
        // --- Kiểm tra và lấy store ---
        StoreEntity store = entityUtil.ensureExists(
                storeRepository.findById(req.getStoreId()),
                "Store not found"
        );
        // --- Kiểm tra và lấy khách hàng ---
        UserEntity customer = entityUtil.ensureExists(
                userRepository.findById(req.getUserId()),
                "Customer not found"
        );
        UserEntity sale; // nhân viên phụ trách đơn hàng
        if (currentUser.isAdmin()) {
            // ✅ ADMIN có thể chọn bất kỳ sale nào
            sale = null;
            if (req.getSaleId() != null) {
                sale = entityUtil.ensureExists(
                        userRepository.findById(req.getSaleId()),
                        "Sale not found"
                );
            }
        } else {
            // ✅ NHÂN VIÊN bán hàng
            // Kiểm tra cửa hàng của họ có trùng storeId hay không
            if (!currentUser.getStoreId().equals(req.getStoreId())) {
                throw new AppException(ErrorCode.FORBIDDEN, "You cannot create order for another store");
            }
            // Kiểm tra saleId có trùng với chính họ không
            if (req.getSaleId() != null && !req.getSaleId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN, "Sale ID does not match current user");
            }
            sale = currentUser;
        }
        req.setIsOnline(false);
        OrderEntity order = createOrder(req);
        return order;
    }

//    @Override
//    @Transactional
//    public OrderEntity createOrder(CreateOrderRequest req) {
//        UserEntity buyer = userRepository.findById(req.getUserId())
//                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));
//
//        StoreEntity store = null;
//        if (req.getStoreId() != null) {
//            store = storeRepository.findById(req.getStoreId())
//                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Store not found"));
//        }
//
//        return processOrderCreation(req, buyer, store);
//    }
//
//    @Override
//    @Transactional
//    public OrderEntity createOrderManual(CreateOrderRequest req) {
//        UserEntity currentUser = authenticationUtil.getCurrentUser();
//
//        // 1. Validate Store & Customer
//        StoreEntity store = entityUtil.ensureExists(storeRepository.findById(req.getStoreId()), "Store not found");
//        UserEntity customer = entityUtil.ensureExists(userRepository.findById(req.getUserId()), "Customer not found");
//
//        // 2. Validate Permissions
//        if (!currentUser.isAdmin()) {
//            if (!Objects.equals(currentUser.getStoreId(), req.getStoreId())) {
//                throw new AppException(ErrorCode.FORBIDDEN, "Bạn không thể tạo đơn cho cửa hàng khác");
//            }
//            if (req.getSaleId() != null && !req.getSaleId().equals(currentUser.getId())) {
//                throw new AppException(ErrorCode.FORBIDDEN, "Sale ID không khớp với người dùng hiện tại");
//            }
//        }
//
//        req.setIsOnline(false); // Đơn tại quầy
//        return processOrderCreation(req, customer, store);
//    }

    private OrderEntity processOrderCreation(CreateOrderRequest req, UserEntity buyer, StoreEntity store) {
        // ... (Logic tạo AddressSnapshot, VariantMap, Promotion giữ nguyên để tiết kiệm không gian)
        // Copy lại logic map variant và tính toán từ code cũ của bạn vào đây
        // ...

        // Demo rút gọn logic mapping:
        AddressSnapshot snapshot = AddressSnapshot.builder()
                .name(req.getFullName()).phone(req.getPhone()).line(req.getLine())
                .ward(req.getWard()).district(req.getDistrict()).province(req.getProvince()).build();

        List<Long> variantIds = req.getItems().stream().map(OrderItemRequest::getVariantId).toList();
        Map<Long, VariantEntity> variantMap = variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(VariantEntity::getId, Function.identity()));

        if (variantMap.size() != variantIds.size())
            throw new AppException(ErrorCode.NOT_FOUND, "Product variant not found");

        List<Long> productIds = variantMap.values().stream().map(v -> v.getProduct().getId()).distinct().toList();
        Map<Long, List<PromotionEntity>> promotionMap = promotionService.getActivePromotionsGroupedByProduct(productIds);

        BigDecimal orderSubtotal = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : req.getItems()) {
            VariantEntity variant = variantMap.get(itemReq.getVariantId());
            entityUtil.ensureActive(variant, false);
            List<PromotionEntity> promos = promotionMap.getOrDefault(variant.getProduct().getId(), List.of());

            BigDecimal basePrice = variant.getPrice();
            BigDecimal discountedPrice = variant.getDiscountedPrice();
            BigDecimal itemSubtotal = discountedPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            orderSubtotal = orderSubtotal.add(itemSubtotal);

            orderItems.add(OrderItemEntity.builder()
                    .variant(variant).productName(variant.getProduct().getName()).sku(variant.getSku())
                    .price(basePrice).quantity(itemReq.getQuantity())
                    .discountAmount(basePrice.subtract(discountedPrice)).finalPrice(discountedPrice)
                    .promotions(promos).subtotal(itemSubtotal).build());
        }

        OrderEntity order = OrderEntity.builder()
                .user(buyer).store(store).status(OrderStatus.PENDING)
                .paymentMethod(req.getPaymentMethod()).receiveMethod(req.getReceiveMethod())
                .snapshot(snapshot).note(req.getNote()).isOnline(req.getIsOnline())
                .subtotal(orderSubtotal).totalAmount(orderSubtotal).paidAmount(BigDecimal.ZERO)
                .remainingAmount(orderSubtotal).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);
        for (OrderItemEntity item : orderItems) item.setOrder(order);
        orderItemRepository.saveAll(orderItems);
        order.setOrderItems(orderItems);

        return order;
    }

    // ========================================================================
    // CANCEL ORDER (MANUAL & TIMEOUT)
    // ========================================================================

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), "Order not found");

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Order is already CANCELLED");
        }
        if (order.getStatus() == OrderStatus.DELIVERING || order.getStatus() == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Cannot cancel order in status: " + order.getStatus());
        }

        // --- CHECK PAYMENT STATUS (Yêu cầu mới) ---
        boolean hasPaidPayment = order.getPayments().stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);

        if (hasPaidPayment) {
            // Nếu đã thanh toán -> Bắt buộc phải Hoàn tiền trước (Hoặc admin xử lý thủ công)
            // Tùy nghiệp vụ, ở đây ta throw lỗi để chặn hủy ngang
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Đơn hàng đã được thanh toán. Vui lòng thực hiện hoàn tiền (Refund) trước khi hủy đơn.");
        }

        performOrderCancellation(order);
    }

    @Override
    @Transactional
    public void cancelOrderPaymentTimeout() {
        Duration paymentTimeout = Duration.ofMinutes(5);
        LocalDateTime timeoutThreshold = LocalDateTime.now().minus(paymentTimeout);

        List<OrderEntity> candidates = orderRepository.findByStatusAndPaymentMethodAndCreatedAtBefore(
                OrderStatus.PENDING, PaymentMethod.BANK, timeoutThreshold
        );

        if (candidates.isEmpty()) return;

        List<OrderEntity> ordersToCancel = candidates.stream()
                .filter(order -> {
                    // Logic lọc: Chỉ hủy nếu chưa có thanh toán PAID nào
                    Set<PaymentEntity> payments = order.getPayments();
                    if (payments == null || payments.isEmpty()) return true;

                    boolean hasPaid = payments.stream().anyMatch(p -> p.getStatus() == PaymentStatus.PAID);
                    if (hasPaid) return false; // Đã trả tiền -> Không hủy tự động

                    return payments.stream().allMatch(p ->
                            p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.FAILED || p.getStatus() == PaymentStatus.CANCELED
                    );
                })
                .toList();

        if (!ordersToCancel.isEmpty()) {
            log.info("Auto-cancelling {} timed-out orders", ordersToCancel.size());
            ordersToCancel.forEach(this::performOrderCancellation);
        }
    }

    // ========================================================================
    // READ OPERATIONS (Optimized)
    // ========================================================================

    @Override
    public PageDto<OrderDto> getOrderByUser(Long userId, MyOrderRequest request) {
        Sort sort = Sort.by(
                request.getDir().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getOrder().equals("total_amount") ? "totalAmount" : "createdAt"
        );
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<OrderEntity> page = (request.getOrderStatus() != null)
                ? orderRepository.findByUserIdAndStatus(userId, request.getOrderStatus(), pageable)
                : orderRepository.findByUserId(userId, pageable);

        return new PageDto<>(page.map(orderMapper::toDto));
    }

    @Override
    public PageDto<OrderDto> searchOrders(SearchOrderRequest req) {
        Sort sort = req.getDir().equalsIgnoreCase("ASC")
                ? Sort.by(req.getSort()).ascending()
                : Sort.by(req.getSort()).descending();
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);
        Specification<OrderEntity> spec = OrderSpecification.build(req);
        Page<OrderEntity> page = orderRepository.findAll(spec, pageable);
        return new PageDto<>(page.map(orderMapper::toDto));
    }

    @Override
    public UserOrderDetailDto getOrderDetailByUser(Long orderId) {
        UserEntity user = authenticationUtil.getCurrentUser();
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), "Order not found");
        if (!order.getUser().getId().equals(user.getId())) throw new AppException(ErrorCode.FORBIDDEN);
        return orderMapper.toUserOrderDetailDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(Long orderId) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), "Order not found");
        OrderDetailDto orderDetailDto = orderMapper.toOrderDetailDto(order);

        List<ReservationEntity> reservations = reservationRepository.findByOrderId(orderId);
        if (reservations.isEmpty()) {
            orderDetailDto.setSourceGoods(Collections.emptyList());
            return orderDetailDto;
        }

        // --- OPTIMIZED: Tránh N+1 Query ---
        Set<Long> storeLocIds = new HashSet<>();
        Set<Long> warehouseLocIds = new HashSet<>();

        reservations.forEach(res -> {
            if (InventoryLocationType.STORE.equals(res.getLocation().getType()))
                storeLocIds.add(res.getLocation().getId());
            else if (InventoryLocationType.WAREHOUSE.equals(res.getLocation().getType()))
                warehouseLocIds.add(res.getLocation().getId());
        });

        Map<Long, StoreEntity> storeMap = storeRepository.findByInventoryLocationIdIn(storeLocIds).stream()
                .collect(Collectors.toMap(s -> s.getInventoryLocation().getId(), Function.identity()));

        Map<Long, WarehouseEntity> warehouseMap = wareHouseRepository.findByInventoryLocationIdIn(warehouseLocIds).stream()
                .collect(Collectors.toMap(w -> w.getInventoryLocation().getId(), Function.identity()));

        List<OrderDetailDto.SourceGoods> sourceGoods = reservations.stream().map(res -> {
            Long locId = res.getLocation().getId();
            InventoryLocationType type = res.getLocation().getType();
            String name = "N/A";
            String address = "N/A";

            if (type == InventoryLocationType.STORE) {
                StoreEntity s = storeMap.get(locId);
                if (s != null) {
                    name = s.getName();
                    address = s.getDisplayAddress();
                }
            } else if (type == InventoryLocationType.WAREHOUSE) {
                WarehouseEntity w = warehouseMap.get(locId);
                if (w != null) {
                    name = w.getName();
                    address = "Kho trung chuyển";
                }
            }

            return OrderDetailDto.SourceGoods.builder()
                    .type(type).address(address).name(name)
                    .sku(res.getOrderItem().getSku()).quantity(res.getQuantity())
                    .status(res.getStatus())
                    .transferStatus(res.getTransfer() != null ? res.getTransfer().getStatus() : null)
                    .build();
        }).toList();

        orderDetailDto.setSourceGoods(sourceGoods);
        return orderDetailDto;
    }

    // ========================================================================
    // OTHER ACTIONS
    // ========================================================================

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId));
        if (order.getStatus() != OrderStatus.PENDING)
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ xác nhận đơn PENDING");

        order.setStatus(OrderStatus.CONFIRMED);
        List<ReservationEntity> reservations = reservationRepository.findByOrderId(orderId);
        reservations.stream().filter(r -> r.getStatus() == ReservationStatus.DRAFT)
                .forEach(r -> r.setStatus(ReservationStatus.PENDING));

        reservationRepository.saveAll(reservations);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void startDelivery(Long orderId) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), "Order not found");

        // 1. Validate trạng thái: Phải là CONFIRMED mới được đi giao
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Chỉ có thể bắt đầu giao hàng khi đơn ở trạng thái Đã Xác Nhận (CONFIRMED). Trạng thái hiện tại: " + order.getStatus());
        }

        // 2. Cập nhật trạng thái
        order.setStatus(OrderStatus.DELIVERING);
        order.setUpdatedAt(LocalDateTime.now());

        // (Tùy chọn) Nếu muốn đồng bộ reservation sang TRANSFERRING tại đây
        // Tuy nhiên thường thì DeliveryService/TransferService sẽ làm việc này chi tiết hơn.
        // Ở đây ta chỉ cập nhật trạng thái Order tổng.

        orderRepository.save(order);
        log.info("Order {} started delivery.", orderId);
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), "Order not found");

        // 1. Validate trạng thái đầu vào
        // Cho phép hoàn thành từ DELIVERING (Giao hàng) hoặc CONFIRMED (Khách nhận tại quầy)
        if (order.getStatus() != OrderStatus.DELIVERING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Không thể hoàn thành đơn hàng đang ở trạng thái: " + order.getStatus());
        }

        // 2. Validate Tài chính (QUAN TRỌNG): Kiểm tra đã thanh toán đủ chưa
        // paidAmount < totalAmount -> Chặn
        if (order.getPaidAmount().compareTo(order.getTotalAmount()) < 0) {
            BigDecimal remaining = order.getTotalAmount().subtract(order.getPaidAmount());
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Đơn hàng chưa thanh toán đủ. Còn thiếu: " + remaining + ". Vui lòng thanh toán trước khi hoàn thành.");
        }

        // 3. Cập nhật trạng thái Order
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());

        // 4. Đồng bộ trạng thái các Reservation con (Safety net)
        // Đảm bảo tất cả các item con cũng được đánh dấu là COMPLETED nếu chưa
        List<ReservationEntity> reservations = reservationRepository.findByOrderId(orderId);
        boolean hasUpdates = false;

        for (ReservationEntity res : reservations) {
            // Chỉ update những cái chưa xong và chưa hủy
            if (res.getStatus() != ReservationStatus.COMPLETED && res.getStatus() != ReservationStatus.CANCELLED) {
                res.setStatus(ReservationStatus.COMPLETED);
                // Serial cũng phải chuyển sang SOLD nếu chưa
                res.getProductSerials().forEach(s -> s.setStatus(ProductSerialStatus.SOLD));
                productSerialRepository.saveAll(res.getProductSerials());
                hasUpdates = true;
            }
        }

        if (hasUpdates) {
            reservationRepository.saveAll(reservations);
        }

        orderRepository.save(order);
        log.info("Order {} marked as COMPLETED.", orderId);
    }

    // ========================================================================
    // CANCELLATION LOGIC (Refactored)
    // ========================================================================

    private void performOrderCancellation(OrderEntity order) {
        log.info("Performing cancellation for order {}", order.getId());

        // 1. Cancel Debt
        debtRepository.findByOrderId(order.getId()).ifPresent(debt -> {
            debt.setStatus(DebtStatus.CANCELLED);
            debtRepository.save(debt);
        });

        // 2. Cancel Payments
        List<PaymentEntity> payments = new ArrayList<>(order.getPayments());
        payments.forEach(p -> p.setStatus(PaymentStatus.CANCELED));
        paymentRepository.saveAll(payments);

        // 3. Release Inventory & Serials (Optimized)
        List<ReservationEntity> reservations = order.getReservations();
        if (reservations.isEmpty()) {
            updateOrderStatus(order, OrderStatus.CANCELLED);
            return;
        }

        // Batch fetch Inventories
        // Key: locationId_variantId
        Map<String, InventoryEntity> inventoryMap = new HashMap<>();

        // Populate map to avoid query inside loop
        for (ReservationEntity res : reservations) {
            String key = res.getLocation().getId() + "_" + res.getOrderItem().getVariant().getId();
            if (!inventoryMap.containsKey(key)) {
                inventoryRepository.findByInventoryLocationIdAndVariantId(
                        res.getLocation().getId(), res.getOrderItem().getVariant().getId()
                ).ifPresent(inv -> inventoryMap.put(key, inv));
            }
        }

        List<ProductSerialEntity> serialsToUpdate = new ArrayList<>();

        for (ReservationEntity res : reservations) {
            String key = res.getLocation().getId() + "_" + res.getOrderItem().getVariant().getId();
            InventoryEntity inventory = inventoryMap.get(key);

            if (inventory == null) {
                log.error("Inventory missing for Res ID {}", res.getId());
                continue;
            }

            if (inventory.getReservedStock() < res.getQuantity()) {
                log.warn("Stock mismatch: Reserved {} < Release {}", inventory.getReservedStock(), res.getQuantity());
                inventory.setReservedStock(0);
            } else {
                inventory.unReservedStock(res.getQuantity());
            }

            res.getProductSerials().forEach(serial -> {
                serial.setReservation(null);
                serial.setStatus(ProductSerialStatus.IN_STOCK);
                serialsToUpdate.add(serial);
            });

            res.setStatus(ReservationStatus.CANCELLED);
        }

        inventoryRepository.saveAll(inventoryMap.values());
        productSerialRepository.saveAll(serialsToUpdate);
        reservationRepository.saveAll(reservations);

        updateOrderStatus(order, OrderStatus.CANCELLED);
    }

    private void updateOrderStatus(OrderEntity order, OrderStatus status) {
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}