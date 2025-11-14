package com.pbl6.mapper;

import com.pbl6.dtos.response.StoreDto;
import com.pbl6.entities.StoreEntity;
import jakarta.persistence.Column;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper {
    public StoreDto toDto(StoreEntity e) {
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
