package com.pbl6.dtos.response;

import lombok.Builder;

import java.util.List;
@Builder
public record AttributeDto(
        long id,
        String code,
        String label,
        List<ValueDto> values
) {
    public record ValueDto(
            long id,
            String value,
            String label
    ) {}
}
