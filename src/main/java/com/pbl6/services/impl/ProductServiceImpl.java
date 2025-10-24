package com.pbl6.services.impl;

import com.pbl6.dtos.request.product.*;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.*;
import com.pbl6.enums.MediaType;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.MediaMapper;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.mapper.PromotionMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.*;
import com.pbl6.specifications.ProductSpecifications;
import com.pbl6.utils.CloudinaryUtil;
import com.pbl6.utils.EntityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryService categoryService;
    private final WareHouseRepository wareHouseRepository;
    private final EntityUtil entityUtil;
    private final ProductMapper productMapper;
    private final MediaService mediaService;
    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;
    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository pavRepository;
    private final MediaMapper mediaMapper;
    private final CategoryRepository categoryRepository;
    private final MediaRepository mediaRepository;
    private final VariantRepository variantRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final VariantAttributeValueRepository vavRepo;

    // --------------------------------------------------------------
    // FEATURED PRODUCTS
    // --------------------------------------------------------------
    @Override
    public List<ProductDto> getFeaturedProducts(String slugPath, Integer size) {
        size = size == null ? 20 : size;
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
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
    public Page<ProductDto> filterProduct(String slugPath, ProductFilterRequest req) {
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);
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
    public Page<ProductDto> searchProduct(ProductSearchRequest req) {
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.keyword(req.getQ()));
        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize());
        Page<ProductEntity> productsPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productsPage);
    }

    // --------------------------------------------------------------
    // PRODUCT DETAIL
    // --------------------------------------------------------------
    @Override
    public ProductDetailDto getProductDetail(String slug) {
        ProductEntity product = productRepository.findBySlugAndIsActive(slug, true)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        return mapToDetail(product);

    }


    @Override
    public Page<ProductDto> filterProducts(AdminSearchProductRequest req) {
        Long cateId = req.getCategoryId() == null ? 3 : req.getCategoryId();
        if (!categoryRepository.existsById(cateId)) {
            throw new AppException(ErrorCode.NOT_FOUND, "Category not found");
        }
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.keyword(req.getKeyword()))
                .and(ProductSpecifications.byCategory(cateId))
                .and(ProductSpecifications.priceRange(req.getPrice_from(), req.getPrice_to()));

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(req.getDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                mapSortField(req.getOrder())
        );

        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<ProductEntity> productEntityPage = productRepository.findAll(spec, pageable);

        Page<ProductEntity> productsPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productsPage);
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

    @Override
    @Transactional
    public ProductDetailDto createProduct(CreateProductRequest request) {
        ProductEntity product = new ProductEntity();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setDetail(request.getDetail());
        product.setIsActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getRelatedName() != null) {
            product.setRelatedName(request.getRelatedName());
        }

        // ✅ Thumbnail upload
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            String thumbnailUrl = cloudinaryUtil.uploadImage(request.getThumbnail(), request.getSlug());
            product.setThumbnail(thumbnailUrl);
        }

        // ✅ Gán danh mục
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<CategoryEntity> categories = categoryRepository.findAllById(request.getCategoryIds());
            product.setCategories(categories);
        }

        // ✅ Lưu product để có ID
        product = productRepository.save(product);


        if (request.getFilters() != null) {
            for (AttributeRequest attrReq : request.getFilters()) {
                ProductAttributeValueEntity pav = new ProductAttributeValueEntity();
                pav.setProduct(product);
                AttributeEntity attribute = attributeRepository.findByCodeAndIsFilterTrue(attrReq.getCode()).orElseThrow(
                        () -> new AppException(ErrorCode.NOT_FOUND, "fillter not found")
                );
                AttributeValueEntity attributeValue = attributeValueRepository
                        .findByValueAndAttributeId(attrReq.getValue(),attribute.getId())
                        .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND,"filter value not found"));
                pav.setAttribute(attribute);
                pav.setAttributeValue(attributeValue);
                pavRepository.save(pav);
            }
        }


        // ✅ Related products
        if (request.getSibling() != null) {
            ProductEntity sibling = productRepository.findById(request.getSibling()).orElseThrow(
                    () -> new AppException(ErrorCode.NOT_FOUND, "sibling not found")
            );
            Set<ProductEntity> relatedProducts = sibling.getRelatedProducts();
            relatedProducts.add(sibling);
            product.setRelatedProducts(relatedProducts);
        }

        // ✅ Medias
        if (request.getMedias() != null) {
            for (CreateProductRequest.MediaRequest m : request.getMedias()) {
                switch (m.getType()){
                    case IMAGE -> {
                        MediaEntity media = new MediaEntity();
                        media.setProduct(product);
                        media.setMediaType(m.getType());
                        media.setSortOrder(m.getSortOrder());
                        media.setUrl(cloudinaryUtil.uploadImage(m.getFile(), request.getSlug() + UUID.randomUUID()));
                        media.setCreatedAt(LocalDateTime.now());
                        mediaRepository.save(media);
                    }
                    default -> throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR,"media chưa support type video");
                }
            }
        }

        // ✅ Variants
        if (request.getVariants() != null) {
            for (CreateVariantRequest vReq : request.getVariants()) {
                VariantEntity variant = new VariantEntity();
                variant.setProduct(product);
                variant.setSku(vReq.getSku());
                variant.setPrice(vReq.getPrice());
                variant.setIsActive(true);
                variant.setCreatedAt(LocalDateTime.now());
                variant.setUpdatedAt(LocalDateTime.now());
                variantRepository.save(variant);

                // ✅ Thuộc tính biến thể
                if (vReq.getOptions() != null) {
                    for (AttributeRequest attrReq : request.getFilters()) {
                        VariantAttributeValueEntity vav = new VariantAttributeValueEntity();
                        vav.setVariant(variant);
                        AttributeEntity attribute = attributeRepository.findByCodeAndIsOptionTrue(attrReq.getCode()).orElseThrow(
                                () -> new AppException(ErrorCode.NOT_FOUND, "option not found")
                        );
                        AttributeValueEntity attributeValue = attributeValueRepository
                                .findByValueAndAttributeId(attrReq.getValue(),attribute.getId())
                                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND,"option value not found"));
                        vav.setAttribute(attribute);
                        vav.setAttributeValue(attributeValue);
                        vavRepo.save(vav);
                    }
                }
            }
        }
        return mapToDetail(productRepository.save(product));
    }

    @Transactional
    public ProductDetailDto updateProduct(Long id, UpdateProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND,"Product not found"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getDetail() != null) {
            product.setDetail(request.getDetail());
        }

        if (request.getSlug() != null) {
            product.setSlug(request.getSlug());
        }

        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            String imageUrl = cloudinaryUtil.uploadImage(request.getThumbnail(),product.getSlug());
            product.setThumbnail(imageUrl);
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            var categories = categoryRepository.findAllById(request.getCategoryIds());
            product.setCategories(categories);
        }

        if (request.getFilters() != null) {
            for (AttributeRequest attrReq : request.getFilters()) {
                ProductAttributeValueEntity pav = new ProductAttributeValueEntity();
                pav.setProduct(product);
                AttributeEntity attribute = attributeRepository.findByCodeAndIsFilterTrue(attrReq.getCode()).orElseThrow(
                        () -> new AppException(ErrorCode.NOT_FOUND, "fillter not found")
                );
                AttributeValueEntity attributeValue = attributeValueRepository
                        .findByValueAndAttributeId(attrReq.getValue(),attribute.getId())
                        .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND,"filter value not found"));
                pav.setAttribute(attribute);
                pav.setAttributeValue(attributeValue);
                pavRepository.save(pav);
            }
        }

        if (request.getSibling() != null) {
            ProductEntity sibling = productRepository.findById(request.getSibling()).orElseThrow(
                    () -> new AppException(ErrorCode.NOT_FOUND, "sibling not found")
            );
            Set<ProductEntity> relatedProducts = sibling.getRelatedProducts();
            relatedProducts.add(sibling);
            product.setRelatedProducts(relatedProducts);
        }

        if (request.getRelatedName() != null) {
            product.setRelatedName(request.getRelatedName());
        }

        return mapToDetail(productRepository.save(product));
    }


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

    private ProductDetailDto mapToDetail(ProductEntity product) {
        Map<Long, List<PromotionEntity>> promoMap =
                promotionService.getActivePromotionsGroupedByProduct(List.of(product.getId()));
        List<ProductDetailDto.SiblingDto> siblings = product.getRelatedProducts().stream()
                .map(sibling ->{
                    return ProductDetailDto.SiblingDto.builder()
                            .id(sibling.getId())
                            .name(sibling.getName())
                            .related_name(sibling.getRelatedName())
                            .slug(sibling.getSlug())
                            .thumbnail(sibling.getThumbnail())
                            .build();
                }).toList();

        ProductDetailDto productDetailDto = productMapper.toDetailDto(product, promoMap);
        productDetailDto.setSiblings(siblings);
        productDetailDto.setBreadcrumb(categoryService.getBreadcrumbByProductSlug(product.getSlug()));
        return productDetailDto;
    }

}
