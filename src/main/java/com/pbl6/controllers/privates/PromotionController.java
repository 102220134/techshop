package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.promotion.PromotionRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.promotion.PromotionResponse;
import com.pbl6.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/promotion")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    @PostMapping
    public ApiResponseDto<?> create(@RequestBody PromotionRequest request) {
        return new ApiResponseDto<>(promotionService.createPromotion(request));
    }

    @PutMapping("/{id}")
    public ApiResponseDto<?> update(@PathVariable Long id, @RequestBody PromotionRequest request) {
        return new ApiResponseDto<>(promotionService.updatePromotion(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponseDto<?> getOne(@PathVariable Long id) {
        return new ApiResponseDto<>(promotionService.getById(id));
    }

    @GetMapping
    public ApiResponseDto<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new ApiResponseDto<>(promotionService.getAll(PageRequest.of(page, size)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
