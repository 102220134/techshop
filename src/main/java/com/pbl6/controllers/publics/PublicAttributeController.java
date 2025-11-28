package com.pbl6.controllers.publics;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.AttributeDto;
import com.pbl6.services.AttributeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/attribute")
@RequiredArgsConstructor
@Tag(name = "Thuộc tính của sản phẩm", description = "Thuộc tính gồm thuộc tính dùng option hoặc dùng filter")
public class PublicAttributeController {
    private final AttributeService filterService;
    @GetMapping("filter/{*slug}")
    @Operation(summary = "Để tạo dynamic filter cho sản phẩm")
    public ApiResponseDto<List<AttributeDto>> getFiltersByCateSlug(@PathVariable("slug") String slug) {
        String cleanSlug = slug.startsWith("/") ? slug.substring(1) : slug;
        ApiResponseDto<List<AttributeDto>> response = new ApiResponseDto<>();
        response.setData(filterService.getFiltersByCateSlug(cleanSlug));
        return response;
    }

}
