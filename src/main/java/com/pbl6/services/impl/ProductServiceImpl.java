package com.pbl6.services.impl;

import com.pbl6.dtos.request.product.ProductFilterRequest;
import com.pbl6.dtos.request.product.ProductSearchRequest;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.CategoryEntity;
import com.pbl6.entities.ProductEntity;
import com.pbl6.entities.PromotionEntity;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.MediaMapper;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.mapper.PromotionMapper;
import com.pbl6.repositories.ProductAttributeValueRepository;
import com.pbl6.repositories.ProductRepository;
import com.pbl6.repositories.WareHouseRepository;
import com.pbl6.services.*;
import com.pbl6.specifications.ProductSpecifications;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryService categoryService;
    private final WareHouseRepository wareHouseRepository;
    private final EntityUtil entityUtil;
    private final ProductMapper productMapper;
    private final VariantService variantService;
    private final MediaService mediaService;
    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;
    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final MediaMapper mediaMapper;

    // --------------------------------------------------------------
    // FEATURED PRODUCTS
    // --------------------------------------------------------------
    @Override
    public List<ProductDto> getFeaturedProducts(String slugPath, Integer size) {
        size = size == null ? 20 : size;
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(categoryEntity, false);
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.isActive(true))
                .and(ProductSpecifications.byCategory(categoryEntity.getId()))
                .and(ProductSpecifications.onlyInStock(true));
        List<ProductEntity> allProducts = productRepository.findAll(spec);
        List<ProductEntity> topProducts = allProducts.stream()
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
    public List<ProductDto> getBestSellerProducts(String slug, Integer size) {
        size = size == null ? 20 : size;
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slug);
        entityUtil.ensureActive(categoryEntity, false);
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.isActive(true))
                .and(ProductSpecifications.byCategory(categoryEntity.getId()))
                .and(ProductSpecifications.onlyInStock(true));
        Sort sort = Sort.by(Sort.Direction.DESC, mapSortField("sold"));
        Pageable pageable = PageRequest.of(0, size, sort);
        Page<ProductEntity> productEntityPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productEntityPage.getContent());
    }

    // --------------------------------------------------------------
    // FILTER PRODUCT
    // --------------------------------------------------------------
    @Override
    public Page<ProductDto> filterProduct(String slugPath, ProductFilterRequest req, boolean includeInactive) {
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
        entityUtil.ensureActive(categoryEntity, false);
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.isActive(true))
                .and(ProductSpecifications.byCategory(categoryEntity.getId()))
                .and(ProductSpecifications.priceRange(req.getPrice_from(), req.getPrice_to()))
                .and(ProductSpecifications.onlyInStock(true))
                .and(ProductSpecifications.attributes(req.getFilter()));

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(req.getDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                mapSortField(req.getOrder())
        );

        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<ProductEntity> productEntityPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productEntityPage);
    }

    // --------------------------------------------------------------
    // SEARCH PRODUCT
    // --------------------------------------------------------------
    @Override
    public Page<ProductDto> searchProduct(ProductSearchRequest req, boolean includeInactive) {
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.isActive(true))
                .and(ProductSpecifications.keyword(req.getQ()));
        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize());
        Page<ProductEntity> productsPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productsPage);
    }

    // --------------------------------------------------------------
    // PRODUCT DETAIL
    // --------------------------------------------------------------
    @Override
    public ProductDetailDto getProductDetail(String slug, boolean includeInactive) {
        ProductEntity product = includeInactive
                ? productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND))
                : productRepository.findBySlugAndIsActive(slug, true)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        Map<Long, List<PromotionEntity>> promoMap =
                promotionService.getActivePromotionsGroupedByProduct(List.of(product.getId()));
        List<ProductDetailDto.SiblingDto> siblings = product.getRelatedProducts().stream()
                .map(productSibling -> {
                    var optionAttrs = productAttributeValueRepository.findOptionAttributesByProductId(productSibling.getId());
                    String relatedName = optionAttrs.stream()
                            .filter(pav -> "version".equals(pav.getAttribute().getCode()))
                            .findFirst().map(pav -> pav.getAttributeValue().getLabel())
                            .orElse(null);
                    return ProductDetailDto.SiblingDto.builder()
                            .id(productSibling.getId())
                            .slug(productSibling.getSlug())
                            .name(productSibling.getName())
                            .related_name(relatedName)
                            .thumbnail(productSibling.getThumbnail())
                            .build();
                }).toList();

        ProductDetailDto productDetailDto = productMapper.toDetailDto(product, promoMap);
        productDetailDto.setSiblings(siblings);
        productDetailDto.setBreadcrumb(categoryService.getBreadcrumbByProductSlug(product.getSlug()));
        return productDetailDto;
    }


    // --------------------------------------------------------------
    // SHARED PROMOTION LOGIC
    // --------------------------------------------------------------

    private List<ProductDto> applyPromotions(List<ProductEntity> productEntities) {
        if (productEntities.isEmpty()) return List.of();
        List<Long> productIds = productEntities.stream().map(ProductEntity::getId).toList();
        Map<Long, List<PromotionEntity>> promoMap = promotionService.getActivePromotionsGroupedByProduct(productIds);
        return productEntities.stream()
                .map(p -> {
                    List<PromotionEntity> promos = promoMap.getOrDefault(p.getId(), List.of());
                    BigDecimal discounted = p.getDiscountedPrice() != null
                            ? p.getDiscountedPrice()
                            : p.getPrice();
                    return productMapper.toDto(p, promos);
                })
                .toList();
    }

    private Page<ProductDto> applyPromotions(Page<ProductEntity> page) {
        List<Long> productIds = page.getContent().stream().map(ProductEntity::getId).toList();
        Map<Long, List<PromotionEntity>> promoMap = promotionService.getActivePromotionsGroupedByProduct(productIds);

        return page.map(p -> {
            List<PromotionEntity> promos = promoMap.getOrDefault(p.getId(), List.of());

            BigDecimal discounted = p.getDiscountedPrice() != null
                    ? p.getDiscountedPrice()
                    : p.getPrice();

            return productMapper.toDto(p, promos);
        });
    }

    private double calculateBaseScore(ProductEntity p) {
        // --- Dữ liệu đầu vào giả định: không null ---
        double rating = p.getAverageRating();      // Đã có COALESCE() trong @Formula
        long totalRating = p.getTotalRating();     // Đã có COALESCE()
        int sold = p.getSold();                    // Đã có COALESCE()
        int stock = p.getStock();                  // Đã có COALESCE()
        int reserved = p.getReservedStock();       // Đã có COALESCE()
        LocalDateTime createdAt = p.getCreatedAt(); // Được đảm bảo luôn có (vd. set khi insert)

        // --- Tính toán các thành phần ---
        double availableStock = Math.max(stock - reserved, 0);
        double stockScore = availableStock > 0 ? 1 : 0; // Ưu tiên sản phẩm còn hàng

        // RatingScore: đánh giá cao + nhiều người đánh giá => điểm cao
        double ratingScore = (rating / 5.0) * Math.log10(totalRating + 1) * 60; // max ~60

        // SoldScore: bán càng nhiều => càng nổi bật
        double soldScore = Math.log10(sold + 1) * 30; // max ~30

        // RecencyScore: sản phẩm mới (<90 ngày) được cộng điểm nhẹ
        long daysOld = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        double recencyScore = (daysOld < 90) ? (30 - daysOld * 0.33) : 0; // max 30, giảm dần theo ngày

        // --- Tổng hợp ---
        return ratingScore + soldScore + recencyScore + (stockScore * 10);
    }


    private String mapSortField(String field) {
        return switch (field) {
            case "price" -> "discountedPrice";
            case "rating" -> "average";
            case "createdAt" -> "createdAt";
            case "sold" -> "sold";
            default -> "id";
        };
    }


}
