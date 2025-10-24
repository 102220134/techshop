package com.pbl6.controllers.privates;

import com.pbl6.dtos.request.category.CategoryCreateRequest;
import com.pbl6.dtos.request.category.CategoryUpdateRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/category")
@RequiredArgsConstructor
@Tag(name = "Quản lý danh mục")
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    @GetMapping("/tree")
    @Operation(description = "Cây danh mục", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponseDto<List<CategoryDto>> getCategoryTree(@RequestParam(required = false) Boolean isOnlyActive) {
        ApiResponseDto<List<CategoryDto>> response = new ApiResponseDto<>();
        response.setData(categoryService.getCategoryTree(isOnlyActive));
        return response;
    }

    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDto> createCategory(@ModelAttribute @Valid CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @ModelAttribute CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
