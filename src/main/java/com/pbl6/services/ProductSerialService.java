package com.pbl6.services;

import com.pbl6.entities.InventoryLocationEntity;
import com.pbl6.entities.InventoryTransferItemEntity;
import com.pbl6.entities.OrderItemEntity;
import com.pbl6.entities.ProductSerialEntity;

import java.util.List;
import java.util.Map;

public interface ProductSerialService {
    //    List<ProductSerialEntity> transferSerial(OrderItemEntity orderItem, Long locationId);
    List<ProductSerialEntity> reserveSerial(OrderItemEntity orderItem, Long locationId);
}
