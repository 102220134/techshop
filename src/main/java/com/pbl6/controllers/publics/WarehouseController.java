package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.WarehouseResponse;
import com.pbl6.services.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/public/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ApiResponseDto<List<WarehouseResponse>> getAllWarehouses() {
        ApiResponseDto<List<WarehouseResponse>> response = new ApiResponseDto<>();
        response.setData(warehouseService.getAllWarehouses());
        return response;
    }
}
