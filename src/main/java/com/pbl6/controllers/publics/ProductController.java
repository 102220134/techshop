package com.pbl6.controllers.publics;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.ProductDetailDto;
import com.pbl6.dtos.response.ProductDto;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.services.ProductService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/product")
public class ProductController {

//    @Value("${app.upload-dir}")
//    private String uploadDir;
//    private final FileStorageUtil fileStorageUtil;


    private final ProductService productService;

//    @GetMapping("/{*slug}")
//    public ApiResponseDto<PageDto<ProductDto>> getProducts(
//            @PathVariable String slug,
//            @ParameterObject ProductParamRequest req
//    ) {
//
//        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), resolveSort(req.getOrderBy(),req.getDir()));
//
//        ApiResponseDto<PageDto<ProductDto>> response = new ApiResponseDto<>();
//        response.setData(new PageDto<>(productService.getProductsByCategory(
//                slug.startsWith("/") ? slug.substring(1) : slug,
//                req,
//                pageable
//        )));
//        return response;
//    }

    @GetMapping("/featured/{*slug}")
    public ApiResponseDto<List<ProductDto>> getFeaturedProducts(
            @PathVariable String slug,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {

        if (size < 1) throw new AppException(ErrorCode.VALIDATION_ERROR);

        slug = slug.startsWith("/") ? slug.substring(1) : slug;

        ApiResponseDto<List<ProductDto>> response = new ApiResponseDto<>();
        response.setData(productService.getFeaturedProducts(slug, size));
        return response;
    }

//    private JpaSort resolveSort(String orderBy,String dir) {
//        if("desc".equals(dir))
//            return JpaSort.unsafe(Sort.Direction.DESC, orderBy);
//        return JpaSort.unsafe(Sort.Direction.ASC, orderBy);
//    }

    @GetMapping("/search/{*slugPath}")
    public ApiResponseDto<PageDto<ProductDto>> searchProducts(
            @PathVariable String slugPath,
            @ParameterObject ProductFilterRequest req,
            @RequestParam @Schema(defaultValue = """
                    {
                    "mobile_nhu_cau_sd":"choi-game"
                    }
                    """) MultiValueMap<String, String> params
    ) {

        final Set<String> PARAM_KEYS = Set.of("order", "dir", "page", "size", "price_from", "price_to");
        Map<String, List<String>> filters = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            if (PARAM_KEYS.contains(key)) {
                continue;
            }

            String raw = entry.getValue().get(0);
            List<String> values = Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .toList();

            filters.put(key, values);
        }

        req.setFilter(filters);
        slugPath = slugPath.startsWith("/") ? slugPath.substring(1) : slugPath;
        PageDto<ProductDto> products = new PageDto<>(productService.searchProduct(slugPath, req, false));
        ApiResponseDto<PageDto<ProductDto>> response = new ApiResponseDto<>();
        response.setData(products);

        return response;
    }

    @GetMapping("/{slug}/detail")
    public ApiResponseDto<ProductDetailDto> getProductDetail(
            @PathVariable String slug,
            @RequestParam Long warehouse_id
    ) {
        slug = slug.startsWith("/") ? slug.substring(1) : slug;
        ProductDetailDto product = productService.getProductDetail(slug, warehouse_id, false);
        ApiResponseDto<ProductDetailDto> response = new ApiResponseDto<>();
        response.setData(product);

        return response;
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

//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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