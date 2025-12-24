package com.pbl6.services.impl;

import com.pbl6.dtos.request.inventory.delivery.DeliveryDetailRequest;
import com.pbl6.dtos.request.inventory.delivery.ListDeliveryRequest;
import com.pbl6.dtos.request.inventory.delivery.UpdateTrackingRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.delivery.DeliveryDto;
import com.pbl6.dtos.response.inventory.delivery.DeliveryItemDto;
import com.pbl6.dtos.response.inventory.transfer.TransferDto;
import com.pbl6.dtos.response.inventory.transfer.TransferItemDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.*;
import com.pbl6.enums.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.*;
import com.pbl6.services.DeliveryService;
import com.pbl6.utils.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final ReservationRepository reservationRepository;
    private final ProductSerialRepository productSerialRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final AuthenticationUtil authenticationUtil;

    @Override
    @Transactional
    public DeliveryDto createDelivery(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Danh sách yêu cầu tạo vận đơn không được trống");
        }

        List<ReservationEntity> reservations = reservationRepository.findAllById(reservationIds);
        if (reservations.size() != reservationIds.size()) {
            throw new AppException(ErrorCode.NOT_FOUND, "Một số yêu cầu giữ hàng không tồn tại");
        }

        validateReservationsForDelivery(reservations);

        OrderEntity order = reservations.get(0).getOrder();
        BigDecimal codAmount = calculateCodForDelivery(order, reservations);

        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setOrder(order);
        delivery.setCarrierName("Manual/External");
        delivery.setTrackingCode("SHIP_" + order.getId() + "_" + System.currentTimeMillis());
        delivery.setShippingFee(BigDecimal.ZERO);
        delivery.setCodAmount(codAmount);
        delivery.setStatus(DeliveryStatus.PENDING);

        delivery = deliveryRepository.save(delivery);

        for (ReservationEntity res : reservations) {
            validateSerialStatus(res);
            res.setStatus(ReservationStatus.CONFIRMED);
            res.setDelivery(delivery);
            res.setUpdatedAt(LocalDateTime.now());
        }

        reservationRepository.saveAll(reservations);
        return toDto(delivery);
    }

    @Override
    @Transactional
    public void updateTrackingInfo(Long deliveryId, UpdateTrackingRequest req) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Chỉ có thể cập nhật thông tin khi vận đơn đang chờ xử lý");
        }

        delivery.setCarrierName(req.getCarrierName());
        delivery.setTrackingCode(req.getTrackingCode());
        delivery.setShippingFee(req.getShippingFee());
        delivery.setNote("Cập nhật thủ công: " + req.getCarrierName());

        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        DeliveryEntity delivery = getDeliveryOrThrow(deliveryId);
        if (delivery.getStatus() == newStatus) return;

        List<ReservationEntity> reservations = reservationRepository.findByDeliveryId(deliveryId);
        if (reservations.isEmpty()) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Vận đơn không có hàng hóa liên kết");
        }

        switch (newStatus) {
            case PICKED_UP, DELIVERING -> handleStartDelivery(delivery, reservations, newStatus);
            case DELIVERED -> handleDeliverySuccess(delivery, reservations);
            case FAILED -> handleDeliveryFailed(reservations, delivery.getId());
            case CANCELLED -> handleDeliveryCancelled(delivery, reservations);
            default -> log.warn("Trạng thái không được hỗ trợ xử lý tự động: {}", newStatus);
        }

        delivery.setStatus(newStatus);
        deliveryRepository.save(delivery);
        reservationRepository.saveAll(reservations);

        checkAndUpdateOrderStatus(reservations.get(0).getOrder());
    }

    @Override
    public PageDto<DeliveryDto> getDelivery(ListDeliveryRequest req) {
        UserEntity user = authenticationUtil.getCurrentUser();
        PageRequest pageable = getPageRequest(
                req.getPage(),
                req.getSize(),
                req.getDir(),
                req.getOrder()
        );

        if (user.isAdmin() || user.getIsGlobalStaff()) {
            Page<DeliveryDto> pageResult =
                    deliveryRepository
                            .findByStatus(req.getStatus(), pageable)
                            .map(this::toDto);
            return new PageDto<>(pageResult);
        }

        if (user.getScops() == null || user.getScops().isEmpty()) {
            return PageDto.empty(pageable);
        }

        Page<DeliveryDto> pageResult =
                deliveryRepository
                        .findByStatusNullableAndLocations(
                                req.getStatus(),
                                user.getScops()
                                        .stream()
                                        .map(InventoryLocationEntity::getId)
                                        .collect(Collectors.toSet()),
                                pageable
                        )
                        .map(this::toDto);

        return new PageDto<>(pageResult);
    }


    @Override
    public PageDto<DeliveryItemDto> getDeliveryItems(long id, DeliveryDetailRequest req) {
        if (!deliveryRepository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Transfer not found");
        }
        PageRequest pageable = getPageRequest(req.getPage(), req.getSize(), req.getDir(), req.getOrder());
        Page<DeliveryItemDto> pageResult = reservationRepository.findByDeliveryId(id, pageable).map(
                r -> DeliveryItemDto.builder()
                        .sku(r.getOrderItem().getSku())
                        .variantId(r.getOrderItem().getVariant().getId())
                        .thumbnail(r.getOrderItem().getVariant().getThumbnail())
                        .attributes(r.getOrderItem().getVariant().getVariantAttributeValues().stream()
                                .map(val -> VariantDto.AttributeDto.builder()
                                        .code(val.getAttribute().getCode())
                                        .label(val.getAttribute().getLabel())
                                        .value(val.getAttributeValue().getLabel()).build())
                                .toList())
                        .serials(r.getProductSerials().stream().map(ProductSerialEntity::getSerialNo).collect(Collectors.toList()))
                        .quantity(r.getQuantity())
                        .build()
        );
        return new PageDto<>(pageResult);
    }

    // ========================================================================
    // PRIVATE BUSINESS LOGIC METHODS
    // ========================================================================

    private void handleStartDelivery(DeliveryEntity delivery, List<ReservationEntity> reservations, DeliveryStatus newStatus) {
        // Chỉ xử lý khi chuyển từ PENDING -> DELIVERING/PICKED_UP
        if (delivery.getStatus() != DeliveryStatus.PENDING) return;

        for (ReservationEntity res : reservations) {
            deductInventory(res.getLocation(), res.getOrderItem().getVariant(), res.getQuantity());

            List<String> serials = getSerialNumbers(res);
            productSerialRepository.updateStatusBySerials(serials, ProductSerialStatus.IN_TRANSFER);

            createMovement(res.getLocation(), res.getOrderItem().getVariant(), -res.getQuantity(), "DELIVERY_OUT", delivery.getId());
            res.setStatus(ReservationStatus.TRANSFERRING);
        }
    }

    private void handleDeliverySuccess(DeliveryEntity delivery, List<ReservationEntity> reservations) {
        if (delivery.getStatus() != DeliveryStatus.DELIVERING && delivery.getStatus() != DeliveryStatus.PICKED_UP)
            return;

        for (ReservationEntity res : reservations) {
            List<String> serials = getSerialNumbers(res);
            productSerialRepository.updateStatusBySerials(serials, ProductSerialStatus.SOLD);
            res.setStatus(ReservationStatus.COMPLETED);
        }
        delivery.setActualDeliveryTime(LocalDateTime.now());
        processFinancialsForDelivery(delivery);
    }

    private void handleDeliveryFailed(List<ReservationEntity> reservations, Long deliveryId) {
        for (ReservationEntity res : reservations) {
            if (res.getStatus() == ReservationStatus.TRANSFERRING) {
                // Hoàn kho
                restockInventory(res.getLocation(), res.getOrderItem().getVariant(), res.getQuantity());

                List<String> serials = getSerialNumbers(res);
                productSerialRepository.updateSerialsForReceiving(serials, res.getLocation());

                createMovement(res.getLocation(), res.getOrderItem().getVariant(), res.getQuantity(), "DELIVERY_RETURN", deliveryId);
            }
            res.setStatus(ReservationStatus.CANCELLED);
        }
    }

    private void handleDeliveryCancelled(DeliveryEntity delivery, List<ReservationEntity> reservations) {
        if (delivery.getStatus() == DeliveryStatus.PENDING) {
            for (ReservationEntity res : reservations) {
                res.setStatus(ReservationStatus.PENDING);
                res.setDelivery(null); // QUAN TRỌNG: Gỡ bỏ vận đơn đã hủy để có thể tạo lại
                res.setUpdatedAt(LocalDateTime.now());
            }
        }
    }

    private void validateReservationsForDelivery(List<ReservationEntity> reservations) {
        ReservationEntity first = reservations.get(0);
        Long orderId = first.getOrder().getId();
        Long locationId = first.getLocation().getId();

        for (ReservationEntity res : reservations) {
            if (res.getStatus() != ReservationStatus.PENDING) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Yêu cầu " + res.getId() + " không ở trạng thái chờ (PENDING)");
            }
            if (!res.getOrder().getId().equals(orderId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tất cả yêu cầu phải thuộc cùng một đơn hàng");
            }
            if (!res.getLocation().getId().equals(locationId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tất cả yêu cầu phải xuất phát từ cùng một kho");
            }
            if (res.getOrder().getReceiveMethod() == ReceiveMethod.PICKUP) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Đơn hàng nhận tại quầy không thể tạo vận đơn giao hàng");
            }
        }
    }

    private void validateSerialStatus(ReservationEntity res) {
        long validCount = res.getProductSerials().stream()
                .filter(s -> s.getStatus() == ProductSerialStatus.RESERVED)
                .count();
        if (validCount != res.getQuantity()) {
            throw new AppException(ErrorCode.INTERNAL_ERROR,
                    "Lỗi dữ liệu: Serial của Reservation " + res.getId() + " không ở trạng thái RESERVED");
        }
    }

    private BigDecimal calculateCodForDelivery(OrderEntity order, List<ReservationEntity> reservations) {
        if (order.getPaymentMethod() != PaymentMethod.COD ||
            order.getSubtotal().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal deliverySubtotal = reservations.stream()
                .map(res -> res.getOrderItem().getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tỷ lệ = (Giá trị gói này) / (Tổng giá trị đơn hàng)
        BigDecimal proportion = deliverySubtotal.divide(order.getSubtotal(), 4, RoundingMode.HALF_UP);

        // COD = Tỷ lệ * Tổng tiền còn lại
        return order.getRemainingAmount().multiply(proportion).setScale(0, RoundingMode.HALF_UP);
    }

// Trong DeliveryServiceImpl.java

    private void processFinancialsForDelivery(DeliveryEntity delivery) {
        BigDecimal codAmount = delivery.getCodAmount();
        OrderEntity order = delivery.getOrder();

        // Validate đầu vào kỹ hơn: null check và > 0
        if (codAmount != null && codAmount.compareTo(BigDecimal.ZERO) > 0) {

            // A1. Tạo Payment
            PaymentEntity payment = new PaymentEntity();
            payment.setOrder(order);
            payment.setAmount(codAmount);
            payment.setMethod(PaymentMethod.COD);
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionRef(delivery.getTrackingCode());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // A2. Cập nhật Order (Null Safety)
            BigDecimal currentPaid = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
            BigDecimal newPaidAmount = currentPaid.add(codAmount);

            order.setPaidAmount(newPaidAmount);
            // Lưu ý: totalAmount cũng nên check null nếu cần, nhưng thường order phải có total
            order.setRemainingAmount(order.getTotalAmount().subtract(newPaidAmount));

            // Nếu đã trả hết hoặc trả dư -> Order hoàn tất thanh toán
            // (Tùy logic bên bạn có muốn set status order là PAID không)
            // if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) { ... }

            orderRepository.save(order);

            // A3. Xử lý Nợ (1-1)
            allocatePaymentToDebt(order, codAmount);
        }
    }

    private void allocatePaymentToDebt(OrderEntity order, BigDecimal paymentAmount) {
        // 1. Tìm bản ghi nợ duy nhất
        Optional<DebtEntity> debtOpt = debtRepository.findByOrderId(order.getId());

        if (debtOpt.isEmpty()) {
            // Log warning nếu có tiền trả mà không tìm thấy nợ (tùy nghiệp vụ)
            log.warn("Order {} có thanh toán {} nhưng không tìm thấy bản ghi Debt.", order.getId(), paymentAmount);
            return;
        }

        DebtEntity debt = debtOpt.get();

        // 2. Nếu nợ đã trả xong rồi thì thôi (hoặc có thể xử lý nợ âm/trả thừa tại đây)
        if (DebtStatus.PAID.equals(debt.getStatus())) return; // Hoặc check status enum của bạn

        // 3. Tính toán an toàn (Null Safety)
        BigDecimal totalDebt = debt.getTotalAmount();
        BigDecimal debtPaidSoFar = debt.getPaidAmount() == null ? BigDecimal.ZERO : debt.getPaidAmount();

        // Số tiền CÒN PHẢI TRẢ cho khoản nợ này
        BigDecimal debtRemaining = totalDebt.subtract(debtPaidSoFar);

        // 4. Logic trừ nợ
        if (paymentAmount.compareTo(debtRemaining) >= 0) {
            // Trường hợp trả ĐỦ hoặc DƯ -> Đóng nợ
            // Set đúng bằng totalAmount (không set dư, tránh số liệu nợ bị sai lệch)
            debt.setPaidAmount(totalDebt);
            debt.setStatus(DebtStatus.PAID);

            // (Optional) Nếu paymentAmount > debtRemaining, phần dư đó đang đi đâu?
            // Thường logic tính COD đã chặn việc này, nên ở đây set max là totalDebt là an toàn.
        } else {
            // Trường hợp trả 1 PHẦN
            debt.setPaidAmount(debtPaidSoFar.add(paymentAmount));
            debt.setStatus(DebtStatus.PARTIAL);
        }

        // Cập nhật ngày thanh toán gần nhất (nếu có field)
        debt.setUpdatedAt(LocalDateTime.now());

        debtRepository.save(debt);
    }

    private void checkAndUpdateOrderStatus(OrderEntity order) {
        OrderEntity freshOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Order not found"));

        List<ReservationEntity> reservations = freshOrder.getReservations();
        if (reservations.isEmpty()) return;

        // Điều kiện 1: TẤT CẢ phải là COMPLETED (Không chấp nhận Cancelled/Failed)
        boolean allStrictlySuccess = reservations.stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.COMPLETED);

        // Điều kiện 2: Có bất kỳ món nào đang đi giao không?
        boolean isShipping = reservations.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.TRANSFERRING);

        boolean allFailed = reservations.stream()
                .allMatch(r -> r.getStatus() == ReservationStatus.CANCELLED);

        // 3. Cập nhật trạng thái
        if (allStrictlySuccess) {
            // CASE: Thành công tuyệt đối -> Đóng đơn
            if (freshOrder.getStatus() != OrderStatus.COMPLETED) {
                freshOrder.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(freshOrder);
                log.info("Order ID {} -> COMPLETED (100% Items Delivered)", freshOrder.getId());
            }
        } else if (allFailed) {
            // Tất cả đều thất bại/hủy -> Hủy đơn hàng
            if (freshOrder.getStatus() != OrderStatus.CANCELLED) {
                freshOrder.setStatus(OrderStatus.FAILED);
                // Có thể thêm lý do hủy tự động
                freshOrder.setNote(freshOrder.getNote() + " [Hệ thống: Tự động hủy do giao thất bại]");
                orderRepository.save(freshOrder);
                processRefundAndCancelDebt(order, "Đơn hàng bị hủy do tất cả giao hàng thất bại");
            }
        } else if (isShipping) {
            // CASE: Đang giao hàng
            if (freshOrder.getStatus() != OrderStatus.DELIVERING) {
                freshOrder.setStatus(OrderStatus.DELIVERING);
                orderRepository.save(freshOrder);
            }
        }
        // CASE ĐẶC BIỆT: Giao xong hết rồi (không còn ai Transferring)
        // NHƯNG có 1 món bị Fail/Cancel.
        // -> Code sẽ KHÔNG chạy vào block 'allStrictlySuccess'.
        // -> Đơn hàng sẽ GIỮ NGUYÊN trạng thái cũ (thường là DELIVERING).
        // -> Nhân viên phải vào xử lý thủ công (hoặc hệ thống cần thêm status PARTIALLY_FAILED).
    }

        private void processRefundAndCancelDebt(OrderEntity order, String reason) {
            BigDecimal paidAmount = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();

            // A. Nếu đã có thanh toán -> Thực hiện hoàn tiền
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                // Tạo bản ghi Payment loại hoàn tiền (có thể dùng Status REFUNDED hoặc số tiền âm)
                PaymentEntity refundPayment = new PaymentEntity();
                refundPayment.setOrder(order);
                refundPayment.setAmount(paidAmount); // Số tiền âm để bù trừ
                refundPayment.setMethod(order.getPaymentMethod());
                refundPayment.setStatus(PaymentStatus.REFUNDED); // Đã thực hiện hoàn
                refundPayment.setTransactionRef("REFUND_" + order.getId() + "_" + System.currentTimeMillis());
                refundPayment.setCreatedAt(LocalDateTime.now());
                paymentRepository.save(refundPayment);

                // Cập nhật lại Order
                order.setPaidAmount(BigDecimal.ZERO);
                order.setRemainingAmount(order.getTotalAmount());
                orderRepository.save(order);

                log.info("Đã tạo bản ghi hoàn tiền cho Order {}: {}", order.getId(), paidAmount);
            }
            // B. Xử lý bản ghi Nợ (Debt)
            Optional<DebtEntity> debtOpt = debtRepository.findByOrderId(order.getId());
            if (debtOpt.isPresent()) {
                DebtEntity debt = debtOpt.get();
                // Nếu đơn failed/cancelled thì khoản nợ này không còn giá trị thu hồi
                debt.setStatus(DebtStatus.CANCELLED); // Cần thêm status CANCELLED trong Enum DebtStatus
                debt.setUpdatedAt(LocalDateTime.now());
                debtRepository.save(debt);
                log.info("Đã hủy bản ghi nợ cho Order {}", order.getId());
            }
        }



    // ========================================================================
    // HELPERS
    // ========================================================================

    private DeliveryEntity getDeliveryOrThrow(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Delivery not found"));
    }

    private List<String> getSerialNumbers(ReservationEntity res) {
        return res.getProductSerials().stream().map(ProductSerialEntity::getSerialNo).toList();
    }

    private void updateInventoryReserve(InventoryLocationEntity loc, VariantEntity variant, int qty, boolean isAdd) {
        InventoryEntity inv = getInventoryOrThrow(loc.getId(), variant.getId());
        int currentReserved = inv.getReservedStock() == null ? 0 : inv.getReservedStock();
        inv.setReservedStock(isAdd ? currentReserved + qty : Math.max(0, currentReserved - qty));
        inventoryRepository.save(inv);
    }

    private void deductInventory(InventoryLocationEntity loc, VariantEntity variant, int qty) {
        InventoryEntity inv = getInventoryOrThrow(loc.getId(), variant.getId());

        if (inv.getStock() < qty) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Kho không đủ số lượng tồn thực tế để xuất hàng");
        }

        inv.setStock(inv.getStock() - qty);
        int currentReserved = inv.getReservedStock() == null ? 0 : inv.getReservedStock();
        inv.setReservedStock(Math.max(0, currentReserved - qty));
        inventoryRepository.save(inv);
    }

    private void restockInventory(InventoryLocationEntity loc, VariantEntity variant, int qty) {
        InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(loc.getId(), variant.getId())
                .orElseGet(() -> {
                    InventoryEntity newInv = new InventoryEntity();
                    newInv.setInventoryLocation(loc);
                    newInv.setVariant(variant);
                    newInv.setStock(0);
                    newInv.setReservedStock(0);
                    return newInv;
                });
        inv.setStock(inv.getStock() + qty);
        inventoryRepository.save(inv);
    }

    private InventoryEntity getInventoryOrThrow(Long locId, Long variantId) {
        return inventoryRepository.findByInventoryLocationIdAndVariantId(locId, variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Inventory record not found"));
    }

    private void createMovement(InventoryLocationEntity loc, VariantEntity variant, int qtyDelta, String reason, Long refId) {
        StockMovementEntity mov = new StockMovementEntity();
        mov.setInventoryLocation(loc);
        mov.setVariant(variant);
        mov.setQuantityDelta(qtyDelta);
        mov.setReason(reason);
        mov.setRefType("DELIVERY");
        mov.setRefId(refId);
        mov.setCreatedAt(LocalDateTime.now());
        stockMovementRepository.save(mov);
    }

    private DeliveryDto toDto(DeliveryEntity entity) {
        return DeliveryDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .carrierName(entity.getCarrierName())
                .trackingCode(entity.getTrackingCode())
                .status(entity.getStatus())
                .codAmount(entity.getCodAmount())
                .build();
    }



    private PageRequest getPageRequest(int page, int size, String dir, String order) {
        Sort sort = Sort.by(dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, order);
        return PageRequest.of(page - 1, size, sort);
    }

}