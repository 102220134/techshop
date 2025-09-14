package com.pbl6.services.impl;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.ProductDetailDto;
import com.pbl6.dtos.response.ProductDto;
import com.pbl6.entities.*;
import com.pbl6.mapper.MediaMapper;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.ProductRepositoryCustom;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.CategoryService;
import com.pbl6.services.ProductService;
import com.pbl6.services.VariantService;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final WareHouseRepository wareHouseRepository;
    private final EntityUtil entityUtil;
    private final ProductRepositoryCustom productRepositoryCustom;
    private final MediaMapper mediaMapper;
    private final ProductMapper productMapper;
    private final VariantService variantService;
//    private final ObjectMapper objectMapper = new ObjectMapper();

//    private ObjectNode parseDetail(String json) {
//        if (json == null) return objectMapper.createObjectNode();
//        try {
//            return (ObjectNode) objectMapper.readTree(json);
//        } catch (Exception e) {
//            return objectMapper.createObjectNode();
//        }
//    }

    @Override
    public List<ProductDto> getFeaturedProducts(String slugPath, int size) {
        CategoryEntity category = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(category, false);

        return productRepository.findAllByCategoryId(category.getId(), false).stream()
                .map(productMapper::toDto)
                .filter(p -> p.availableStock() > 0)
                .sorted(Comparator.comparingDouble(ProductDto::score).reversed())
                .limit(size)
                .toList();
    }

    @Override
    public Page<ProductDto> searchProduct(String slugPath, ProductFilterRequest req, boolean includeInactive) {
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(categoryEntity, false);
        return productRepositoryCustom.searchProducts(categoryEntity.getId(), req, includeInactive, true);
    }

    @Override
    public ProductDetailDto getProductDetail(String slug, Long wareHouseId, boolean includeInactive) {
        ProductEntity product = entityUtil.ensureExists(productRepository.findBySlug(slug));
        entityUtil.ensureActive(product, includeInactive);

        entityUtil.ensureExists(wareHouseRepository.findById(wareHouseId));

        List<ReviewEntity> reviews = product.getReviews();

        int globalAvailableStock = product.getVariants().stream()
                .filter(VariantEntity::getIsActive)
                .mapToInt(v -> v.getInventories().stream()
                        .mapToInt(i -> i.getStock() - i.getReservedStock())
                        .sum()
                )
                .sum();



        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .thumbnail(product.getThumbnail())
                .detail(product.getDetail().put("id", product.getId()))
                .rating(new ProductDetailDto.RatingSummary(
                        reviews.size(),
                        reviews.stream()
                                .mapToInt(ReviewEntity::getRating)
                                .average()
                                .orElse(0L)

                ))
                .isAvailable(globalAvailableStock>0)
                .variants(variantService.getVariantsByProduct(product.getId(), wareHouseId))
                .medias(
                        product.getMedias().stream()
                                .map(mediaMapper::toDto)
                                .toList()
                )
                .build();
    }


}
