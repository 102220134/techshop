package com.pbl6.services.impl;

import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.inventory.movement.StockMovementDto;
import com.pbl6.entities.StockMovementEntity;
import com.pbl6.mapper.VariantMapper;
import com.pbl6.repositories.StockMovementRepository;
import com.pbl6.services.InventoryLocationService;
import com.pbl6.services.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {
    private final StockMovementRepository stockMovementRepository;
    private final VariantMapper variantMapper;
    private final InventoryLocationService inventoryLocationService;
    @Override
    public PageDto<StockMovementDto> getHistory(Long variantId, Long locationId, Pageable pageable) {
        Page<StockMovementEntity> page = stockMovementRepository.findByVariantIdAndLocationId(variantId, locationId, pageable);
        var pageDto = page.map(st->StockMovementDto.builder()
                .id(st.getId())
                .quantityDelta(st.getQuantityDelta())
                .reason(st.getReason())
                .refType(st.getRefType())
                .refId(st.getRefId())
                .createdAt(st.getCreatedAt())
                .variant(variantMapper.toDto(st.getVariant()))
                .inventoryLocation(inventoryLocationService.toDto(st.getInventoryLocation()))
                .build());
        return new PageDto<>(pageDto);
    }
}
