package com.pbl6.services.impl;

import com.pbl6.dtos.request.order.CreateOrderRequest;
import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.order.OrderItemRequest;
import com.pbl6.dtos.request.order.SearchOrderRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDetailDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.response.order.UserOrderDetailDto;
import com.pbl6.dtos.response.payment.PaymentInitResponse;
import com.pbl6.entities.*;
import com.pbl6.enums.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.OrderMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.InventoryService;
import com.pbl6.services.OrderService;
import com.pbl6.services.PaymentService;
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
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public OrderEntity createOrder(CreateOrderRequest req) {
        UserEntity buyer = userRepository.findById(req.getUserId()).get();

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
                    .order(null)
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
                .totalAmount(orderSubtotal)
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
        if (currentUser.isAdmin() || currentUser.getIsGlobalStaff()) {
            // ✅ ADMIN có thể chọn bất kỳ sale nào
            sale = null;
            if (req.getSaleId() != null) {
                sale = entityUtil.ensureExists(
                        userRepository.findById(req.getSaleId()),
                        "Sale not found"
                );
            }
        } else {
            boolean hasScope = currentUser.getScops()
                    .stream()
                    .anyMatch(loc -> loc.getId().equals(store.getInventoryLocation().getId()));
            // ✅ NHÂN VIÊN bán hàng
            if (!hasScope) {
                throw new AppException(ErrorCode.FORBIDDEN, "You cannot create order for another store");
            }
            if (req.getSaleId() != null && !req.getSaleId().equals(currentUser.getId())) {
                throw new AppException(ErrorCode.FORBIDDEN, "Sale ID does not match current user");
            }
        }
        req.setIsOnline(false);
        req.setPaymentMethod(PaymentMethod.COD);
        req.setReceiveMethod(ReceiveMethod.PICKUP);

        OrderEntity order = createOrder(req);

        //Giữ hàng
        inventoryService.handlePickupAtStore(store, order.getOrderItems());

        //Tạo yêu cầu payment
        PaymentInitResponse payRes = paymentService.create(order);

        //tạo xong xác nhận luôn
        confirmOrder(order.getId());

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

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Không thể hủy đơn hàng ở trạng thái này");
        }

        // Nếu đơn đang giao (DELIVERING), thường phải qua luồng trả hàng của DeliveryService
        // để kiểm soát hàng vật lý. Ở đây ta chỉ cho phép hủy PENDING/CONFIRMED.
        if (order.getStatus() == OrderStatus.DELIVERING) {
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Đơn đang giao, vui lòng xử lý thất bại bên vận đơn");
        }

        // Xử lý hoàn tiền nếu đã có thanh toán thành công
        if (order.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            handleRefund(order);
        }

        performOrderCancellation(order);
    }

    private void handleRefund(OrderEntity order) {
        BigDecimal refundAmount = order.getPaidAmount();

        PaymentEntity refund = new PaymentEntity();
        refund.setOrder(order);
        refund.setAmount(refundAmount.negate()); // Số âm để trừ vào tổng doanh thu
        refund.setMethod(order.getPaymentMethod());
        refund.setStatus(PaymentStatus.REFUNDED); // Ghi nhận đã hoàn (hoặc PENDING_REFUND nếu cần kế toán duyệt)
        refund.setTransactionRef("REFUND_" + order.getId() + "_" + System.currentTimeMillis());
        refund.setCreatedAt(LocalDateTime.now());

        paymentRepository.save(refund);

        // Cập nhật lại số dư trên đơn hàng về 0
        order.setPaidAmount(BigDecimal.ZERO);
        order.setRemainingAmount(order.getTotalAmount());
        order.setNote("Đã hoàn tiền");
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

        UserEntity user = authenticationUtil.getCurrentUser();

        Sort sort = req.getDir().equalsIgnoreCase("ASC")
                ? Sort.by(req.getSort()).ascending()
                : Sort.by(req.getSort()).descending();

        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);

        Specification<OrderEntity> spec = OrderSpecification.build(req);

        // ✅ Chỉ staff thường mới bị giới hạn scope
        if (!user.isAdmin() && !Boolean.TRUE.equals(user.getIsGlobalStaff())) {

            List<Long> storeIds = storeRepository
                    .findByInventoryLocationIn(user.getScops())
                    .stream()
                    .map(StoreEntity::getId)
                    .toList();

            // ⚠️ Nếu staff không có store nào → không trả dữ liệu
            if (storeIds.isEmpty()) {
                return PageDto.empty(pageable);
            }

            spec = spec.and(OrderSpecification.belongsToStores(storeIds));
        }

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
                    .serials(res.getProductSerials().stream().map(ProductSerialEntity::getSerialNo).toList())
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

        validateStatus(order);

        List<ReservationEntity> reservations = reservationRepository.findByOrderId(orderId);
        BigDecimal remaining = order.getTotalAmount().subtract(order.getPaidAmount());

        if (order.getReceiveMethod() == ReceiveMethod.DELIVERY) {
            boolean isAllDelivered = reservations.stream()
                    .allMatch(r -> r.getStatus() == ReservationStatus.COMPLETED);
            if (!isAllDelivered) {
                throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Đơn hàng chưa được giao thành công");
            }
        } else {
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                paymentService.createTransaction(order, remaining, PaymentMethod.CASH_AT_COUNTER);
                order.setPaidAmount(order.getTotalAmount());
                order.setRemainingAmount(BigDecimal.ZERO);
            }

            for (ReservationEntity res : reservations) {
                if (!res.getStatus().equals(ReservationStatus.AVAILABLE)) {
                    throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR,"Không thể hoàn thành đơn hàng vì cửa hàng không có sẵn sản phẩm");
                } else {
                    processPickupInventory(res);
                    res.setStatus(ReservationStatus.COMPLETED);
                }
            }
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());

        reservationRepository.saveAll(reservations);
        orderRepository.save(order);
    }

    private void validateStatus(OrderEntity order) {
        if (order.isOnline() && order.getReceiveMethod() == ReceiveMethod.DELIVERY) {
            if (order.getStatus() != OrderStatus.DELIVERING) {
                throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Đơn giao tận nhà phải ở trạng thái đang giao mới được hoàn thành");
            }
        } else {
            if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.DELIVERING) {
                throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION, "Đơn nhận tại quầy phải ở trạng thái đã xác nhận mới được hoàn thành");
            }
        }
    }

    private void processPickupInventory(ReservationEntity res) {
        InventoryEntity inventory = inventoryRepository.findByInventoryLocationIdAndVariantId(
                        res.getLocation().getId(), res.getOrderItem().getVariant().getId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR, "Inventory record not found"));

        // 1. Trừ tồn kho vật lý và nhả hàng giữ chỗ
        inventory.setStock(inventory.getStock() - res.getQuantity());
        int currentReserved = inventory.getReservedStock() == null ? 0 : inventory.getReservedStock();
        inventory.setReservedStock(Math.max(0, currentReserved - res.getQuantity()));
        inventoryRepository.save(inventory);

        // 2. CẬP NHẬT TRẠNG THÁI SERIAL SANG SOLD
        if (res.getProductSerials() != null && !res.getProductSerials().isEmpty()) {
            res.getProductSerials().forEach(serial -> {
                serial.setStatus(ProductSerialStatus.SOLD);
                serial.setUpdatedAt(LocalDateTime.now());
            });
            productSerialRepository.saveAll(res.getProductSerials());
        }

        // 3. Ghi nhận biến động kho
        StockMovementEntity mov = new StockMovementEntity();
        mov.setInventoryLocation(res.getLocation());
        mov.setVariant(res.getOrderItem().getVariant());
        mov.setQuantityDelta(-res.getQuantity());
        mov.setReason("STORE_PICKUP");
        mov.setRefType("ORDER");
        mov.setRefId(res.getOrder().getId());
        mov.setCreatedAt(LocalDateTime.now());

        // Ghi log serial vào ghi chú của Stock Movement để dễ truy vết
        if (res.getProductSerials() != null && !res.getProductSerials().isEmpty()) {
            String serialList = res.getProductSerials().stream()
                    .map(ProductSerialEntity::getSerialNo)
                    .collect(Collectors.joining(", "));
            mov.setReason("Xuất trực tiếp tại quầy. Serials: " + serialList);
        } else {
            mov.setReason("Xuất trực tiếp tại quầy.");
        }

        stockMovementRepository.save(mov);
    }

    // ========================================================================
    // CANCELLATION LOGIC (Refactored)
    // ========================================================================

    private void performOrderCancellation(OrderEntity order) {
        // 1. Hủy nợ
        debtRepository.findByOrderId(order.getId()).ifPresent(debt -> {
            debt.setStatus(DebtStatus.CANCELLED);
            debtRepository.save(debt);
        });

        // 2. Nhả hàng giữ chỗ và Serial
        List<ReservationEntity> reservations = order.getReservations();
        List<ProductSerialEntity> serialsToUpdate = new ArrayList<>();

        for (ReservationEntity res : reservations) {
            if (res.getStatus() == ReservationStatus.CANCELLED) continue;

            // Trả lại hàng giữ chỗ về kho
            inventoryRepository.findByInventoryLocationIdAndVariantId(
                    res.getLocation().getId(), res.getOrderItem().getVariant().getId()
            ).ifPresent(inv -> {
                inv.unReservedStock(res.getQuantity());
                inventoryRepository.save(inv);
            });

            // Đưa Serial về lại trạng thái sẵn sàng
            res.getProductSerials().forEach(serial -> {
                serial.setReservation(null);
                serial.setStatus(ProductSerialStatus.IN_STOCK);
                serialsToUpdate.add(serial);
            });

            res.setStatus(ReservationStatus.CANCELLED);
        }

        productSerialRepository.saveAll(serialsToUpdate);
        reservationRepository.saveAll(reservations);

        // 3. Đóng trạng thái đơn hàng
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void updateOrderStatus(OrderEntity order, OrderStatus status) {
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

//    // Hàm hỗ trợ trừ kho thực tế khi lấy hàng tại quầy
//    private void deductPhysicalStockForPickup(ReservationEntity res) {
//        inventoryRepository.findByInventoryLocationIdAndVariantId(
//                        res.getLocation().getId(), res.getOrderItem().getVariant().getId())
//                .ifPresent(inventory -> {
//                    // 1. Trừ kho thực tế (stock) và kho giữ chỗ (reserved)
//                    inventory.setStock(inventory.getStock() - res.getQuantity());
//                    inventory.unReservedStock(res.getQuantity());
//                    inventoryRepository.save(inventory);
//
//                    // 2. Ghi nhận Stock Movement
//                    createStockMovement(res);
//                });
//    }
//
//    private void createStockMovement(ReservationEntity res) {
//        StockMovementEntity mov = new StockMovementEntity();
//
//        // 1. Thông tin kho và sản phẩm
//        mov.setInventoryLocation(res.getLocation());
//        mov.setVariant(res.getOrderItem().getVariant());
//
//        // 2. Số lượng delta (Âm vì hàng xuất ra khỏi kho)
//        mov.setQuantityDelta(-res.getQuantity());
//
//        // 3. Thông tin tham chiếu
//        mov.setReason("STORE_PICKUP"); // Lý do: Khách lấy tại quầy
//        mov.setRefType("ORDER");       // Loại tham chiếu: Đơn hàng
//        mov.setRefId(res.getOrder().getId());
//
//        // 4. Lưu vết Serial (Nếu có quản lý theo Serial)
//        if (res.getProductSerials() != null && !res.getProductSerials().isEmpty()) {
//            String serialList = res.getProductSerials().stream()
//                    .map(ProductSerialEntity::getSerialNo)
//                    .collect(Collectors.joining(", "));
//            mov.setReason("Xuất trực tiếp tại quầy. Serials: " + serialList);
//        } else {
//            mov.setReason("Xuất trực tiếp tại quầy.");
//        }
//
//        mov.setCreatedAt(LocalDateTime.now());
//
//        // 5. Lưu vào Database
//        stockMovementRepository.save(mov);
//    }
}