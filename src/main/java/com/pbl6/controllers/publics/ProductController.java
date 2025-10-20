package com.pbl6.controllers.publics;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.request.product.ProductSearchRequest;
import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.PageDto;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.repositories.CategoryRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.VariantRepository;
import com.pbl6.services.ProductService;
import com.pbl6.utils.CloudinaryUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/product")
@Tag(name = "Sản phẩm (get public)")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    @GetMapping("/featured/{*cateSlug}")
    @Operation(summary = "Sản phẩm nổi bật")
    public ApiResponseDto<List<ProductDto>> getFeaturedProducts(
            @PathVariable String cateSlug,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {

        if (size < 1) throw new AppException(ErrorCode.VALIDATION_ERROR);

        cateSlug = cateSlug.startsWith("/") ? cateSlug.substring(1) : cateSlug;

        ApiResponseDto<List<ProductDto>> response = new ApiResponseDto<>();
        response.setData(productService.getFeaturedProducts(cateSlug, size));
        return response;
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm theo keyword")
    public ApiResponseDto<PageDto<ProductDto>> getFeaturedProducts(@ParameterObject ProductSearchRequest req) {
        PageDto<ProductDto> products = new PageDto<>(productService.searchProduct(req,false));
        ApiResponseDto<PageDto<ProductDto>> response = new ApiResponseDto<>();
        response.setData(products);
        return response;
    }

    @GetMapping("/best_seller/{*cateSlug}")
    @Operation(summary = "Sản phẩm top lượt bán")
    public ApiResponseDto<List<ProductDto>> getBestSellerProducts(
            @PathVariable String cateSlug,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {

        if (size < 1) throw new AppException(ErrorCode.VALIDATION_ERROR);

        cateSlug = cateSlug.startsWith("/") ? cateSlug.substring(1) : cateSlug;

        ApiResponseDto<List<ProductDto>> response = new ApiResponseDto<>();
        response.setData(productService.getBestSellerProducts(cateSlug, size));
        return response;
    }
    @Operation(summary = "Lọc sản phẩm")
    @GetMapping("/filter/{*cateSlug}")
    public ApiResponseDto<PageDto<ProductDto>> searchProducts(
            @Schema(defaultValue = "mobile")
            @PathVariable String cateSlug,
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
        cateSlug = cateSlug.startsWith("/") ? cateSlug.substring(1) : cateSlug;
        PageDto<ProductDto> products = new PageDto<>(productService.filterProduct(cateSlug, req, false));
        ApiResponseDto<PageDto<ProductDto>> response = new ApiResponseDto<>();
        response.setData(products);

        return response;
    }

    @GetMapping("/{slug}/detail")
    @Operation(summary = "Chi tiết sản phẩm")
    public ApiResponseDto<ProductDetailDto> getProductDetail(
            @Schema(defaultValue = "iphone-16-pro-max")
            @PathVariable String slug
    ) {
        slug = slug.startsWith("/") ? slug.substring(1) : slug;
        ProductDetailDto product = productService.getProductDetail(slug, false);
        ApiResponseDto<ProductDetailDto> response = new ApiResponseDto<>();
        response.setData(product);

        return response;
    }

//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> upload(@ModelAttribute FileRequest request) throws Exception {
//        Long id = request.getCategoryId();
//        MultipartFile file = request.getFile();
//
//        var entity = variantRepository.findById(id)
//                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
//
//        // Upload Cloudinary thay vì local
//        String uploadedUrl = cloudinaryUtil.uploadImage(file, entity.getSku());
//
//        // Lưu URL vào DB
//        entity.setThumbnail(uploadedUrl);
//        variantRepository.save(entity);
//
//        return ResponseEntity.ok(Map.of("url", uploadedUrl));
//    }

}