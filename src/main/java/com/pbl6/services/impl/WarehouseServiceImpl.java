package com.pbl6.services.impl;

import com.pbl6.dtos.response.WarehouseResponse;
import com.pbl6.repositories.InventoryRepository;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
