package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.inventory.SearchInventoryRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.InventoryDto;
import com.pbl6.enums.InventoryLocationType;
import com.pbl6.services.GRService;
import com.pbl6.services.InventoryLocationService;
import com.pbl6.services.InventoryService;
import com.pbl6.services.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/private/inventory")
@RequiredArgsConstructor
@Tag(name = "Quản lý kho")
public class InventoryController {
    private final InventoryService inventoryService;
    private final GRService gRService;
    private final InventoryLocationService inventoryLocationService;
    private final StockMovementService stockMovementService;

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/supplier")
    @Operation(summary = "Danh sách nhà cung cấp", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getSupplier() {
        return new ApiResponseDto<>(gRService.getSupplier());
    }

    // ===================== WAREHOUSE =====================

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping("/location")
    @Operation(summary = "Danh sách kho/cửa hàng", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getWarehouses(@RequestParam(required = false) InventoryLocationType type) {
        return new ApiResponseDto<>(inventoryLocationService.getInventoryLocation(type));
    }

    // ===================== INVENTORY =====================

    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    @GetMapping
    @Operation(summary = "Xem tồn kho theo kho hoặc sản phẩm", security = {@SecurityRequirement(name = "bearerAuth")})
    public PageDto<InventoryDto> searchInventory(@ParameterObject SearchInventoryRequest req) {
        return inventoryService.searchInventory(req);
    }

    @PreAuthorize("hasAuthority('INVENTORY_HISTORY')")
    @GetMapping("/history")
    @Operation( security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> getHistory(
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Long locationId)
    {

        int page = 1; // page index, bắt đầu từ 0
        int size = 20;

        Pageable pageable = PageRequest.of(
                page-1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return new ApiResponseDto<>(stockMovementService.getHistory(variantId, locationId, pageable));
    }
}
