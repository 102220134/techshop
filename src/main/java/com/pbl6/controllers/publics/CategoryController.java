package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/main")
    public ApiResponseDto<List<CategoryDto>> getcategoryByRoot() {
        ApiResponseDto<List<CategoryDto>> response = new ApiResponseDto<>();
        response.setData(categoryService.getcategoryByRoot(Boolean.FALSE));
        return response;
    }

    @GetMapping("/{*slug}")
    public CategoryDto getChildrenByType(
            @PathVariable("slug") String slug,
            @RequestParam(required = false) String type
    ) {
        String cleanSlug = slug.startsWith("/") ? slug.substring(1) : slug;
        return categoryService.getChildrenByType(cleanSlug, type, Boolean.FALSE );
    }

}

