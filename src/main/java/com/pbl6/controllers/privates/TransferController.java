package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.inventory.GR.GRDetailRequest;
import com.pbl6.dtos.request.inventory.GR.ListGRRequest;
import com.pbl6.dtos.request.inventory.transfer.CreateTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.ListTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.TransferDetailRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.GR.GRDto;
import com.pbl6.dtos.response.inventory.GR.GRItemDto;
import com.pbl6.services.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("api/private/inventory/transfers")
@RequiredArgsConstructor
@Tag(name = "Quản lý chuyển kho nội bộ")
public class TransferController {

    private final TransferService transferService;

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping()
    @Operation(summary = "Danh sách phiếu chuyển kho nội bộ", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getTransfers(@ParameterObject ListTransferRequest req) {
        return new ApiResponseDto<>(transferService.getTransfers(req));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết", security = {@SecurityRequirement(name = "bearerAuth")})
    // FIX: Đổi tên hàm từ getGoodsReceipt -> getTransferDetail
    public ApiResponseDto<?> getTransferDetail(
            @PathVariable long id,
            @ParameterObject TransferDetailRequest request) {
        return new ApiResponseDto<>(transferService.getTransferItems(id, request));
    }
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PostMapping()
    @Operation(summary = "tạo", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createTransfer(@RequestBody CreateTransferRequest req) {
        return new ApiResponseDto<>(transferService.createTransfer(req));
    }
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/confirm")
    @Operation(summary = "xác nhận", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> confirmTransfer(
            @PathVariable long id
    ) {
        transferService.confirmTransfer(id);
        return new ApiResponseDto<>();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @Operation(summary = "Bắt đầu xuất kho (Start Transfer)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> startTransfer(@PathVariable Long id) {
        transferService.startTransfer(id);
        return new ApiResponseDto<>();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @Operation(summary = "Hoàn thành nhập kho (Complete Transfer)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> completeTransfer(@PathVariable Long id) {
        transferService.completeTransfer(id);
        return new ApiResponseDto<>();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @Operation(summary = "Xoá phiếu chuyển (Chỉ DRAFT)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> deleteTransfer(@PathVariable Long id) {
        transferService.deleteTransfer(id);
        return new ApiResponseDto<>();
    }
}

