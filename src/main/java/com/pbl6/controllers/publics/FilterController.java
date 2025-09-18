package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.CategoryDto;
import com.pbl6.dtos.response.FilterDto;
import com.pbl6.services.FilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/filter")
@RequiredArgsConstructor
public class FilterController {
    private final FilterService filterService;
    @GetMapping("/{*slug}")
    public ApiResponseDto<List<FilterDto>> getFiltersByCateSlug(@PathVariable("slug") String slug) {
        String cleanSlug = slug.startsWith("/") ? slug.substring(1) : slug;
        ApiResponseDto<List<FilterDto>> response = new ApiResponseDto<>();
        response.setData(filterService.getFiltersByCateSlug(cleanSlug,false));
        return response;
    }
}
