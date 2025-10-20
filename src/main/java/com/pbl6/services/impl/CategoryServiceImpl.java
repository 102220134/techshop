package com.pbl6.services.impl;

import com.pbl6.dtos.response.BreadcrumbDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.ProductEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.CategoryMapper;
import com.pbl6.repositories.CategoryRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper cateMapper;
    private final EntityUtil entityUtil;
    private final ProductRepository productRepository;

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
        dto.setBreadcrumb(new BreadcrumbDto(buildBreadcrumbItems(node.getParent()),cateMapper.toBreadcrumbItem(node)));
        return dto;
    }

    @Override
    public List<CategoryDto> getCategoryByRoot(Boolean includeInactive) {
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

//        return new BreadcrumbDto(buildBreadcrumbItems(category.getParent()), cateMapper.toBreadcrumbItem(category));

    @Override
    @Transactional(readOnly = true)
    public BreadcrumbDto getBreadcrumbByProductSlug(String productSlug) {
        ProductEntity product = entityUtil.ensureExists(productRepository.findBySlug(productSlug));

        BreadcrumbDto.BreadcrumbItem current =
                new BreadcrumbDto.BreadcrumbItem(product.getName(), product.getSlug());

        if (product.getCategories() == null || product.getCategories().isEmpty()) {
            return new BreadcrumbDto(null, current);
        }

        CategoryEntity deepestCategory = product.getCategories().stream()
                .max(Comparator.comparingInt(CategoryEntity::getLevel))
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        List<BreadcrumbDto.BreadcrumbItem> items = buildBreadcrumbItems(deepestCategory);

        return new BreadcrumbDto(items, current);
    }

    private List<BreadcrumbDto.BreadcrumbItem> buildBreadcrumbItems(CategoryEntity category) {
        List<BreadcrumbDto.BreadcrumbItem> items = new ArrayList<>();
        CategoryEntity temp = category;
        while (temp != null && temp.getParent() != null) {
            items.add(cateMapper.toBreadcrumbItem(temp));
            temp = temp.getParent();
        }
        Collections.reverse(items);
        return items;
    }
}
