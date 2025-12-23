package com.pbl6.services;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.movement.StockMovementDto;
import org.springframework.data.domain.Pageable;

public interface StockMovementService {
    PageDto<StockMovementDto> getHistory(Long variantId, Long locationId, Pageable pageable);
}
