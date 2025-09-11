package com.pbl6.mapper;

import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class CategoryMapper {
    public CategoryDto toDto(CategoryEntity cat) {
        return new CategoryDto(
                cat.getId(),
                cat.getName(),
                getFullSlug(cat),
                cat.getCategoryType(),
                cat.getLogo(),
                new ArrayList<>()
        );
    }

    private String getFullSlug(CategoryEntity cat) {
        if (cat.getParent() == null || "root".equals(cat.getParent().getCategoryType())) {
            return cat.getSlug();
        }
        return getFullSlug(cat.getParent()) + "/" + cat.getSlug();
    }
}
