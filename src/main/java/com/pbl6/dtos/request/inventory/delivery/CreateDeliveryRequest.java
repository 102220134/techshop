package com.pbl6.dtos.request.inventory.delivery;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateDeliveryRequest {
    @NotEmpty(message = "Danh sách reservationIds không được để trống")
    private List<Long> reservationIds;
}
