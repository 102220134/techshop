package com.pbl6.services.impl;

import com.pbl6.dtos.response.VariantDto;
import com.pbl6.dtos.response.WarehouseResponse;
import com.pbl6.entities.InventoryEntity;
import com.pbl6.entities.WarehouseEntity;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WareHouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream().map(
                wh-> new WarehouseResponse(wh.getId(),wh.getName())
        ).toList();
    }

    @Override
    public List<VariantDto.WarehouseStockDto> getWarehouseStockByVariantId(Long variantId) {
        // lấy tất cả warehouse
        List<WarehouseEntity> allWarehouses = warehouseRepository.findAll();

        // lấy inventories của variant
        List<InventoryEntity> inventories = inventoryRepository.findByVariantId(variantId);

        // map warehouseId -> inventory
        Map<Long, InventoryEntity> invMap = inventories.stream()
                .collect(Collectors.toMap(i -> i.getWarehouse().getId(), i -> i));

        // merge tất cả warehouse với stock
        return allWarehouses.stream()
                .map(w -> {
                    InventoryEntity inv = invMap.get(w.getId());
                    int stock = inv != null ? inv.getStock() : 0;
                    int reserved = inv != null ? inv.getReservedStock() : 0;
                    return VariantDto.WarehouseStockDto.builder()
                            .warehouseId(w.getId())
                            .name(w.getName())
                            .availableStock(stock - reserved)
                            .build();
                })
                .toList();
    }
}
