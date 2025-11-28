package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.attribute.AddAttributeRequest;
import com.pbl6.dtos.request.attribute.EditAttributeRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.AttributeDto;
import com.pbl6.services.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/private/attribute")
@RequiredArgsConstructor
@Tag(name = "Thuộc tính của sản phẩm", description = "Thuộc tính gồm thuộc tính dùng option hoặc dùng filter")
public class AttributeController {
    private final AttributeService filterService;

    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @GetMapping("{categoryId}/filter")
    @Operation(summary = "Khi thêm/sửa sản phẩm -> chọn category -> gọi api này để lấy khung filter sản phẩm",security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<List<AttributeDto>> getFiltersByCateSlug(@PathVariable("categoryId") Long categoryId) {
        return new ApiResponseDto<>(filterService.getAllAttributeFilter(categoryId));
    }

    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @GetMapping("option")
    @Operation(summary = "Dùng để thêm thuộc tính option cho lúc tạo/sửa sản phẩm", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<List<AttributeDto>> getAllOption() {
        ApiResponseDto<List<AttributeDto>> response = new ApiResponseDto<>();
        response.setData(filterService.getAllAttributeOption());
        return response;
    }

    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @PostMapping("/add-value")
    @Operation(summary = "Thêm value cho filter/option", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> addFilterValue(@RequestBody AddAttributeRequest attributeRequest) {
        filterService.addAttributeValue(attributeRequest);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @PutMapping("/edit-value/{valueId}")
    @Operation(summary = "Edit value ", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> editFilterValue(
            @PathVariable Long valueId,
            @RequestBody EditAttributeRequest attributeRequest) {
        filterService.editAttributeValue(valueId, attributeRequest);
        return new ApiResponseDto<>();
    }

    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    @DeleteMapping("/delete-value/{valueId}")
    @Operation(summary = "xoá value ", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<?> deleteFilterValue(
            @PathVariable Long valueId) {
        filterService.deleteAttributeValue(valueId);
        return new ApiResponseDto<>();
    }
}

