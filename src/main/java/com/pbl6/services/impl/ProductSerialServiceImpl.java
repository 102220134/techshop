package com.pbl6.services.impl;

import com.pbl6.entities.*;
import com.pbl6.enums.ProductSerialStatus;
import com.pbl6.enums.ReceiveMethod;
import com.pbl6.enums.ReservationStatus;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.ProductSerialRepository;
import com.pbl6.repositories.ReservationRepository;
import com.pbl6.services.ProductSerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSerialServiceImpl implements ProductSerialService {

    private final ProductSerialRepository serialRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public List<ProductSerialEntity> reserveSerial( OrderItemEntity orderItem, InventoryLocationEntity location) {

        List<ProductSerialEntity> serials = serialRepository.findByVariantIdAndInventoryLocationIdAndStatus(
                orderItem.getVariant().getId(),
                location.getId(),
                ProductSerialStatus.IN_STOCK
        );

        if (serials.size() < orderItem.getQuantity()) {
            log.error("Product serial không đủ để giữ hàng");
            throw new AppException(ErrorCode.BUSINESS_RULE_VIOLATION,"oversell");
        }

        // ✅ Kiểm tra nếu là đơn PICKUP và location trùng với store location của order
        OrderEntity order = orderItem.getOrder();
        ReservationStatus reservationStatus = ReservationStatus.DRAFT; // mặc định

        if (order.getReceiveMethod() == ReceiveMethod.PICKUP && order.getStore() != null) {
            // tìm location của cửa hàng
            InventoryLocationEntity storeLocation = order.getStore().getInventoryLocation();
            if (storeLocation != null && storeLocation.getId().equals(location.getId())) {
                // hàng đang ở đúng cửa hàng mà khách sẽ tới lấy
                reservationStatus = ReservationStatus.AVAILABLE;
            }
        }

        ReservationEntity reservation = new ReservationEntity();
        reservation.setOrder(orderItem.getOrder());
        reservation.setOrderItem(orderItem);
        reservation.setQuantity(orderItem.getQuantity());
        reservation.setLocation(location);
        reservation.setStatus(reservationStatus);

        ReservationEntity finalReservation = reservationRepository.save(reservation);
        List<ProductSerialEntity> updatedSerials = serials.stream()
                .limit(orderItem.getQuantity())
                .peek(ps -> {
                    ps.setReservation(finalReservation);
                    ps.setStatus(ProductSerialStatus.RESERVED);
                })
                .toList();

        return serialRepository.saveAll(updatedSerials);
    }

}
