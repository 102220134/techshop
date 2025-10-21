package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.services.CategoryService;
import com.pbl6.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/inventory")
@RequiredArgsConstructor
@Tag(name = "Kiểm tra stock")
public class InventoryController {
    private final InventoryService inventoryService;
    @GetMapping("/{variantId}")
    @Operation(description = "Kiểm tra tồn kho của 1 variant")
    public ApiResponseDto<?> checkInStock(
            @PathVariable Long variantId,

            @RequestParam(required = true) Integer quantity) {

        return new ApiResponseDto<>(inventoryService.isInStock(variantId,quantity));
    }

}

