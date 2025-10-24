package com.pbl6.mapper;

import com.pbl6.dtos.response.product.MediaDto;
import com.pbl6.entities.MediaEntity;
import com.pbl6.entities.ReviewMediaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class MediaMapper {

    public List<MediaDto> toDtoList(Set<MediaEntity> medias) {
        if (medias == null) return List.of();
        return medias.stream()
                .map(this::toDto)
                .toList();
    }

    public MediaDto toDto(MediaEntity media) {
        return MediaDto.builder()
                .id(media.getId())
                .url(media.getUrl())
                .mediaType(media.getMediaType())
                .altText(media.getAltText())
                .build();
    }

    public MediaDto toDto(ReviewMediaEntity media) {
        return MediaDto.builder()
                .id(media.getId())
                .url(media.getUrl())
                .mediaType(media.getMediaType())
                .build();
    }
}

