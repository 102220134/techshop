package com.pbl6.services.impl;

import com.pbl6.dtos.response.VariantDto;
import com.pbl6.entities.InventoryEntity;
import com.pbl6.entities.VariantEntity;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.VariantService;
import com.pbl6.services.WarehouseService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {
    private final VariantRepository variantRepo;
    private final WarehouseService warehouseService;

    //    private final InventoryRepository inventoryRepo;
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

                    // stock info
                    List<InventoryEntity> inventories = v.getInventories();

                    // stock ở kho được chọn
                    InventoryEntity mainInv = inventories.stream()
                            .filter(i -> i.getWarehouse().getId().equals(warehouseId))
                            .findFirst()
                            .orElse(null);

                    int stock = mainInv != null ? mainInv.getStock() : 0;
                    int reserved = mainInv != null ? mainInv.getReservedStock() : 0;

                    // các kho khác
//                    List<VariantDto.WarehouseStockDto> otherWh = inventories.stream()
//                            .filter(i -> !i.getWarehouse().getId().equals(warehouseId))
//                            .map(i -> VariantDto.WarehouseStockDto.builder()
//                                    .warehouseId(i.getWarehouse().getId())
//                                    .name(i.getWarehouse().getName())
////                                    .stock(i.getStock())
////                                    .reservedStock(i.getReservedStock())
//                                    .availableStock(i.getStock() - i.getReservedStock())
//                                    .build())
//                            .toList();

                    List<VariantDto.WarehouseStockDto> otherWh = warehouseService.getWarehouseStockByVariantId(v.getId()).stream()
                            .filter(wh-> !wh.warehouseId().equals(warehouseId))
                            .toList();


                    VariantDto.StockDto stockDto = VariantDto.StockDto.builder()
                            .warehouseId(warehouseId)
//                            .stock(stock)
//                            .reservedStock(reserved)
                            .availableStock(stock - reserved)
                            .otherWarehouses(otherWh)
                            .build();

                    return VariantDto.builder()
                            .id(v.getId())
//                            .name(v.getName)
                            .sku(v.getSku())
                            .thumbnail(v.getThumbnail())
                            .price(v.getPrice())
                            .attributes(attrs)
                            .stock(stockDto)
                            .build();
                })
                .toList();
    }
}
