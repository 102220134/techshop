package com.pbl6.dtos.request.inventory.reservation;

import com.pbl6.enums.ReceiveMethod;
import com.pbl6.enums.ReservationStatus;
import lombok.Data;

@Data
public class ListReservationRequest {
    private ReservationStatus status;
    private ReceiveMethod receiveMethod;
    private Long storeId;
    private String order = "createdAt";
    private String dir = "desc";
    private int page = 1;
    private int size = 20;
}
