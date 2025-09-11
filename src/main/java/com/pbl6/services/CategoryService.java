package com.pbl6.services;

import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;

import java.util.List;

public interface CategoryService {
    CategoryDto getChildrenByType(String slugPath, String type, Boolean includeInactive);
    List<CategoryDto> getcategoryByRoot(Boolean includeInactive);
    CategoryEntity resolveBySlugPath(String slug);
}
