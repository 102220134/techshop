package com.pbl6.services.impl;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.response.*;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.enums.PromotionType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.repositories.ProductRepositoryCustom;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.*;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final CategoryService categoryService;
    private final WareHouseRepository wareHouseRepository;
    private final EntityUtil entityUtil;
    private final ProductRepositoryCustom productRepositoryCustom;
    private final ProductMapper productMapper;
    private final VariantService variantService;
    private final MediaService mediaService;
    private final PromotionService promotionService;
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

        // B1: Lấy tất cả sản phẩm trong category
        List<ProductProjection> products = productRepositoryCustom.findAllByCategoryId(category.getId(), false, true);

        // B2: Lọc còn hàng + tính score cơ bản (chưa có promotion)
        List<ProductProjection> topProducts = products.stream()
                .filter(p -> p.getAvailableStock() > 0)
                .sorted(Comparator.comparingDouble(this::calculateBaseScore).reversed())
                .limit(size)
                .toList();

        // B3: Với N sản phẩm đã chọn, mới apply promotion
        return topProducts.stream()
                .map(projection -> {
                    PromotionDto promotion = promotionService.findBestPromotion(projection.getId(), projection.getPrice());

                    BigDecimal specialPrice = promotion != null
                            ? promotion.getSpecialPrice()
                            : projection.getPrice();

                    return ProductDto.builder()
                            .id(projection.getId())
                            .name(projection.getName())
                            .description(projection.getDescription())
                            .slug(projection.getSlug())
                            .thumbnail(projection.getThumbnail())
                            .price(projection.getPrice())
                            .special_price(specialPrice)
                            .promotion(promotion)
                            .stock(projection.getStock() != null ? projection.getStock() : 0)
                            .reserved_stock(projection.getReservedStock() != null ? projection.getReservedStock() : 0)
                            .available_stock(projection.getAvailableStock())
                            .sold(projection.getSold() != null ? projection.getSold() : 0)
                            .rating(new ProductDto.RatingSummary(
                                    projection.getTotal() != null ? projection.getTotal() : 0L,
                                    projection.getAverage() != null ? projection.getAverage() : 0.0
                            ))
                            .build();
                })
                .toList();
    }

    @Override
    public List<ProductDto> getBestSellerProducts(String slug, int size) {
        CategoryEntity category = categoryService.resolveBySlugPath(slug);
        entityUtil.ensureActive(category, false);

        // B1: Lấy tất cả sản phẩm trong category
        List<ProductProjection> products = productRepositoryCustom.findAllByCategoryId(category.getId(), false, true);

        // B2: Lọc còn hàng + tính score cơ bản (chưa có promotion)
        List<ProductProjection> topProducts = products.stream()
                .filter(p -> p.getAvailableStock() > 0)
                .sorted(Comparator.comparingInt(ProductProjection::getSold).reversed())
                .limit(size)
                .toList();

        return topProducts.stream()
                .map(projection -> {
                    PromotionDto promotion = promotionService.findBestPromotion(projection.getId(), projection.getPrice());

                    BigDecimal specialPrice = promotion != null
                            ? promotion.getSpecialPrice()
                            : projection.getPrice();

                    return ProductDto.builder()
                            .id(projection.getId())
                            .name(projection.getName())
                            .description(projection.getDescription())
                            .slug(projection.getSlug())
                            .thumbnail(projection.getThumbnail())
                            .price(projection.getPrice())
                            .special_price(specialPrice)
                            .promotion(promotion)
                            .stock(projection.getStock() != null ? projection.getStock() : 0)
                            .reserved_stock(projection.getReservedStock() != null ? projection.getReservedStock() : 0)
                            .available_stock(projection.getAvailableStock())
                            .sold(projection.getSold() != null ? projection.getSold() : 0)
                            .rating(new ProductDto.RatingSummary(
                                    projection.getTotal() != null ? projection.getTotal() : 0L,
                                    projection.getAverage() != null ? projection.getAverage() : 0.0
                            ))
                            .build();
                })
                .toList();
    }

    @Override
    public Page<ProductDto> searchProduct(String slugPath, ProductFilterRequest req, boolean includeInactive) {
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(categoryEntity, false);

        Page<ProductProjection> projectionPage = productRepositoryCustom.searchProducts(
                categoryEntity.getId(), req, includeInactive, true);

        return projectionPage.map(projection -> {
            PromotionDto promotion = promotionService.findBestPromotion(projection.getId(), projection.getPrice());
            return ProductDto.builder()
                    .id(projection.getId())
                    .name(projection.getName())
                    .description(projection.getDescription())
                    .slug(projection.getSlug())
                    .thumbnail(projection.getThumbnail())
                    .price(projection.getPrice())
                    .special_price(promotion != null ? promotion.getSpecialPrice() : projection.getPrice())
                    .promotion(promotion)
                    .stock(projection.getStock() != null ? projection.getStock() : 0)
                    .reserved_stock(projection.getReservedStock() != null ? projection.getReservedStock() : 0)
                    .available_stock(projection.getAvailableStock())
                    .sold(projection.getSold() != null ? projection.getSold() : 0)
                    .rating(new ProductDto.RatingSummary(
                            projection.getTotal() != null ? projection.getTotal() : 0L,
                            projection.getAverage() != null ? projection.getAverage() : 0.0
                    ))
                    .build();
        });
    }

    @Override
    public ProductDetailDto getProductDetail(String slug,boolean includeInactive) {

        ProductProjection projection = productRepositoryCustom.findBySlug(slug, includeInactive)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        PromotionDto promotion = promotionService.findBestPromotion(projection.getId(), projection.getPrice());

        List<VariantDto> variants = variantService.getVariantsByProduct(projection.getId())
                .stream()
                .map(v -> {
                    BigDecimal basePrice = v.price();
                    BigDecimal specialPrice = basePrice;

                    if (promotion != null) {
                        specialPrice = PromotionType.fromCode(promotion.getDiscountType())
                                .map(type -> switch (type) {
                                    case PERCENTAGE -> basePrice.subtract(
                                            basePrice.multiply(
                                                    promotion.getDiscountValue()
                                                            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                                            )
                                    );
                                    case AMOUNT -> basePrice.subtract(promotion.getDiscountValue());
                                })
                                .orElse(basePrice);
                    }

                    return VariantDto.builder()
                            .id(v.id())
                            .sku(v.sku())
                            .thumbnail(v.thumbnail())
                            .price(basePrice)
                            .specialPrice(specialPrice)
                            .attributes(v.attributes())
                            .availableStock(v.availableStock())
                            .build();
                })
                .toList();

        List<MediaDto> medias = mediaService.findByProductId(projection.getId());

        return ProductDetailDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .slug(projection.getSlug())
                .thumbnail(projection.getThumbnail())
                .detail(projection.getDetail())
                .isAvailable(projection.getAvailableStock() > 0)
                .rating(new ProductDetailDto.RatingSummary(
                        projection.getTotal() != null ? projection.getTotal() : 0L,
                        projection.getAverage() != null ? projection.getAverage() : 0.0
                ))
                .variants(variants)
                .medias(medias)
                .promotion(promotion)
                .build();
    }

    private double calculateBaseScore(ProductProjection p) {
        double ratingScore = p.getTotal() != null ? p.getAverage() * 5 : 0;
        double soldScore = p.getSold() != null ? p.getSold() * 0.5 : 0;
        return ratingScore + soldScore;
    }
}
