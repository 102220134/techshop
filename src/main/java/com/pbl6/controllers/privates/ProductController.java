package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.order.MyOrderRequest;
import com.pbl6.dtos.request.product.AdminSearchProductRequest;
import com.pbl6.dtos.request.product.CreateProductRequest;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.request.product.UpdateProductRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.order.OrderDto;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.UserEntity;
import com.pbl6.services.OrderService;
import com.pbl6.services.ProductService;
import com.pbl6.utils.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/product")
@RequiredArgsConstructor
@Tag(name = "Quản lý sản phẩm")
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    @Operation(summary = "Lọc sản phẩm", security = { @SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/filter")
    public ApiResponseDto<PageDto<ProductDto>> filterProducts(
            @Schema(defaultValue = "3")
            @ParameterObject AdminSearchProductRequest req
    ) {
        PageDto<ProductDto> pageDto = new PageDto<>(productService.filterProducts(req));
        return new ApiResponseDto<>(pageDto);
    }

    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @Operation(summary = "Thêm sản phẩm", security = { @SecurityRequirement(name = "bearerAuth")})
    @PostMapping(value  = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<ProductDetailDto> createProduct(
            @Valid
            @ModelAttribute CreateProductRequest req
    ) {
        return new ApiResponseDto<>(productService.createProduct(req));
    }

    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    @Operation(summary = "Thêm sản phẩm", security = { @SecurityRequirement(name = "bearerAuth")})
    @PutMapping(value  = "/update/{productId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<ProductDetailDto> updateProduct(
            @PathVariable Long productId,
            @Valid
            @ModelAttribute UpdateProductRequest req
    ) {
        return new ApiResponseDto<>(productService.updateProduct(productId,req));
    }
}
