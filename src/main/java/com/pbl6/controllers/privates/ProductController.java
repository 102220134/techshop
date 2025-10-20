//package com.pbl6.controllers.privates;
//
//import com.pbl6.dtos.request.order.MyOrderRequest;
//import com.pbl6.dtos.request.product.ProductFilterRequest;
//import com.pbl6.dtos.response.ApiResponseDto;
//import com.pbl6.dtos.response.PageDto;
//import com.pbl6.dtos.response.order.OrderDto;
//import com.pbl6.dtos.response.product.ProductDto;
//import com.pbl6.entities.UserEntity;
//import com.pbl6.services.OrderService;
//import com.pbl6.utils.AuthenticationUtil;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.springdoc.core.annotations.ParameterObject;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//
//@RestController
//@RequestMapping("api/product")
//@RequiredArgsConstructor
//@Tag(name = "Quản lý sản phẩm")
//public class ProductController {
//    @Operation(summary = "Lọc sản phẩm")
//    @GetMapping("/filter/{*cateSlug}")
//    public ApiResponseDto<PageDto<ProductDto>> searchProducts(
//            @Schema(defaultValue = "mobile")
//            @PathVariable String cateSlug,
//            @ParameterObject ProductFilterRequest req,
//            @RequestParam @Schema(defaultValue = """
//                    {
//                    "mobile_nhu_cau_sd":"choi-game"
//                    }
//                    """) MultiValueMap<String, String> params
//    ) {
//
//        final Set<String> PARAM_KEYS = Set.of("order", "dir", "page", "size", "price_from", "price_to");
//        Map<String, List<String>> filters = new HashMap<>();
//        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
//            String key = entry.getKey();
//            if (PARAM_KEYS.contains(key)) {
//                continue;
//            }
//
//            String raw = entry.getValue().get(0);
//            List<String> values = Arrays.stream(raw.split(","))
//                    .map(String::trim)
//                    .toList();
//
//            filters.put(key, values);
//        }
//
//        req.setFilter(filters);
//        cateSlug = cateSlug.startsWith("/") ? cateSlug.substring(1) : cateSlug;
//        PageDto<ProductDto> products = new PageDto<>(productService.filterProduct(cateSlug, req, false));
//        ApiResponseDto<PageDto<ProductDto>> response = new ApiResponseDto<>();
//        response.setData(products);
//
//        return response;
//    }
//
//}
