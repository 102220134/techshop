package com.pbl6.services.impl;
import com.pbl6.dtos.request.inventory.delivery.UpdateTrackingRequest;
import com.pbl6.dtos.response.inventory.delivery.DeliveryDto;
import com.pbl6.entities.*;
import com.pbl6.enums.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.*;
import com.pbl6.services.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * TÁI CẤU TRÚC: Tạo 1 Vận đơn (Delivery) từ 1 hoặc NHIỀU Reservation
     * Dùng để gom các món hàng (từ cùng 1 kho) vào 1 gói hàng để giao.
     */
    @Override
    @Transactional
    public DeliveryDto createDelivery(List<Long> reservationIds) {
        if (reservationIds == null || reservationIds.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Phải chọn ít nhất 1 yêu cầu để tạo vận đơn");
        }

        // 1. Lấy và kiểm tra thông tin chung
        List<ReservationEntity> reservations = reservationRepository.findAllById(reservationIds);
        if (reservations.isEmpty() || reservations.size() != reservationIds.size()) {
            throw new AppException(ErrorCode.NOT_FOUND, "Một số yêu cầu (Reservation) không tồn tại.");
        }

        // 2. Validate Logic
        validateReservationsForDelivery(reservations);
        OrderEntity order = reservations.get(0).getOrder();
        InventoryLocationEntity location = reservations.get(0).getLocation();

        // 3. Tính toán COD Pro-rata (Chia tỷ lệ)
        BigDecimal codAmountForThisDelivery = calculateCodForDelivery(order, reservations);

        // 4. Tạo Vận đơn (Delivery Entity)
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setOrder(order);
        delivery.setCarrierName("Manual/External"); // Mặc định
        delivery.setTrackingCode("WAITING_UPDATE_" + order.getId() + "_" + System.currentTimeMillis()); // Mã tạm
        delivery.setShippingFee(BigDecimal.ZERO); // Sẽ cập nhật sau
        delivery.setCodAmount(codAmountForThisDelivery); // Tiền COD đã chia tỷ lệ
        delivery.setStatus(DeliveryStatus.PENDING);
        delivery = deliveryRepository.save(delivery);

        // 5. Xử lý kho và cập nhật các Reservation
        for (ReservationEntity res : reservations) {
            // 5a. Update Inventory: Tăng ReservedStock (Hàng được giữ)
//            updateInventoryReserve(location, res.getOrderItem().getVariant(), res.getQuantity(), true);

            // 5b. Update Serial: (Đã là RESERVED từ lúc tạo đơn, không cần làm lại)
            // Chúng ta chỉ cần đảm bảo chúng không bị ai cướp mất
            long serialCount = res.getProductSerials().stream()
                    .filter(s -> s.getStatus() == ProductSerialStatus.RESERVED)
                    .count();
            if (serialCount != res.getQuantity()) {
                log.error("Lỗi dữ liệu: Serial của Reservation {} không ở trạng thái RESERVED.", res.getId());
                throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi đồng bộ, serial đã bị thay đổi trạng thái.");
            }

            // 5c. Update Reservation (Link Delivery và Cập nhật Status)
            res.setStatus(ReservationStatus.CONFIRMED); // Chuyển từ PENDING -> CONFIRMED (đã gán vào vận đơn)
            res.setDelivery(delivery);
            res.setUpdatedAt(LocalDateTime.now());
        }

        reservationRepository.saveAll(reservations);

        return toDto(delivery);
    }

    /**
     * Kiểm tra các điều kiện để gom nhiều Reservation vào 1 Delivery
     */
    private void validateReservationsForDelivery(List<ReservationEntity> reservations) {
        Long firstOrderId = reservations.get(0).getOrder().getId();
        Long firstLocationId = reservations.get(0).getLocation().getId();

        for (ReservationEntity res : reservations) {
            // Check 1: Phải đang chờ (PENDING)
            if (res.getStatus() != ReservationStatus.PENDING) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Yêu cầu " + res.getId() + " đã được xử lý.");
            }
            // Check 2: Phải cùng 1 Order
            if (!res.getOrder().getId().equals(firstOrderId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Các yêu cầu phải thuộc cùng 1 đơn hàng.");
            }
            // Check 3: Phải cùng 1 Kho Nguồn
            if (!res.getLocation().getId().equals(firstLocationId)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Các yêu cầu phải xuất phát từ cùng 1 kho.");
            }
            // Check 4: Phải là đơn Giao Hàng (Không phải Nhận tại quầy)
            if (res.getOrder().getReceiveMethod() == ReceiveMethod.PICKUP) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Đây là đơn nhận tại quầy, không thể tạo vận đơn giao hàng.");
            }
        }
    }

    /**
     * Tính toán COD tỷ lệ cho các món hàng trong vận đơn này.
     */
    private BigDecimal calculateCodForDelivery(OrderEntity order, List<ReservationEntity> reservations) {
        // Nếu khách thanh toán online (không phải COD), thì tiền thu hộ = 0
        if (order.getPaymentMethod() != PaymentMethod.COD) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRemaining = order.getRemainingAmount(); // Tổng tiền còn lại của cả đơn hàng
        BigDecimal orderSubtotal = order.getSubtotal(); // Tổng giá trị hàng hóa của cả đơn hàng

        // Nếu tổng đơn = 0 (đơn 0 đồng), thì COD = 0
        if (orderSubtotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Tính tổng giá trị (subtotal) của CÁC MÓN HÀNG trong vận đơn này
        BigDecimal subtotalForThisDelivery = BigDecimal.ZERO;
        for (ReservationEntity res : reservations) {
            // Giả sử subtotal trong OrderItem là giá cuối (đã trừ khuyến mãi)
            subtotalForThisDelivery = subtotalForThisDelivery.add(res.getOrderItem().getSubtotal());
        }

        // Tính tỷ lệ giá trị của gói hàng này so với tổng đơn hàng
        // Tỷ lệ = (Giá trị gói này) / (Tổng giá trị đơn hàng)
        BigDecimal proportion = subtotalForThisDelivery.divide(orderSubtotal, 4, RoundingMode.HALF_UP);

        // COD của gói này = (Tỷ lệ) * (Tổng tiền còn lại của đơn hàng)
        BigDecimal codAmount = totalRemaining.multiply(proportion).setScale(0, RoundingMode.HALF_UP); // Làm tròn đến đồng

        return codAmount;
    }


    // ... (Toàn bộ các hàm khác: updateTrackingInfo, updateDeliveryStatus, helpers... giữ nguyên) ...
    // ...
    @Override
    @Transactional
    public void updateTrackingInfo(Long deliveryId, UpdateTrackingRequest req) {
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Không tìm thấy vận đơn"));

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Không thể cập nhật. Vận đơn đã được xử lý hoặc đang trên đường giao.");
        }

        delivery.setCarrierName(req.getCarrierName());
        delivery.setTrackingCode(req.getTrackingCode());
        delivery.setShippingFee(req.getShippingFee());
        delivery.setNote("Đã cập nhật mã vận đơn thủ công (" + req.getCarrierName() + ")");

        deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Delivery not found"));

        if (delivery.getStatus() == newStatus) return;

        List<ReservationEntity> reservations = reservationRepository.findByDeliveryId(deliveryId);
        if (reservations.isEmpty()) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Dữ liệu vận đơn không liên kết với yêu cầu kho.");
        }

        OrderEntity order = reservations.get(0).getOrder();

        for (ReservationEntity res : reservations) {
            InventoryLocationEntity location = res.getLocation();
            VariantEntity variant = res.getOrderItem().getVariant();
            List<String> serials = res.getProductSerials().stream().map(ProductSerialEntity::getSerialNo).toList();
            int qty = res.getQuantity();

            switch (newStatus) {
                case PICKED_UP:
                case DELIVERING:
                    if (delivery.getStatus() == DeliveryStatus.PENDING) {
                        deductInventory(location, variant, qty);
                        productSerialRepository.updateStatusBySerials(serials,ProductSerialStatus.IN_TRANSFER);
                        createMovement(location, variant, -qty, "DELIVERY_OUT", delivery.getId());
                        res.setStatus(ReservationStatus.TRANSFERRING);
                    }
                    break;
                case DELIVERED:
                    if (delivery.getStatus() == DeliveryStatus.DELIVERING) {
                        productSerialRepository.updateStatusBySerials(serials, ProductSerialStatus.SOLD);
                        delivery.setActualDeliveryTime(LocalDateTime.now());
                        res.setStatus(ReservationStatus.COMPLETED);
                    }
                    break;
//                case RETURNED:
                case FAILED:
                    if (res.getStatus() == ReservationStatus.TRANSFERRING) {
                        restockInventory(location, variant, qty);
                        productSerialRepository.updateSerialsForReceiving(serials, location);
                        createMovement(location, variant, qty, "DELIVERY_RETURN", delivery.getId());
                    }
                    res.setStatus(ReservationStatus.CANCELLED);
                    break;
                case CANCELLED:
                    if (delivery.getStatus() == DeliveryStatus.PENDING) {
                        updateInventoryReserve(location, variant, qty, false);
                        productSerialRepository.updateStatusBySerials(serials, ProductSerialStatus.IN_STOCK);
                        res.setStatus(ReservationStatus.CANCELLED);
                    }
                    break;
                default:
                    break;
            }
        }

        delivery.setStatus(newStatus);
        deliveryRepository.save(delivery);
        reservationRepository.saveAll(reservations);

        // KIỂM TRA ĐƠN HÀNG HOÀN THÀNH (Logic quan trọng)
        checkAndUpdateOrderStatus(order);
    }

    /**
     * Kiểm tra xem tất cả Reservation của 1 Order đã hoàn thành/hủy chưa,
     * nếu rồi thì tự động cập nhật Order status.
     */
    private void checkAndUpdateOrderStatus(OrderEntity order) {
        // Tải lại order để lấy trạng thái mới nhất của tất cả reservations
        OrderEntity freshOrder = orderRepository.findById(order.getId()).get();

        boolean allCompletedOrCancelled = true;
        boolean hasDelivered = false;

        for (ReservationEntity res : freshOrder.getReservations()) {
            if (res.getStatus() == ReservationStatus.COMPLETED) {
                hasDelivered = true;
            }
            // Nếu có bất kỳ món nào vẫn đang PENDING/CONFIRMED/TRANSFERRING
            if (res.getStatus() == ReservationStatus.PENDING ||
                res.getStatus() == ReservationStatus.CONFIRMED ||
                res.getStatus() == ReservationStatus.TRANSFERRING) {
                allCompletedOrCancelled = false;
                break;
            }
        }

        if (allCompletedOrCancelled && hasDelivered) {
            log.info("Tất cả gói hàng cho Order ID {} đã xử lý xong. Cập nhật Order -> COMPLETED.", order.getId());
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        } else if (hasDelivered) {
            // Có 1 gói đã giao, nhưng gói khác còn đang đi (PARTIALLY_DELIVERED)
            // (Bạn cần thêm status PARTIALLY_DELIVERED vào OrderStatus nếu muốn)
            // order.setStatus(OrderStatus.PARTIALLY_DELIVERED);
            // orderRepository.save(order);
        }
    }

    // --- (Các hàm Helper: updateInventoryReserve, deductInventory, restockInventory, createMovement, toDto giữ nguyên) ---
    private void updateInventoryReserve(InventoryLocationEntity loc, VariantEntity variant, int qty, boolean isAdd) {
        InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(loc.getId(), variant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Inventory not found"));

        int currentReserved = inv.getReservedStock() == null ? 0 : inv.getReservedStock();
        if (isAdd) {
            inv.setReservedStock(currentReserved + qty);
        } else {
            inv.setReservedStock(Math.max(0, currentReserved - qty));
        }
        inventoryRepository.save(inv);
    }
    private void deductInventory(InventoryLocationEntity loc, VariantEntity variant, int qty) {
        InventoryEntity inv = inventoryRepository.findByInventoryLocationIdAndVariantId(loc.getId(), variant.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Inventory not found"));
        if (inv.getStock() < qty) throw new AppException(ErrorCode.VALIDATION_ERROR, "Not enough stock");
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
}