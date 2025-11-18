package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.inventory.transfer.CreateTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.ListTransferRequest;
import com.pbl6.dtos.request.inventory.transfer.TransferDetailRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.enums.TransferStatus; // Import enum
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
@Tag(name = "Quản lý Chuyển kho") // Đổi tên Tag cho rõ
public class TransferController {

    private final TransferService transferService;

    // (Các hàm Get và Create giữ nguyên)

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping()
    @Operation(summary = "Danh sách phiếu chuyển kho", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getTransfers(@ParameterObject ListTransferRequest req) {
        return new ApiResponseDto<>(transferService.getTransfers(req));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết phiếu chuyển", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getTransferDetail(
            @PathVariable long id,
            @ParameterObject TransferDetailRequest request) {
        return new ApiResponseDto<>(transferService.getTransferItems(id, request));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PostMapping()
    @Operation(summary = "Tạo phiếu chuyển kho (Thủ công)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createTransfer(@RequestBody CreateTransferRequest req) {
        return new ApiResponseDto<>(transferService.createTransfer(req));
    }

    // --- CÁC HÀM UPDATE TRẠNG THÁI (ĐÃ REFACTOR) ---

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/confirm")
    @Operation(summary = "Xác nhận phiếu (DRAFT -> CONFIRMED)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> confirmTransfer(@PathVariable long id) {
        transferService.updateTransferStatus(id, TransferStatus.CONFIRMED);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Xác nhận phiếu (DRAFT -> CANCELD)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> cancelTransfer(@PathVariable long id) {
        transferService.updateTransferStatus(id, TransferStatus.CANCELLED);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/start")
    @Operation(summary = "Bắt đầu xuất kho (CONFIRMED -> TRANSFERRING)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> startTransfer(@PathVariable Long id) {
        transferService.updateTransferStatus(id, TransferStatus.TRANSFERRING);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/{id}/complete")
    @Operation(summary = "Hoàn thành nhập kho (TRANSFERRING -> COMPLETED)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> completeTransfer(@PathVariable Long id) {
        transferService.updateTransferStatus(id, TransferStatus.COMPLETED);
        return new ApiResponseDto<>();
    }

    // (Hàm Delete giữ nguyên logic cũ vì nó là "xóa" chứ không phải "cập nhật")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @Operation(summary = "Xoá phiếu chuyển (Chỉ DRAFT)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> deleteTransfer(@PathVariable Long id) {
        transferService.deleteTransfer(id);
        return new ApiResponseDto<>();
    }
}