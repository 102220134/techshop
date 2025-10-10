package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.StoreDto;
import com.pbl6.dtos.response.WarehouseResponse;
import com.pbl6.services.StoreService;
import com.pbl6.services.WarehouseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/public/store")
@RequiredArgsConstructor
@Tag(name = "Cửa hàng", description = "Chọn cửa hàng để pickup hoặc xem địa chỉ cửa hàng")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ApiResponseDto<List<StoreDto>> getAllStore() {
        ApiResponseDto<List<StoreDto>> response = new ApiResponseDto<>();
        response.setData(storeService.getAllStore());
        return response;
    }
}
