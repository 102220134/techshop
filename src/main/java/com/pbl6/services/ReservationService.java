package com.pbl6.services;

import com.pbl6.dtos.request.inventory.reservation.ListReservationRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.reservation.ReservationDto;

public interface ReservationService {
    PageDto<ReservationDto> listReservations(ListReservationRequest request);
}
