package com.pbl6.controllers;

import com.pbl6.dtos.request.ProductRequest;
import com.pbl6.dtos.request.ProductSearchRequest;
import com.pbl6.dtos.response.*;
import com.pbl6.services.ProductService;
import com.pbl6.utils.FileStorageUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product")
public class ProductController {
//    @Value("${app.upload-dir}")
//    private String uploadDir;
//    private final FileStorageUtil fileStorageUtil;
//    private final ProductService productService;


    private final ProductService productService;

    @GetMapping("/{*slugPath}")
    public ApiResponseDto<PageDto<ProductListItemDto>> getProducts(
            @PathVariable String slugPath,
            @RequestParam(required = false) Boolean includeInactive,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false)  String dir,
            @RequestParam(required = false)  Integer page,
            @RequestParam(required = false)  Integer size
            ) {

        ProductRequest req = new ProductRequest(
                slugPath.startsWith("/") ? slugPath.substring(1) : slugPath,
                Boolean.TRUE.equals(includeInactive),
                orderBy,
                dir,
                Math.max(0, page),
                Math.max(1, Math.min(200, size))
        );

        Pageable pageable = PageRequest.of(page, size, JpaSort.unsafe(Sort.Direction.DESC, "price"));

        ApiResponseDto<PageDto<ProductListItemDto>> response = new ApiResponseDto<>();
        response.setData(new PageDto<>(productService.getProductsByCategory(
                req,
                pageable
        )));
        return response;
    }

    private Sort resolveSort(String orderBy) {
        if (orderBy == null) {
            return Sort.by(Sort.Order.desc("sold"), Sort.Order.desc("stock"));
        }
        return switch (orderBy) {
            case "priceAsc"  -> Sort.by(Sort.Order.asc("price"));
            case "priceDesc" -> Sort.by(Sort.Order.desc("price"));
            case "ratingAsc" -> Sort.by(Sort.Order.asc("average"));
            case "ratingDesc"-> Sort.by(Sort.Order.desc("average"));
            case "stockDesc" -> Sort.by(Sort.Order.desc("stock"));
            default          -> Sort.by(Sort.Order.desc("sold"), Sort.Order.desc("stock"));
        };
    }

//    @GetMapping("/{slug}")
//    public ApiResponseDto<?> test(
//            @Valid
//            @NotBlank(message = "REQUIRED_FIELD_MISSING")
//            @NotNull(message = "REQUIRED_FIELD_MISSING")
//            @PathVariable String slug) {
//        ApiResponseDto<ProductDetailDto> response = new ApiResponseDto<>();
//        response.setData(productService.getProductDetail(slug));
//        return  response;
//    }

//    @PostMapping("/searchAdvanced")
//    @Operation(summary = "List products (paging, sorting, filtering)")
//    public ApiResponseDto<PageDto<ProductListItemDto>> search(@Valid @RequestBody ProductSearchRequest req) {
//        int page = req.page() == null ? 0 : Math.max(0, req.page());
//        int size = req.size() == null ? 20 : Math.max(1, Math.min(200, req.size()));
//
//        Sort sort = Sort.by("createdAt").descending();
//        if (req.sort() != null && !req.sort().isEmpty()) {
//            List<Sort.Order> orders = new ArrayList<>();
//            for (String s : req.sort()) {
//                if (s == null || s.isBlank()) continue;
//                String[] parts = s.split(",", 2);
//                String prop = parts[0].trim();
//                Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
//                        ? Sort.Direction.ASC : Sort.Direction.DESC;
//                // whitelist cột cho an toàn:
//                if (prop.matches("(?i)name|price|createdAt|updatedAt|sku")) {
//                    orders.add(new Sort.Order(dir, prop));
//                }
//            }
//            if (!orders.isEmpty()) sort = Sort.by(orders);
//        }
//
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        ApiResponseDto<PageDto<ProductListItemDto>> response = new ApiResponseDto<>();
//        response.setData(new PageDto<>(productService.searchAdvanced(
//                req,
//                pageable
//        )));
//        return response;
//    }

//    @PostMapping(value = "/test1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> upload(@ModelAttribute FileRequest request) throws Exception {
//        // Lấy sku và file từ DTO
//        String sku = request.getSku();
//        MultipartFile file = request.getFile();
//        var stored = fileStorageUtil.storeImage(sku, file);
//        StringBuilder urlPath = new StringBuilder();
//        urlPath.append('/').append(uploadDir).append('/').append(stored.path());
//
//        return ResponseEntity.ok(Map.of(
//                "sku", sku,
//                "url", urlPath
//        ));
//    }

//    @GetMapping(value = "/filters")
//    public ApiResponseDto<?> getFiltersByCategories( @RequestParam(required = false) List<String> categories) throws Exception {
//     List<FilterDto> filterDtos = productService.getFiltersByCategories(categories);
//     ApiResponseDto<List<FilterDto>> response = new ApiResponseDto<>();
//     response.setData(filterDtos);
//     return response;
//    }


}