package com.pbl6.services.impl;

import com.pbl6.dtos.response.VariantDto;
import com.pbl6.entities.InventoryEntity;
import com.pbl6.entities.VariantEntity;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.VariantService;
import com.pbl6.services.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {
    private final VariantRepository variantRepo;
    private final WarehouseService warehouseService;

    @Override
    public List<VariantDto> getVariantsByProduct(Long productId, Long warehouseId) {
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

                    List<InventoryEntity> inventories = v.getInventories();

                    InventoryEntity mainInv = inventories.stream()
                            .filter(i -> i.getWarehouse().getId().equals(warehouseId))
                            .findFirst()
                            .orElse(null);

                    int stock = mainInv != null ? mainInv.getStock() : 0;
                    int reserved = mainInv != null ? mainInv.getReservedStock() : 0;

                    List<VariantDto.WarehouseStockDto> otherWh = warehouseService.getWarehouseStockByVariantId(v.getId()).stream()
                            .filter(wh -> !wh.warehouseId().equals(warehouseId))
                            .toList();

                    VariantDto.StockDto stockDto = VariantDto.StockDto.builder()
                            .warehouseId(warehouseId)
                            .availableStock(stock - reserved)
                            .otherWarehouses(otherWh)
                            .build();

                    return VariantDto.builder()
                            .id(v.getId())
                            .sku(v.getSku())
                            .thumbnail(v.getThumbnail())
                            .price(v.getPrice())
                            .attributes(attrs)
                            .stock(stockDto)
                            .build();
                })
                .toList();
    }

    @Override
    public Map<Long, List<VariantDto.WarehouseStockDto>> getWarehouseStockForVariants(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }

        // Batch fetch warehouse stock for all variants
        return variantIds.stream()
                .collect(Collectors.toMap(
                        variantId -> variantId,
                        warehouseService::getWarehouseStockByVariantId
                ));
    }
}
