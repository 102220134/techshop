package com.pbl6.services.impl;

import com.pbl6.dtos.response.StoreDto;
import com.pbl6.entities.StoreEntity;
import com.pbl6.repositories.StoreRepository;
import com.pbl6.services.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;

    @Override
    public List<StoreDto> getAllStore() {
        return storeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    private StoreDto toDto(StoreEntity e) {
        StoreDto dto = new StoreDto();
        dto.setId(e.getId());
        dto.setName(e.getName());

        StoreDto.Location location = new StoreDto.Location();
        location.setLatitude(e.getLatitude());
        location.setLongitude(e.getLongitude());

        dto.setLocation(location);
        dto.setDisplayAddress(e.getDisplayAddress());
        dto.setTimeOpen(e.getTimeOpen());
        dto.setTimeClose(e.getTimeClose());
        return dto;
    }
}
