package com.pbl6.dtos.response.product;

import lombok.Builder;

@Builder
public record MediaDto(
        Long id,
        String mediaType,
        String url,
        String altText
) {}
