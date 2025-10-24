package com.pbl6.services;

import com.pbl6.dtos.request.category.CategoryCreateRequest;
import com.pbl6.dtos.request.category.CategoryUpdateRequest;
import com.pbl6.dtos.response.BreadcrumbDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;

import java.util.List;

public interface CategoryService {
    CategoryDto getChildrenByType(String slugPath, String type);
    List<CategoryDto> getCategoryByRoot();
    CategoryEntity resolveBySlugPath(String slug);
    public BreadcrumbDto getBreadcrumbByProductSlug(String productSlug);
    List<CategoryDto> getCategoryTree(Boolean isOnlyActive);
    CategoryDto createCategory(CategoryCreateRequest request);
    CategoryDto updateCategory(Long id, CategoryUpdateRequest request);
    void deleteCategory(Long id);
}
