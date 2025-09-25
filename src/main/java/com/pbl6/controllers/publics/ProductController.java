package com.pbl6.controllers.publics;

import com.pbl6.dtos.request.FileRequest;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.ProductDetailDto;
import com.pbl6.dtos.response.ProductDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.CategoryRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.services.ProductService;
import com.pbl6.utils.CloudinaryUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/product")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final ProductRepository productRepository;

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

//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> upload(@ModelAttribute FileRequest request) throws Exception {
//        Long id = request.getCategoryId();
//        MultipartFile file = request.getFile();
//
//        var entity = productRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
//
//        // Upload Cloudinary thay vì local
//        String uploadedUrl = cloudinaryUtil.uploadImage(file, entity.getSlug());
//
//        // Lưu URL vào DB
//        entity.setThumbnail(uploadedUrl);
//        productRepository.save(entity);
//
//        return ResponseEntity.ok(Map.of("url", uploadedUrl));
//    }

}