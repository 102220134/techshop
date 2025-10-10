package com.pbl6.services.impl;

import com.pbl6.dtos.response.PromotionDto;
import com.pbl6.dtos.response.VariantDto;
import com.pbl6.entities.InventoryEntity;
import com.pbl6.entities.VariantEntity;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.PromotionService;
import com.pbl6.services.VariantService;
import com.pbl6.services.WarehouseService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {
    private final VariantRepository variantRepo;
    private final WarehouseService warehouseService;
    private final PromotionService promotionService;
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

    @Override
    public VariantDto getVariantById(Long id) {
        VariantEntity v = entityUtil.ensureExists(variantRepo.findById(id));
        List<VariantDto.AttributeDto> attrs = v.getVariantAttributeValues().stream()
                .map(vav -> VariantDto.AttributeDto.builder()
                        .code(vav.getAttribute().getCode())
                        .label(vav.getAttribute().getLabel())
                        .value(vav.getAttributeValue().getLabel())
                        .build())
                .toList();

        PromotionDto promotion = promotionService.findBestPromotion(v.getProduct().getId(),v.getPrice());
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
                .specialPrice(promotion == null ? v.getPrice() : promotion.getSpecialPrice())
                .build();
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
