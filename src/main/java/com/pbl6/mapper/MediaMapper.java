package com.pbl6.mapper;

import com.pbl6.dtos.response.product.MediaDto;
import com.pbl6.entities.MediaEntity;
import org.springframework.stereotype.Component;

@Component
public class MediaMapper {
    public MediaDto toDto(MediaEntity entity) {
        return MediaDto.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .mediaType(entity.getMediaType())
                .altText(entity.getAltText())
                .build();
    }
}
