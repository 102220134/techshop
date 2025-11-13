package com.pbl6.services.impl;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.InventoryLocationDto;
import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.entities.StoreEntity;
import com.pbl6.entities.WarehouseEntity;
import com.pbl6.enums.InventoryLocationType;
import com.pbl6.repositories.InventoryLocationRepository;
import com.pbl6.repositories.StoreRepository;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.InventoryLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryLocationServiceImpl implements InventoryLocationService {

    private final WareHouseRepository wareHouseRepository;
    private final InventoryLocationRepository inventoryLocationRepository;
    private final StoreRepository storeRepository;

    @Override
    public List<InventoryLocationDto> getInventoryLocation(InventoryLocationType type) {

        List<InventoryLocationEntity> locations =
                inventoryLocationRepository.findByType(type);

        List<InventoryLocationDto> dtos = new ArrayList<>();

        for (InventoryLocationEntity loc : locations) {
            dtos.add(toDto(loc));
        }

        return dtos;
    }

    @Override
    public InventoryLocationDto toDto(InventoryLocationEntity loc) {
        InventoryLocationDto dto = new InventoryLocationDto();
        dto.setId(loc.getId());
        dto.setType(loc.getType());

        switch (loc.getType()) {

            case WAREHOUSE -> {
                WarehouseEntity warehouseEntity = wareHouseRepository.findWarehouseByInventoryLocationId(loc.getId()).get();
                dto.setName(warehouseEntity.getName());
            }

            case STORE -> {
                StoreEntity storeEntity = storeRepository.findByInventoryLocationId(loc.getId()).get();
                dto.setName(storeEntity.getName());
            }

        }
        return dto;
    }
}
