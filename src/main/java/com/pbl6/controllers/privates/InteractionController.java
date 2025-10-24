package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.review.CreateReviewRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.services.LikeProductService;
import com.pbl6.services.ReviewService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/interaction")
@RequiredArgsConstructor
@Tag(name = "Tương tác của user -> product")
@PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
public class InteractionController {

    private final ReviewService reviewService;
    private final LikeProductService likeProductService;
    private final AuthenticationUtil authenticationUtil;

    @PostMapping(value = "review/{productId}", consumes = {"multipart/form-data"})
    @Operation(summary = "Đánh giá",security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> createReview(
            @PathVariable Long productId,
            @Valid @ModelAttribute CreateReviewRequest request
    ) {
        reviewService.createOrUpdateReview(productId,request);
        return new ApiResponseDto<>();
    }

    @PostMapping("like/{productId}")
    @Operation(summary = "Like product", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<?> likeProduct(
            @PathVariable Long productId
    ) {
        Long userId = authenticationUtil.getCurrentUserId();
        likeProductService.likeProduct(userId, productId);
        return new ApiResponseDto<>();
    }
    @DeleteMapping("unlike/{productId}")
    @Operation(summary = "Bỏ Like product", security = { @SecurityRequirement(name = "bearerAuth") })
    public ApiResponseDto<?> unlikeProduct(
            @PathVariable Long productId
    ) {
        Long userId = authenticationUtil.getCurrentUserId();
        likeProductService.unlikeProduct(userId, productId);
        return new ApiResponseDto<>();
    }
}
