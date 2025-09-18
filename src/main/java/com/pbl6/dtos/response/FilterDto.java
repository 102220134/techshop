package com.pbl6.dtos.response;

import lombok.Builder;

import java.util.List;
@Builder
public record FilterDto(
        String code,
        String label,
        List<ValueDto> values
) {
    public record ValueDto(
            String value,
            String label
    ) {}
}
