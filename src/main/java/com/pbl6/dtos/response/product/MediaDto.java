package com.pbl6.dtos.response.product;

import com.pbl6.enums.MediaType;
import lombok.Builder;

@Builder
public record MediaDto(
        Long id,
        MediaType mediaType,
        String url,
        String altText
) {}
