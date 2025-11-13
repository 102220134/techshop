package com.pbl6.services;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.InventoryLocationDto;
import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.enums.InventoryLocationType;

import java.util.List;

public interface InventoryLocationService {
    List<InventoryLocationDto> getInventoryLocation(InventoryLocationType type);
    InventoryLocationDto toDto(InventoryLocationEntity loc);
}
