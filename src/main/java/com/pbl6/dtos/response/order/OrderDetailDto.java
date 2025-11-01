package com.pbl6.dtos.response.order;

import com.pbl6.enums.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
public class OrderDetailDto {
    private Long id;
    private OrderStatus status;
    private BigDecimal grossAmount;
    private BigDecimal directDiscount;
    private BigDecimal voucherDiscount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PaymentMethod paymentMethod;
    private ReceiveMethod receiveMethod;
    private String receiverAddress;
    private String receiverName;
    private String receiverPhone;
    private List<OrderItemDto> items;
    List<SourceGoods> sourceGoods;

    @Getter
    @Builder
    static public class SourceGoods {
        private String sku;
        private int quantity;
        private String name;
        private InventoryLocationType type;
        private String address;
        private ReservationStatus status;
        private String transferStatus;
    }
}
