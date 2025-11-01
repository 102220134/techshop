package com.pbl6.services;

import com.pbl6.dtos.response.WarehouseDto;

import java.util.List;

public interface WarehouseService {
    List<WarehouseDto> getAllWarehouses();
}
