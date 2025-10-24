package com.pbl6.mapper;

import com.pbl6.dtos.request.category.CategoryCreateRequest;
import com.pbl6.dtos.response.BreadcrumbDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.utils.CloudinaryUtil;
import com.pbl6.utils.EntityUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class CategoryMapper {
    private final CloudinaryUtil cloudinaryUtil;
    private final EntityUtil entityUtil;

    public CategoryMapper(CloudinaryUtil cloudinaryUtil, EntityUtil entityUtil) {
        this.cloudinaryUtil = cloudinaryUtil;
        this.entityUtil = entityUtil;
    }

    public CategoryDto toDto(CategoryEntity cat) {
        return new CategoryDto(
                cat.getId(),
                cat.getName(),
                getFullSlug(cat),
                cat.getCategoryType(),
                cat.getLogo(),
                null,
                new ArrayList<>()
        );
    }

    private String getFullSlug(CategoryEntity cat) {
        if (cat.getParent() == null || "root".equals(cat.getParent().getCategoryType())) {
            return cat.getSlug();
        }
        return getFullSlug(cat.getParent()) + "/" + cat.getSlug();
    }

    public BreadcrumbDto.BreadcrumbItem toBreadcrumbItem(CategoryEntity entity) {
        if (entity == null) return null;
        return new BreadcrumbDto.BreadcrumbItem(entity.getName(), getFullSlug(entity));
    }

}
