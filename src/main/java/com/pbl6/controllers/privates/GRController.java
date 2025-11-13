package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.inventory.GR.CreateGRRequest;
import com.pbl6.dtos.request.inventory.GR.GRDetailRequest;
import com.pbl6.dtos.request.inventory.GR.ListGRRequest;
import com.pbl6.dtos.request.inventory.SearchInventoryRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.GR.GRDto;
import com.pbl6.dtos.response.inventory.GR.GRItemDto;
import com.pbl6.dtos.response.inventory.InventoryDto;
import com.pbl6.enums.InventoryLocationType;
import com.pbl6.services.GRService;
import com.pbl6.services.InventoryLocationService;
import com.pbl6.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/inventory")
@RequiredArgsConstructor
@Tag(name = "Quản lý nhập hàng")
public class GRController {
    private final GRService gRService;

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/receipts")
    @Operation(summary = "Danh sách phiếu nhập kho", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<PageDto<GRDto>> getGoodsReceipts(@ParameterObject ListGRRequest req) {
        return new ApiResponseDto<>(gRService.getGRs(req));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/receipt/{id}")
    @Operation(summary = "Xem chi tiết phiếu nhập kho", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<PageDto<GRItemDto>> getGoodsReceipt(
            @PathVariable long id,
            @ParameterObject GRDetailRequest request) {
        return new ApiResponseDto<>(gRService.getGRItems(id, request));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PostMapping("/receipt")
    @Operation(summary = "Tạo phiếu nhập kho mới (Draft)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createGoodsReceipt(@Valid @RequestBody CreateGRRequest request) {
        return new ApiResponseDto<>(gRService.createGoodsReceipt(request));
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @DeleteMapping("/receipt/{id}")
    @Operation(summary = "Xoá phiếu nhập (chỉ DRAFT)", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> deleteGoodsReceipt(@PathVariable Long id) {
        gRService.deleteGoodsReceipt(id);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @PutMapping("/receipt/{id}/completed")
    @Operation(summary = "Hoàn thành nhập kho", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<Void> completeGoodsReceipt(@PathVariable Long id) {
        gRService.completeGoodsReceipt(id);
        return new ApiResponseDto<>();
    }
}