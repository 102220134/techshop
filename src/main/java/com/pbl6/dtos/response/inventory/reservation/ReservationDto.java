package com.pbl6.dtos.response.inventory.reservation;

import com.pbl6.dtos.response.StoreDto;
import com.pbl6.dtos.response.inventory.InventoryLocationDto;
import com.pbl6.dtos.response.order.OrderItemDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.enums.ReceiveMethod;
import com.pbl6.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ReservationDto {
    private long id;
    private ReservationStatus status;
    private InventoryLocationDto source;
    private ReceiveMethod receiveMethod;
    private String deliveryAddress;
    private StoreDto destination;
    private long orderId;
    private String sku;
    private String thumbnail;
    private List<VariantDto.AttributeDto> attributes;
    private int quantity;
    private LocalDateTime createdAt;
    private  LocalDateTime updatedAt;
}
