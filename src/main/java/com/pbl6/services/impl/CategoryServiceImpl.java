package com.pbl6.services.impl;

import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.CategoryMapper;
import com.pbl6.repositories.CategoryRepository;
import com.pbl6.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper cateMapper;

    private Long getRootId() {
        // Nếu bạn dùng "default-category" là root:
        return categoryRepository.findBySlugAndParentId("default-category", null)
                .map(CategoryEntity::getId)
                .orElseThrow(()-> new AppException(ErrorCode.DATA_NOT_FOUND));
    }

    public CategoryEntity resolveBySlugPath(String slugPath) {
        String[] parts = slugPath.split("/");
        Long parentId = getRootId(); // id của root (default-category) hoặc null nếu root real = null

        CategoryEntity current = null;
        for (String slug : parts) {
            current = categoryRepository.findBySlugAndParentId(slug, parentId)
                    .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
            parentId = current.getId();
        }
        return current;
    }


    @Override
    public CategoryDto getChildrenByType(String slugPath, String type , Boolean includeInactive) {
        boolean includeAll = Boolean.TRUE.equals(includeInactive);
        CategoryEntity node = resolveBySlugPath(slugPath);

        if (!includeAll && !Boolean.TRUE.equals(node.getIsActive())) {
            throw new AppException(ErrorCode.DATA_NOT_FOUND);
        }

        List<CategoryEntity> children = (type == null || type.isBlank())
                ? categoryRepository.findByParentId(node.getId())
                : categoryRepository.findByParentIdAndCategoryType(node.getId(), type);

        if (!includeAll) {
            children = children.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .toList();
        }

        CategoryDto dto = cateMapper.toDto(node);
        dto.setChildren(children.stream().map(cateMapper::toDto).toList());
        return dto;
    }

    @Override
    public List<CategoryDto> getcategoryByRoot(Boolean includeInactive) {
        List<CategoryEntity> categoryEntities = categoryRepository.findByParentId(getRootId());
        return (includeInactive == null || !includeInactive) ?
                categoryEntities.stream()
                        .filter(CategoryEntity::getIsActive)
                        .map(cateMapper::toDto)
                        .toList():
                categoryEntities.stream()
                        .map(cateMapper::toDto)
                        .toList();
    }
}
