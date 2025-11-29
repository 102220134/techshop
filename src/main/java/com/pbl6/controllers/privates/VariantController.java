package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.product.*;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.services.ProductService;
import com.pbl6.services.VariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/private/variant")
@RequiredArgsConstructor
@Tag(name = "Quản lý variant")
public class VariantController {

    private final VariantService variantService;

    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Tìm kiếm variant", security = { @SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/search")
    public ApiResponseDto<PageDto<VariantDto>> getVariant(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String keyword
    ) {
        return new ApiResponseDto<>(variantService.searchVariant(page, size, keyword));
    }


    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @Operation(summary = "thông tin variant", security = { @SecurityRequirement(name = "bearerAuth")})
    @PostMapping(value  = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<VariantDto> addVariant(
            @Valid
            @ModelAttribute CreateVariantRequest request
            ) {
        return new ApiResponseDto<>(variantService.createVariant(request));
    }

    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "thông tin variant", security = { @SecurityRequirement(name = "bearerAuth")})
    @PutMapping(value  = "/edit/{variantId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<VariantDto> editVariant(
            @PathVariable long variantId,
            @Valid
            @ModelAttribute UpdateVariantRequest request
    ) {
        return new ApiResponseDto<>(variantService.editVariant(variantId,request));
    }

//    @PreAuthorize("hasAuthority('PRODUCT_READ')")
//    @Operation(summary = "Chi tiết sản phẩm", security = { @SecurityRequirement(name = "bearerAuth")})
//    @GetMapping("/{productId}")
//    public ApiResponseDto<ProductDetailDto> getProductDetail(
//            @PathVariable Long productId
//    ) {
//
//        return new ApiResponseDto<>(productService.getProductDetail(productId));
//    }
//
//    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
//    @Operation(summary = "Thêm sản phẩm", security = { @SecurityRequirement(name = "bearerAuth")})
//    @PostMapping(value  = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApiResponseDto<ProductDetailDto> createProduct(
//            @Valid
//            @ModelAttribute CreateProductRequest req
//    ) {
//        return new ApiResponseDto<>(productService.createProduct(req));
//    }
//
//    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
//    @Operation(summary = "Thêm sản phẩm", security = { @SecurityRequirement(name = "bearerAuth")})
//    @PutMapping(value  = "/update/{productId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApiResponseDto<ProductDetailDto> updateProduct(
//            @PathVariable Long productId,
//            @Valid
//            @ModelAttribute UpdateProductRequest req
//    ) {
//        return new ApiResponseDto<>(productService.updateProduct(productId,req));
//    }
}
