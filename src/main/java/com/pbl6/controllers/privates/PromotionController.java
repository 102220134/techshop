package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.promotion.PromotionRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.promotion.PromotionResponse;
import com.pbl6.services.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/promotion")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasAuthority('PROMOTION_CREATE')")
    @Operation( security = { @SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> create(@RequestBody PromotionRequest request) {
        return new ApiResponseDto<>(promotionService.createPromotion(request));
    }
    @PreAuthorize("hasAuthority('PROMOTION_UPDATE')")
    @Operation( security = { @SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/{id}")
    public ApiResponseDto<?> update(@PathVariable Long id, @RequestBody PromotionRequest request) {
        return new ApiResponseDto<>(promotionService.updatePromotion(id, request));
    }
    @PreAuthorize("hasAuthority('PROMOTION_READ')")
    @Operation( security = { @SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/{id}")
    public ApiResponseDto<?> getOne(@PathVariable Long id) {
        return new ApiResponseDto<>(promotionService.getById(id));
    }
    @PreAuthorize("hasAuthority('PROMOTION_READ')")
    @Operation( security = { @SecurityRequirement(name = "bearerAuth")})
    @GetMapping
    public ApiResponseDto<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new ApiResponseDto<>(promotionService.getAll(PageRequest.of(page-1, size)));
    }
    @PreAuthorize("hasAuthority('PROMOTION_DELETE')")
    @Operation( security = { @SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
