package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/inventory")
@RequiredArgsConstructor
@Tag(name = "Kiểm tra stock")
public class PublicInventoryController {
    private final InventoryService inventoryService;
    @GetMapping("/check-stock/{variantId}")
    @Operation(description = "Kiểm tra tồn kho của 1 variant")
    public ApiResponseDto<?> checkInStock(
            @PathVariable Long variantId,

            @RequestParam(required = true) Integer quantity) {

        return new ApiResponseDto<>(inventoryService.isInStock(variantId,quantity));
    }

}

