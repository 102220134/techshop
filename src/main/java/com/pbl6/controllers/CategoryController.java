package com.pbl6.controllers;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/main")
    public ApiResponseDto<List<CategoryDto>> getcategoryByRoot(
            @RequestParam(required = false) Boolean includeInactive
    ) {
        ApiResponseDto<List<CategoryDto>> response = new ApiResponseDto<>();
        response.setData(categoryService.getcategoryByRoot(includeInactive));
        return response;
    }

//     Lấy một nhánh theo slug path (1 hoặc nhiều tầng)
//     Ví dụ: /api/categories/mobile  hoặc /api/categories/mobile/apple
//    @GetMapping("/{slugPath:.+}")
//    public CategoryDto getCategoryBranch(@PathVariable("slugPath") String slugPath) {
//        return categoryService.getBranchBySlugPath(slugPath);
//    }
//
//     Lấy children của một nhánh + lọc theo type (brand/feature/...)
//     Ví dụ: /api/categories/mobile/children?type=brand
//            /api/categories/mobile/apple/children?type=feature
    @GetMapping("/{*slugPath}")
    public CategoryDto getChildrenByType(
            @PathVariable("slugPath") String slugPath,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean includeInactive
    ) {
        String cleanSlug = slugPath.startsWith("/") ? slugPath.substring(1) : slugPath;
        return categoryService.getChildrenByType(cleanSlug, type, includeInactive );
    }

//    @GetMapping("/breadcrumb/{*slugPath}")
//    public CategoryDto getBreadcrumb(
//            @PathVariable("slugPath") String slugPath,
//            @RequestParam(required = false) Boolean includeInactive
//    ) {
//        String cleanSlug = slugPath.startsWith("/") ? slugPath.substring(1) : slugPath;
//        return categoryService.getChildrenByType(cleanSlug, includeInactive );
//    }
}

