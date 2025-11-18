package com.pbl6.services.impl;

import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.order.OrderItemRequest;
import com.pbl6.dtos.request.order.CreateOrderRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final AuthenticationUtil authenticationUtil;
    private final WareHouseRepository wareHouseRepository;
    private final UserRepository userRepository;

    // ---------------------- CREATE ORDER ----------------------

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

        if (ordersToCancel.isEmpty()) return;

        log.info("Found {} pending BANK orders exceeding payment timeout. Cancelling...", ordersToCancel.size());

        ordersToCancel.forEach(this::performOrderCancellation);
        log.info("Successfully cancelled {} timed-out orders.", ordersToCancel.size());
    }


    // ---------------------- GET USER ORDERS ----------------------

    public PageDto<OrderDto> getOrderByUser(Long userId, MyOrderRequest request) {
        Sort.Direction direction = request.getDir().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Map<String, String> fieldMapping = Map.of(
                "created_at", "createdAt",
                "total_amount", "totalAmount"
        );

        String sortField = fieldMapping.getOrDefault(request.getOrder(), "createdAt");

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), Sort.by(direction, sortField));

        Page<OrderEntity> page;

        if (request.getOrderStatus() != null) {
            page = orderRepository.findByUserIdAndStatus(userId, request.getOrderStatus(), pageable);
        } else {
            page = orderRepository.findByUserId(userId, pageable);
        }
        List<OrderDto> content = page.getContent().stream()
                .map(orderMapper::toDto)
                .toList();

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
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId), "order not found");
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return orderMapper.toUserOrderDetailDto(order);
    }

    @Override
    public OrderDetailDto getOrderDetail(Long orderId) {
        // Lấy thông tin order từ DB, nếu không có thì throw lỗi
        OrderEntity order = entityUtil.ensureExists(
                orderRepository.findById(orderId),
                "order not found"
        );

        // Map entity sang DTO cơ bản
        OrderDetailDto orderDetailDto = orderMapper.toOrderDetailDto(order);

        // Lấy danh sách nguồn hàng từ bảng Reservation
        var sourceGoods = reservationRepository.findByOrderId(orderId).stream()
                .map(res -> {
                    InventoryLocationType type = res.getLocation().getType();
                    String address;
                    String name;

                    // Nếu là cửa hàng (STORE)
                    if (InventoryLocationType.STORE.equals(type)) {
                        StoreEntity store = storeRepository
                                .findByInventoryLocationId(res.getLocation().getId())
                                .orElseThrow(() -> {
                                    log.error("Store not found for inventory location id: {}", res.getLocation().getId());
                                    return new AppException(ErrorCode.INTERNAL_ERROR);
                                });
                        address = store.getDisplayAddress();
                        name = store.getName();
                    }
                    // Nếu là kho (WAREHOUSE)
                    else if (InventoryLocationType.WAREHOUSE.equals(type)) {
                        WarehouseEntity warehouse = wareHouseRepository
                                .findWarehouseByInventoryLocationId(res.getLocation().getId())
                                .orElseThrow(() -> {
                                    log.error("Warehouse not found for inventory location id: {}", res.getLocation().getId());
                                    return new AppException(ErrorCode.INTERNAL_ERROR);
                                });
                        address = "Địa chỉ kho (tạm thời, chưa có trường địa chỉ)";
                        name = warehouse.getName();
                    }
                    // Nếu loại khác (phòng trường hợp future)
                    else {
                        address = "Không xác định";
                        name = "N/A";
                    }

                    // Tạo DTO con cho SourceGoods
                    return OrderDetailDto.SourceGoods.builder()
                            .type(type)
                            .address(address)
                            .name(name)
                            .sku(res.getOrderItem().getSku())
                            .quantity(res.getQuantity())
                            .status(res.getStatus())
                            .transferStatus(
                                    res.getTransfer() == null
                                            ? null
                                            : res.getTransfer().getStatus()
                            )
                            .build();
                })
                .toList();

        // Gắn danh sách nguồn hàng vào order detail
        orderDetailDto.setSourceGoods(sourceGoods);

        return orderDetailDto;
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        OrderEntity order = entityUtil.ensureExists(orderRepository.findById(orderId));
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể xác nhận đơn ở trạng thái chờ xác nhận");
        }
        order.setStatus(OrderStatus.CONFIRMED);
        List<ReservationEntity> reservations = reservationRepository.findByOrderId(orderId);
        reservations.stream()
                .filter(res -> res.getStatus().equals(ReservationStatus.DRAFT))
                .forEach(res -> res.setStatus(ReservationStatus.PENDING));
        reservationRepository.saveAll(reservations);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Order is already CANCELLED. No action taken");
        }

        if (order.getStatus() == OrderStatus.DELIVERING || order.getStatus() == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Cannot be cancelled as it is already " + order.getStatus());
        }

        performOrderCancellation(order);
    }

    @Override
    public void startDelivery(Long orderId) {

    }

    @Override
    public void markAsDelivered(Long orderId) {

    }

    @Override
    public void completeOrder(Long orderId) {

    }

    @Override
    public void returnOrder(Long orderId) {

    }

    private void performOrderCancellation(OrderEntity order) {
        List<PaymentEntity> paymentsToUpdate = new ArrayList<>();
        List<InventoryEntity> inventoriesToUpdate = new ArrayList<>();
        List<ProductSerialEntity> productSerialsToUpdate = new ArrayList<>();
        List<ReservationEntity> reservationsToUpdate = new ArrayList<>();

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        // Huỷ thanh toán
        order.getPayments().forEach(payment -> {
            payment.setStatus(PaymentStatus.CANCELED);
            paymentsToUpdate.add(payment);
        });

        // Giải phóng tồn kho, serial, reservation
        order.getReservations().forEach(reservation -> {
            int quantity = reservation.getQuantity();
            OrderItemEntity orderItem = reservation.getOrderItem();
            InventoryLocationEntity location = reservation.getLocation();

            InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(
                            location.getId(), orderItem.getVariant().getId())
                    .orElseThrow(() -> {
                        log.error("Inventory not found for location {} and variant {} (order {}).",
                                location.getId(), orderItem.getVariant().getId(), order.getId());
                        return new AppException(ErrorCode.NOT_FOUND);
                    });

            if (inventory.getReservedStock() < quantity) {
                log.error("Oversell: unreserve {} units, only {} reserved (inventory ID {}).",
                        quantity, inventory.getReservedStock(), inventory.getId());
                throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Oversell");
            }

            inventory.unReservedStock(quantity);
            inventoriesToUpdate.add(inventory);

            reservation.getProductSerials().forEach(serial -> {
                serial.setReservation(null);
                serial.setStatus(ProductSerialStatus.IN_STOCK);
                productSerialsToUpdate.add(serial);
            });

            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationsToUpdate.add(reservation);
        });

        // Batch save để tối ưu hiệu năng
        paymentRepository.saveAll(paymentsToUpdate);
        inventoryRepository.saveAll(inventoriesToUpdate);
        productSerialRepository.saveAll(productSerialsToUpdate);
        reservationRepository.saveAll(reservationsToUpdate);
        orderRepository.save(order);
    }
}
