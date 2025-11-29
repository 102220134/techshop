package com.pbl6.dtos.request.inventory.delivery;

import com.pbl6.enums.DeliveryStatus;
import com.pbl6.enums.TransferStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListDeliveryRequest {
    private DeliveryStatus status;
    private String order = "createdAt";
    private String dir = "desc";
    private int page = 1;
    private int size = 20;
}
