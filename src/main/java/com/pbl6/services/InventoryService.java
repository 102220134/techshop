package com.pbl6.services;

import com.pbl6.dtos.request.inventory.SearchInventoryRequest;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.InventoryDto;
import com.pbl6.entities.OrderItemEntity;
import com.pbl6.entities.StoreEntity;

import java.util.List;

public interface InventoryService {
    void handlePickupAtStore(StoreEntity store, List<OrderItemEntity> orderItems);
    void handleShip(List<OrderItemEntity> orderItems);
    boolean isInStock(Long variantId, Integer quantity);

    PageDto<InventoryDto> searchInventory(SearchInventoryRequest req);

}
