package com.pbl6.services;

import com.pbl6.dtos.response.FilterDto;

import java.util.List;

public interface FilterService {
    List<FilterDto> getFiltersByCateSlug(String slug, boolean includeInactive);
}
