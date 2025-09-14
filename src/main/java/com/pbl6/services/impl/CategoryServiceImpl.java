package com.pbl6.services.impl;

import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.CategoryMapper;
import com.pbl6.repositories.CategoryRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper cateMapper;
    private final EntityUtil entityUtil;

    @Value("${default_root_category}")
    private  String defaultRootCategory;


    private Long getRootId() {
        return categoryRepository.findBySlugAndParentId(defaultRootCategory, null)
                .map(CategoryEntity::getId)
                .orElseThrow(()-> new AppException(ErrorCode.DATA_NOT_FOUND));
    }

    public CategoryEntity resolveBySlugPath(String slugPath) {
        String[] parts = slugPath.split("/");
        Long parentId = getRootId();

        CategoryEntity current = null;
        for (String slug : parts) {
            current = entityUtil.ensureExists(categoryRepository.findBySlugAndParentId(slug, parentId));
            parentId = current.getId();
        }
        return current;
    }


    @Override
    public CategoryDto getChildrenByType(String slugPath, String type , Boolean includeInactive) {

        CategoryEntity node = resolveBySlugPath(slugPath);
        entityUtil.ensureActive(node,includeInactive);

        List<CategoryEntity> children = (type == null || type.isBlank())
                ? categoryRepository.findByParentId(node.getId())
                : categoryRepository.findByParentIdAndCategoryType(node.getId(), type);

        if (!includeInactive) {
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
        return (!includeInactive) ?
                categoryEntities.stream()
                        .filter(CategoryEntity::getIsActive)
                        .map(cateMapper::toDto)
                        .toList():
                categoryEntities.stream()
                        .map(cateMapper::toDto)
                        .toList();
    }
}
