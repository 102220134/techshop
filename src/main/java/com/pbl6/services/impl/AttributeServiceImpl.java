package com.pbl6.services.impl;

import com.pbl6.dtos.request.attribute.AddAttributeRequest;
import com.pbl6.dtos.request.attribute.EditAttributeRequest;
import com.pbl6.dtos.response.AttributeDto;
import com.pbl6.entities.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.*;
import com.pbl6.services.AttributeService;
import com.pbl6.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {
    private final CategoryService categoryService;
    private final ProductAttributeValueRepository pavRepo;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final VariantAttributeValueRepository variantAttributeValueRepository; // Cần thêm repo này để check xóa

    @Override
    public List<AttributeDto> getFiltersByCateSlug(String slug) {
        // 1. Lấy ID category (nhanh hơn lấy cả entity to đùng)
        CategoryEntity category = categoryService.resolveBySlugPath(slug);

        // 2. TỐI ƯU: Không load product lên. Query trực tiếp các AttributeValue đang được dùng bởi Product trong Category này.
        // Cần viết query custom trong pavRepo (Xem phần Repository bên dưới)
        List<ProductAttributeValueEntity> pavs = pavRepo.findAllByCategory(category.getId());

        // 3. Xử lý Grouping và Mapping (Logic giữ nguyên nhưng input đã nhẹ hơn rất nhiều)
        return pavs.stream()
                .filter(pav -> pav.getAttribute() != null && Boolean.TRUE.equals(pav.getAttribute().getIsFilter()))
                .collect(Collectors.groupingBy(
                        pav -> pav.getAttribute().getCode(),
                        Collectors.collectingAndThen(Collectors.toList(), pavList -> {
                            var attr = pavList.get(0).getAttribute();
                            List<AttributeDto.ValueDto> values = pavList.stream()
                                    .map(ProductAttributeValueEntity::getAttributeValue)
                                    .filter(Objects::nonNull)
                                    .distinct() // Loại bỏ trùng lặp
                                    .map(av -> new AttributeDto.ValueDto(av.getId(), av.getValue(), av.getLabel()))
                                    .toList();

                            return AttributeDto.builder()
                                    .id(attr.getId())
                                    .code(attr.getCode())
                                    .label(attr.getLabel())
                                    .values(values)
                                    .build();
                        })
                ))
                .values().stream()
                .filter(filter -> filter.values() != null && filter.values().size() > 1) // Chỉ hiện filter nếu có > 1 lựa chọn
                .toList();
    }

    @Override
    public List<AttributeDto> getAllAttributeFilter(Long cateId) {
        // Lấy danh sách cha con
        List<CategoryEntity> categories = categoryService.getAllParents(cateId);

        // Lấy list ID
        List<Long> categoryIds = categories.stream().map(CategoryEntity::getId).toList();

        // TỐI ƯU: 1 Query lấy tất cả Attribute + Values của các category này (Tránh N+1)
        List<AttributeEntity> attributes = attributeRepository.findAllByCategoryIdInAndIsFilterTrue(categoryIds);

        // Map DTO
        return mapToAttributeDtoList(attributes);
    }

    @Override
    public List<AttributeDto> getAllAttributeOption() {
        // TỐI ƯU: Sử dụng method fetch join values để tránh N+1
        List<AttributeEntity> attributes = attributeRepository.findAllByIsOptionTrueWithValues();
        return mapToAttributeDtoList(attributes);
    }

    // Hàm helper map DTO để tránh lặp code
    private List<AttributeDto> mapToAttributeDtoList(List<AttributeEntity> attributes) {
        // Dùng LinkedHashMap để giữ thứ tự nếu cần, ở đây distinct theo Code
        Map<String, AttributeDto> map = new LinkedHashMap<>();

        for (AttributeEntity attr : attributes) {
            if (!map.containsKey(attr.getCode())) {
                List<AttributeDto.ValueDto> values = attr.getValues().stream()
                        .map(v -> new AttributeDto.ValueDto(v.getId(), v.getValue(), v.getLabel()))
                        .toList();

                map.put(attr.getCode(), AttributeDto.builder()
                        .id(attr.getId())
                        .code(attr.getCode())
                        .label(attr.getLabel())
                        .values(values)
                        .build());
            }
        }
        return new ArrayList<>(map.values());
    }

    @Transactional
    @Override
    public void addAttributeValue(AddAttributeRequest attributeRequest) {
        AttributeEntity attribute = attributeRepository.findByCode(attributeRequest.getCode())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "attribute not found"));

        boolean exists = attributeValueRepository.existsByValueAndAttributeId(attributeRequest.getValue(), attribute.getId());
        if (exists) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "value already exists");
        }

        AttributeValueEntity attributeValue = new AttributeValueEntity();
        attributeValue.setAttribute(attribute);
        attributeValue.setValue(attributeRequest.getValue());
        attributeValue.setLabel(attributeRequest.getLabel());
        attributeValue.setCreatedAt(LocalDateTime.now());
        attributeValueRepository.save(attributeValue);
    }

    @Transactional
    @Override
    public void editAttributeValue(Long valueId, EditAttributeRequest attributeRequest) {
        AttributeValueEntity attributeValue = attributeValueRepository.findById(valueId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "value not found"));

        // Nên check trùng value mới nếu cần thiết
        attributeValue.setValue(attributeRequest.getValue());
        attributeValue.setLabel(attributeRequest.getLabel());
        attributeValueRepository.save(attributeValue);
    }

    @Transactional
    @Override
    public void deleteAttributeValue(Long valueId) {
        AttributeValueEntity attributeValue = attributeValueRepository.findById(valueId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "value not found"));

        // ✅ AN TOÀN DỮ LIỆU: Check xem value này có đang được dùng không
        boolean inUseInProduct = pavRepo.existsByAttributeValueId(valueId);
        boolean inUseInVariant = variantAttributeValueRepository.existsByAttributeValueId(valueId);

        if (inUseInProduct || inUseInVariant) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Cannot delete. This attribute value is currently assigned to products or variants.");
        }

        attributeValueRepository.delete(attributeValue);
    }
}