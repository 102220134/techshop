package com.pbl6.mapper;

import com.pbl6.dtos.response.order.OrderDetailDto;
import com.pbl6.dtos.response.order.UserOrderDetailDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.response.order.OrderItemDto;
import com.pbl6.entities.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderDto toDto(OrderEntity order) {
        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .paymentMethod(order.getPaymentMethod())
                .receiveMethod(order.getReceiveMethod())
                .items(toOrderItemDtos(order.getItems()))
                .build();
    }
    private List<OrderItemDto> toOrderItemDtos(Set<OrderItemEntity> items) {
        if (items == null) return List.of();
        return items.stream().map(this::toOrderItemDto).collect(Collectors.toList());
    }

    private OrderItemDto toOrderItemDto(OrderItemEntity item) {
        VariantEntity variant = item.getVariant();
        List<VariantDto.AttributeDto> attrs = variant.getVariantAttributeValues().stream()
                .map(vav -> VariantDto.AttributeDto.builder()
                        .code(vav.getAttribute().getCode())
                        .label(vav.getAttribute().getLabel())
                        .value(vav.getAttributeValue().getLabel())
                        .build())
                .toList();
        return OrderItemDto.builder()
                .id(item.getId())
                .name(variant.getProduct().getName())
                .sku(variant.getSku())
                .thumbnail(variant.getThumbnail())
                .price(variant.getPrice())
                .finalPrice(item.getFinalPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .attributes((attrs))
                .build();
    }

    public UserOrderDetailDto toUserOrderDetailDto(OrderEntity order){
        BigDecimal grossAmount = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal directDiscount = order.getOrderItems().stream()
                .map(item -> item.getDiscountAmount().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4️⃣ Map ra OrderDetailDto
        return UserOrderDetailDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .grossAmount(grossAmount)
                .directDiscount(directDiscount)
                .voucherDiscount(order.getVoucherDiscount())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .remainingAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .paymentMethod(order.getPaymentMethod())
                .receiveMethod(order.getReceiveMethod())
                .receiverName(order.getSnapshot().getName())
                .receiverPhone(order.getSnapshot().getPhone())
                .receiverAddress(
                        switch (order.getReceiveMethod()) {
                            case DELIVERY -> order.getSnapshot().getDeliveryAddress();
                            case PICKUP -> order.getStore() != null
                                    ? order.getStore().getDisplayAddress()
                                    : "Tại cửa hàng";
                        }
                )
                .items(toOrderItemDtos(order.getItems()))
                .build();
    }

    public OrderDetailDto toOrderDetailDto(OrderEntity order){
        BigDecimal grossAmount = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal directDiscount = order.getOrderItems().stream()
                .map(item -> item.getDiscountAmount().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4️⃣ Map ra OrderDetailDto
        return OrderDetailDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .grossAmount(grossAmount)
                .directDiscount(directDiscount)
                .voucherDiscount(order.getVoucherDiscount())
                .totalAmount(order.getTotalAmount())
                .paidAmount(order.getPaidAmount())
                .remainingAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .paymentMethod(order.getPaymentMethod())
                .receiveMethod(order.getReceiveMethod())
                .receiverName(order.getSnapshot().getName())
                .receiverPhone(order.getSnapshot().getPhone())
                .receiverAddress(
                        switch (order.getReceiveMethod()) {
                            case DELIVERY -> order.getSnapshot().getDeliveryAddress();
                            case PICKUP -> order.getStore() != null
                                    ? order.getStore().getDisplayAddress()
                                    : "Tại cửa hàng";
                        }
                )
                .items(toOrderItemDtos(order.getItems()))
                .build();
    }

}