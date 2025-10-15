package com.pbl6.controllers.publics;

import com.pbl6.annotations.ByPassJWT;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/category")
@RequiredArgsConstructor
@Tag(name = "Danh mục")
public class CategoryController {
    private final CategoryService categoryService;
    @GetMapping("/main")
    @Operation(description = "Danh mục chính")
    public ApiResponseDto<List<CategoryDto>> getcategoryByRoot() {
        ApiResponseDto<List<CategoryDto>> response = new ApiResponseDto<>();
        response.setData(categoryService.getcategoryByRoot(Boolean.FALSE));
        return response;
    }

    @GetMapping("/{*slug}")
    public ApiResponseDto<CategoryDto> getChildrenByType(
            @PathVariable("slug") String slug,
            @RequestParam(required = false) String type
    ) {
        String cleanSlug = slug.startsWith("/") ? slug.substring(1) : slug;
        ApiResponseDto<CategoryDto> response = new ApiResponseDto<>();
        response.setData(categoryService.getChildrenByType(cleanSlug, type, Boolean.FALSE));
        return response;
    }

}

