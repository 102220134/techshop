package com.pbl6.services.impl;

import com.pbl6.dtos.response.AttributeDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.ProductAttributeValueEntity;
import com.pbl6.entities.ProductEntity;
import com.pbl6.repositories.AttributeRepository;
import com.pbl6.repositories.ProductAttributeValueRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.services.AttributeService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {
    private final CategoryService categoryService;
    private final ProductAttributeValueRepository pavRepo;
    private final EntityUtil entityUtil;
    private final AttributeRepository attributeRepository;

    @Override
    public List<AttributeDto> getFiltersByCateSlug(String slug) {
        CategoryEntity category = categoryService.resolveBySlugPath(slug);

        List<Long> productIds = category.getProducts().stream()
                .filter(p->p.getIsActive())
                .map(ProductEntity::getId)
                .toList();

        return pavRepo.findByProductIdIn(productIds).stream()
                .filter(pav -> pav.getAttribute() != null && Boolean.TRUE.equals(pav.getAttribute().getIsFilter()))
                // Group theo attribute code
                .collect(Collectors.groupingBy(
                        pav -> pav.getAttribute().getCode(),
                        Collectors.collectingAndThen(Collectors.toList(), pavList -> {
                            var attr = pavList.get(0).getAttribute();

                            // Lấy tất cả attributeValue trong nhóm
                            List<AttributeDto.ValueDto> values = pavList.stream()
                                    .map(ProductAttributeValueEntity::getAttributeValue)
                                    .filter(av -> av != null)
                                    .distinct()
                                    .map(av -> new AttributeDto.ValueDto(av.getValue(), av.getLabel()))
                                    .toList();

                            return AttributeDto.builder()
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

    @Override
    public List<AttributeDto> getAllAttributeFilter() {
        return attributeRepository.findAllByIsFilterTrue().stream()
                .map(attr->{
                    List<AttributeDto.ValueDto> values = attr.getValues().stream()
                            .map(av -> new AttributeDto.ValueDto(av.getValue(), av.getLabel()))
                            .toList();

                    return AttributeDto.builder()
                            .code(attr.getCode())
                            .label(attr.getLabel())
                            .values(values)
                            .build();
                }).toList();
    }

    @Override
    public List<AttributeDto> getAllAttributeOption() {
        return attributeRepository.findAllByIsOptionTrue().stream()
                .map(attr->{
                    List<AttributeDto.ValueDto> values = attr.getValues().stream()
                            .map(av -> new AttributeDto.ValueDto(av.getValue(), av.getLabel()))
                            .toList();

                    return AttributeDto.builder()
                            .code(attr.getCode())
                            .label(attr.getLabel())
                            .values(values)
                            .build();
                }).toList();
    }


}
