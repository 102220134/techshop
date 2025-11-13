package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.inventory.reservation.ListReservationRequest;
import com.pbl6.dtos.request.inventory.transfer.ListTransferRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.services.DeliveryService;
import com.pbl6.services.ReservationService;
import com.pbl6.services.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/private/inventory")
@RequiredArgsConstructor
@Tag(name = "Quản lý yêu cầu vận đơn")
public class ReservationController {
    private final ReservationService reservationService;
    private final TransferService transferService;
    private final DeliveryService deliveryService;

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/reservations")
    @Operation(summary = "Danh sách yeu cau van don", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getReservations(@ParameterObject ListReservationRequest req) {
        return new ApiResponseDto<>(reservationService.listReservations(req));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PostMapping("/reservation/transfer")
    @Operation(summary = "tao van chuyen kho cho don hang nhan tai cua hang", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createTransfer(
            @RequestBody List<Long> reservationIds
            ) {
        return new ApiResponseDto<>(transferService.createTransfer(reservationIds));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PostMapping("/reservation/{id}/delivery")
    @Operation(summary = "tao van chuyen kho cho don hang giao hang tan noi (chua lam xong)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createDelivery(
            @PathVariable long id
    ) {
        return new ApiResponseDto<>();
    }


}
