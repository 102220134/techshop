package com.pbl6.services.impl;

import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.VariantEntity;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.VariantService;
import com.pbl6.services.WarehouseService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {
    private final VariantRepository variantRepo;
    private final WarehouseService warehouseService;
    private final EntityUtil entityUtil;

    @Override
    public List<VariantDto> getVariantsByProduct(Long productId) {
        List<VariantEntity> variants = variantRepo.findByProductId(productId);

        return variants.stream()
                .filter(VariantEntity::getIsActive)
                .map(v -> {
                    List<VariantDto.AttributeDto> attrs = v.getVariantAttributeValues().stream()
                            .map(vav -> VariantDto.AttributeDto.builder()
                                    .code(vav.getAttribute().getCode())
                                    .label(vav.getAttribute().getLabel())
                                    .value(vav.getAttributeValue().getLabel())
                                    .build())
                            .toList();

                    return VariantDto.builder()
                            .id(v.getId())
                            .sku(v.getSku())
                            .thumbnail(v.getThumbnail())
                            .price(v.getPrice())
                            .attributes(attrs)
                            .availableStock(
                                    v.getInventories().stream()
                                            .mapToInt(i -> i.getStock() - i.getReservedStock())
                                            .sum())
                            .build();
                })
                .toList();
    }

//    @Override
//    public Map<Long, List<VariantDto.WarehouseStockDto>> getWarehouseStockForVariants(List<Long> variantIds) {
//        if (variantIds == null || variantIds.isEmpty()) {
//            return Map.of();
//        }
//
//        // Batch fetch warehouse stock for all variants
//        return variantIds.stream()
//                .collect(Collectors.toMap(
//                        variantId -> variantId,
//                        warehouseService::getWarehouseStockByVariantId
//                ));
//    }
}
