package com.pbl6.services.impl;

import com.pbl6.dtos.response.product.MediaDto;
import com.pbl6.mapper.MediaMapper;
import com.pbl6.repositories.MediaRepository;
import com.pbl6.services.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    @Override
    public List<MediaDto> findByProductId(Long productId) {
        return mediaRepository.findByProductId(productId).stream()
                .map(mediaMapper::toDto)
                .toList();
    }
}
