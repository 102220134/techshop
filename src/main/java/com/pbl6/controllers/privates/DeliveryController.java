package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.inventory.delivery.CreateDeliveryRequest;
import com.pbl6.dtos.request.inventory.delivery.DeliveryDetailRequest;
import com.pbl6.dtos.request.inventory.delivery.ListDeliveryRequest;
import com.pbl6.dtos.request.inventory.transfer.CreateTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.ListTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.TransferDetailRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.enums.DeliveryStatus;
import com.pbl6.enums.TransferStatus; // Import enum
import com.pbl6.services.DeliveryService;
import com.pbl6.services.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/inventory/delivery")
@RequiredArgsConstructor
@Tag(name = "Giao hàng") // Đổi tên Tag cho rõ
public class DeliveryController {


    private final DeliveryService deliveryService;

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PostMapping()
    @Operation(summary = "Tạo phiếu giao hàng", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> create(@RequestBody CreateDeliveryRequest request) {
        return new ApiResponseDto<>(deliveryService.createDelivery(request.getReservationIds()));
    }


    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/pickup")
    @Operation(summary = "Bắt đầu lấy hàng", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> pickUp(@PathVariable long id) {
        deliveryService.updateDeliveryStatus(id, DeliveryStatus.PICKED_UP);
        return new ApiResponseDto<>();
    }
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/delivering")
    @Operation(summary = "đang giao", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> delivering(@PathVariable long id) {
        deliveryService.updateDeliveryStatus(id, DeliveryStatus.DELIVERING);
        return new ApiResponseDto<>();
    }
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/delivered")
    @Operation(summary = "đã giao", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> delivered(@PathVariable long id) {
        deliveryService.updateDeliveryStatus(id, DeliveryStatus.DELIVERED);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/canceled")
    @Operation(summary = "huỷ", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> canceled(@PathVariable long id) {
        deliveryService.updateDeliveryStatus(id,DeliveryStatus.CANCELLED);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/failed")
    @Operation(summary = "giao thất bại", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> failed(@PathVariable long id) {
        deliveryService.updateDeliveryStatus(id,DeliveryStatus.FAILED);
        return new ApiResponseDto<>();
    }
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/list")
    @Operation(summary = "Danh sách phiếu giao hàng", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getTransfers(@ParameterObject ListDeliveryRequest req) {
        return new ApiResponseDto<>(deliveryService.getDelivery(req));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/detail/{id}")
    @Operation(summary = "Xem chi tiết phiếu chuyển", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getTransferDetail(
            @PathVariable long id,
            @ParameterObject DeliveryDetailRequest request) {
        return new ApiResponseDto<>(deliveryService.getDeliveryItems(id, request));
    }
}