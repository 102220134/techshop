package com.pbl6.services;

import com.pbl6.dtos.response.AttributeDto;

import java.util.List;

public interface AttributeService {
    List<AttributeDto> getFiltersByCateSlug(String slug);

    List<AttributeDto> getAllAttributeFilter();

    List<AttributeDto> getAllAttributeOption();
}
