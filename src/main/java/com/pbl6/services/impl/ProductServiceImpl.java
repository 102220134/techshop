package com.pbl6.services.impl;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.request.product.ProductSearchRequest;
import com.pbl6.dtos.response.product.MediaDto;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.dtos.response.product.VariantDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.PromotionEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.mapper.PromotionMapper;
import com.pbl6.repositories.ProductAttributeValueRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.ProductRepositoryCustom;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.*;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
    private final PromotionMapper promotionMapper;
    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;

    // --------------------------------------------------------------
    // FEATURED PRODUCTS
    // --------------------------------------------------------------
    @Override
    public List<ProductDto> getFeaturedProducts(String slugPath, int size) {
        CategoryEntity category = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(category, false);

        List<ProductProjection> products = productRepositoryCustom.findAllByCategoryId(category.getId(), false, true);
        List<ProductProjection> topProducts = products.stream()
                .filter(p -> p.getAvailableStock() > 0)
                .sorted(Comparator.comparingDouble(this::calculateBaseScore).reversed())
                .limit(size)
                .toList();

        return applyPromotions(topProducts);
    }

    // --------------------------------------------------------------
    // BEST SELLER PRODUCTS
    // --------------------------------------------------------------
    @Override
    public List<ProductDto> getBestSellerProducts(String slug, int size) {
        CategoryEntity category = categoryService.resolveBySlugPath(slug);
        entityUtil.ensureActive(category, false);

        List<ProductProjection> products = productRepositoryCustom.findAllByCategoryId(category.getId(), false, true);
        List<ProductProjection> topProducts = products.stream()
                .filter(p -> p.getAvailableStock() > 0)
                .sorted(Comparator.comparingInt(ProductProjection::getSold).reversed())
                .limit(size)
                .toList();

        return applyPromotions(topProducts);
    }

    // --------------------------------------------------------------
    // FILTER PRODUCT
    // --------------------------------------------------------------
    @Override
    public Page<ProductDto> filterProduct(String slugPath, ProductFilterRequest req, boolean includeInactive) {
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(categoryEntity, false);

        Page<ProductProjection> projectionPage =
                productRepositoryCustom.filterProducts(categoryEntity.getId(), req, includeInactive, true);

        return applyPromotions(projectionPage);
    }

    // --------------------------------------------------------------
    // SEARCH PRODUCT
    // --------------------------------------------------------------
    @Override
    public Page<ProductDto> searchProduct(ProductSearchRequest req, boolean includeInactive) {
        Page<ProductProjection> projectionPage =
                productRepositoryCustom.searchProductByKeyword(req, includeInactive, true);
//        return projectionPage.map(p -> ProductDto.builder().name(p.getName()).build());
        return applyPromotions(projectionPage);
    }

    // --------------------------------------------------------------
    // PRODUCT DETAIL
    // --------------------------------------------------------------
    @Override
    public ProductDetailDto getProductDetail(String slug, boolean includeInactive) {
        ProductProjection projection = productRepositoryCustom.findBySlug(slug, includeInactive)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        Map<Long, List<PromotionEntity>> promoMap =
                promotionService.getActivePromotionsGroupedByProduct(List.of(projection.getId()));

        List<PromotionEntity> promos = promoMap.getOrDefault(projection.getId(), List.of());
//        BigDecimal finalPrice = promotionService.calculateFinalPrice(
//                projection.getPrice(),
//                promos
//        );

        List<VariantDto> variants = variantService.getVariantsByProduct(projection.getId())
                .stream()
                .map(v -> {
                    BigDecimal basePrice = v.price();
                    BigDecimal specialPrice = promotionService.calculateFinalPrice(
                            basePrice,
                            promos
                    );
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

        List<ProductDetailDto.SiblingDto> siblings = productRepository.findSiblingsByProductId(projection.getId())
                .stream()
                .map(product -> {
                    // Lấy attribute option (nếu có)
                    var optionAttrs = productAttributeValueRepository.findOptionAttributesByProductId(product.getId());

                    String relatedName = optionAttrs.stream()
                            .filter(pav -> "version".equals(pav.getAttribute().getCode()))
                            .findFirst() // trả về Optional<ProductAttributeValueEntity>
                            .map(pav -> pav.getAttributeValue().getLabel()) // ánh xạ sang label
                            .orElse(null); // nếu không có thì null



                    return ProductDetailDto.SiblingDto.builder()
                            .id(product.getId())
                            .slug(product.getSlug())
                            .name(product.getName())
                            .related_name(relatedName)
                            .thumbnail(product.getThumbnail())
                            .build();
                })
                .toList();

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
                .promotions(promos.isEmpty() ? null : promos.stream().map(promotionMapper::toDto).toList())
                .siblings(siblings)
                .build();
    }

    // --------------------------------------------------------------
    // SHARED PROMOTION LOGIC
    // --------------------------------------------------------------

    private List<ProductDto> applyPromotions(List<ProductProjection> projections) {
        if (projections.isEmpty()) return List.of();

        List<Long> productIds = projections.stream().map(ProductProjection::getId).toList();
        Map<Long, List<PromotionEntity>> promoMap = promotionService.getActivePromotionsGroupedByProduct(productIds);

        return projections.stream()
                .map(p -> {
                    List<PromotionEntity> promos = promoMap.getOrDefault(p.getId(), List.of());
                    BigDecimal finalPrice = promotionService.calculateFinalPrice(
                            p.getPrice(),
                            promos
                    );
                    return productMapper.toDto(p, finalPrice, promos);
                })
                .toList();
    }

    private Page<ProductDto> applyPromotions(Page<ProductProjection> page) {
        List<Long> productIds = page.getContent().stream().map(ProductProjection::getId).toList();
        Map<Long, List<PromotionEntity>> promoMap = promotionService.getActivePromotionsGroupedByProduct(productIds);

        return page.map(p -> {
            List<PromotionEntity> promos = promoMap.getOrDefault(p.getId(), List.of());
            BigDecimal finalPrice = promotionService.calculateFinalPrice(
                    p.getPrice(),
                    promos
            );
            return productMapper.toDto(p, finalPrice, promos);
        });
    }

    // --------------------------------------------------------------
    // PRICE CALCULATION
    // --------------------------------------------------------------

//    public BigDecimal calculateFinalPrice(BigDecimal basePrice, List<PromotionEntity> promotions) {
//        if (basePrice == null || promotions == null || promotions.isEmpty()) {
//            return basePrice;
//        }
//
//        List<PromotionEntity> sortedPromos = promotions.stream()
//                .sorted(Comparator.comparingInt(p -> Optional.ofNullable(p.getPriority()).orElse(0)))
//                .toList();
//
//        BigDecimal currentPrice = basePrice;
//        for (PromotionEntity promo : sortedPromos) {
//            currentPrice = applyPromotion(currentPrice, promo);
//            if (Boolean.TRUE.equals(promo.getExclusive())) break;
//        }
//
//        return currentPrice.max(BigDecimal.ZERO);
//    }

//    private BigDecimal applyPromotion(BigDecimal basePrice, PromotionEntity promo) {
//        if (promo == null || basePrice == null) return basePrice;
//
//        BigDecimal discount = switch (promo.getDiscountType()) {
//            case PERCENTAGE -> basePrice
//                    .multiply(promo.getDiscountValue())
//                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
//            case AMOUNT -> promo.getDiscountValue();
//        };
//
//        if (promo.getMaxDiscountValue() != null &&
//            discount.compareTo(promo.getMaxDiscountValue()) > 0) {
//            discount = promo.getMaxDiscountValue();
//        }
//
//        return basePrice.subtract(discount).max(BigDecimal.ZERO);
//    }

    private double calculateBaseScore(ProductProjection p) {
        double ratingScore = p.getTotal() != null ? p.getAverage() * 5 : 0;
        double soldScore = p.getSold() != null ? p.getSold() * 0.5 : 0;
        return ratingScore + soldScore;
    }
}
