package com.pbl6.services;

import com.pbl6.dtos.response.WarehouseResponse;

import java.util.List;

public interface WarehouseService {
    List<WarehouseResponse> getAllWarehouses();
}
