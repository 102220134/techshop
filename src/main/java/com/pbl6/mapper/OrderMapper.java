package com.pbl6.mapper;

import com.pbl6.dtos.request.checkout.OrderRequest;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.response.order.OrderItemDto;
import com.pbl6.entities.*;
import com.pbl6.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderEntity toEntity(UserEntity user, OrderRequest req, StoreEntity store) {
        return OrderEntity.builder()
                .user(user)
                .store(store)
                .receiveMethod(req.getReceiveMethod())
                .paymentMethod(req.getPaymentMethod())
                .discountAmount(BigDecimal.ZERO)
                .status(
                        switch (req.getPaymentMethod()) {
                            case COD -> OrderStatus.PENDING;
                            case BANK -> OrderStatus.AWAITING_PAYMENT;
                            case VNPAY -> OrderStatus.PENDING;
                        }
                )
                .snapshotName(req.getFullName())
                .snapshotPhone(req.getPhone())
                .snapshotLine(req.getLine())
                .snapshotWard(req.getWard())
                .snapshotDistrict(req.getDistrict())
                .snapshotProvince(req.getProvince())
                .build();
    }
    public OrderDto toOrderDto(OrderEntity order) {
        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus().getLabel())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .paymentMethod(order.getPaymentMethod().getLabel())
                .receiveMethod(order.getReceiveMethod().getLabel())
                .deliveryAddress(order.getDeliveryAddress())
                .receiverName(order.getSnapshotName())
                .receiverPhone(order.getSnapshotPhone())
                .items(toOrderItemDtos(order.getItems()))
                .build();
    }
    private List<OrderItemDto> toOrderItemDtos(List<OrderItemEntity> items) {
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
                .specialPrice(item.getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubTotal())
                .attributes((attrs))
                .build();
    }
}

