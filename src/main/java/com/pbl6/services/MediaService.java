package com.pbl6.services;

import com.pbl6.dtos.response.product.MediaDto;

import java.util.List;

public interface MediaService {
    List<MediaDto> findByProductId(Long productId);
}
