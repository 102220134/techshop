package com.pbl6.services.impl;

import com.pbl6.dtos.response.FilterDto;
import com.pbl6.entities.AttributeValueEntity;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.ProductAttributeValueEntity;
import com.pbl6.entities.ProductEntity;
import com.pbl6.repositories.ProductAttributeValueRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.services.FilterService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilterServiceImpl implements FilterService {
    private final CategoryService categoryService;
    private final ProductAttributeValueRepository pavRepo;
    private final EntityUtil entityUtil;

    @Override
    public List<FilterDto> getFiltersByCateSlug(String slug, boolean includeInactive) {
        CategoryEntity category = categoryService.resolveBySlugPath(slug);
        entityUtil.ensureActive(category, includeInactive);

        List<Long> productIds = category.getProducts().stream()
                .filter(p -> includeInactive || p.getIsActive())
                .map(ProductEntity::getId)
                .toList();

        return pavRepo.findByProductIdIn(productIds).stream()
                // Group theo attribute code
                .collect(Collectors.groupingBy(
                        pav -> pav.getAttribute().getCode(),
                        Collectors.collectingAndThen(Collectors.toList(), pavList -> {
                            var attr = pavList.get(0).getAttribute();

                            // Lấy tất cả attributeValue trong nhóm
                            List<FilterDto.ValueDto> values = pavList.stream()
                                    .map(ProductAttributeValueEntity::getAttributeValue)
                                    .filter(av -> av != null)
                                    .distinct()
                                    .map(av -> new FilterDto.ValueDto(av.getValue(), av.getLabel()))
                                    .toList();

                            return FilterDto.builder()
                                    .code(attr.getCode())
                                    .label(attr.getLabel())
                                    .values(values)
                                    .build();
                        })
                ))
                .values().stream()
                .filter(filter -> filter.values() != null && filter.values().size() > 1)
                .toList();
    }



}
