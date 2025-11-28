package com.pbl6.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl6.dtos.request.product.*;
import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.*;
import com.pbl6.exceptions.AppException;
import com.pbl6.exceptions.ErrorCode;
import com.pbl6.mapper.ProductMapper;
import com.pbl6.repositories.*;
import com.pbl6.services.*;
import com.pbl6.specifications.ProductSpecifications;
import com.pbl6.utils.CloudinaryUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    private final ProductMapper productMapper;
    private final PromotionService promotionService;
    private final ProductRepository productRepository;
    private final ProductAttributeValueRepository pavRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryUtil cloudinaryUtil;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts(String slugPath, Integer size) {
        size = size == null ? 20 : size;
        CategoryEntity categoryEntity = categoryService.resolveBySlugPath(slugPath);

        // CẢNH BÁO: findAll(spec) sẽ load toàn bộ sản phẩm của Category lên RAM.
        // Nếu data lớn (>1000 sp/cate), nên cân nhắc lưu field "score" vào DB để sort bằng SQL.
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

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProduct(ProductSearchRequest req) {
        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.keyword(req.getQ()));
        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize());
        Page<ProductEntity> productsPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productsPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(String slug) {
        ProductEntity product = productRepository.findBySlugAndIsActive(slug, true)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        return mapToDetail(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));
        return mapToDetail(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> filterProducts(AdminSearchProductRequest req) {
        Long cateId = req.getCategoryId();

        Specification<ProductEntity> spec = Specification
                .where(ProductSpecifications.keyword(req.getKeyword()))
                .and(ProductSpecifications.priceRange(req.getPrice_from(), req.getPrice_to()));

        if (cateId != null) {
            if (!categoryRepository.existsById(cateId)) {
                throw new AppException(ErrorCode.NOT_FOUND, "Category not found");
            }
            spec = spec.and(ProductSpecifications.byCategory(cateId));
        }

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(req.getDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                mapSortField(req.getOrder())
        );

        Pageable pageable = PageRequest.of(req.getPage() - 1, req.getSize(), sort);
        Page<ProductEntity> productsPage = productRepository.findAll(spec, pageable);
        return applyPromotions(productsPage);
    }

    // --------------------------------------------------------------
    // WRITE OPERATIONS
    // --------------------------------------------------------------

    @Override
    @Transactional
    public ProductDetailDto createProduct(CreateProductRequest request) {
        productRepository.findBySlug(request.getSlug()).ifPresent(p -> {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Slug sản phẩm đã tồn tại");
        });
        ProductEntity product = new ProductEntity();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSlug(request.getSlug());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getDetail() != null && !request.getDetail().isBlank()) {
            try {
                JsonNode detailNode = objectMapper.readTree(request.getDetail());
                product.setDetail(detailNode);
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Format JSON của 'detail' không hợp lệ");
            }
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getRelatedName() != null) product.setRelatedName(request.getRelatedName());

        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            String thumbnailUrl = cloudinaryUtil.uploadThumbnail(request.getThumbnail(), request.getSlug());
            product.setThumbnail(thumbnailUrl);
        }

        if (request.getCategoryId() != null) {
            List<CategoryEntity> categories = categoryService.getAllParents(request.getCategoryId());
            product.setCategories(categories);
        }

        // 1. Lưu lần đầu để có ID
        ProductEntity savedProduct = productRepository.save(product);

        // 2. Lưu Attributes
        if (request.getFilters() != null && !request.getFilters().isBlank()) {
            try {
                List<AttributeRequest> filterList = Arrays.asList(
                        objectMapper.readValue(request.getFilters(), AttributeRequest[].class)
                );
                // Save list, không cần set ngược lại vào savedProduct vì tí nữa refresh sẽ tự load
                pavRepository.saveAll(processProductAttributes(filterList, savedProduct));
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Format JSON của 'filters' không hợp lệ");
            }
        }

        // 3. Update Sibling (Thay đổi quan hệ trong RAM)
        if (request.getSibling() != null) {
            updateSiblingRelationship(savedProduct, request.getSibling());
        }

        // ✅ FIX QUAN TRỌNG: Đẩy dữ liệu xuống DB trước khi Refresh
        // Nếu không có dòng này, quan hệ sibling vừa set ở trên sẽ bị refresh xóa mất.
        productRepository.flush();

        // 4. Refresh để load lại đầy đủ thông tin (bao gồm @Formula, updated relations)
        entityManager.refresh(savedProduct);

        return mapToDetail(savedProduct);
    }

    @Override
    @Transactional
    public ProductDetailDto updateProduct(Long id, UpdateProductRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Product not found"));

        // Set các trường cơ bản
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getDetail() != null && !request.getDetail().isBlank()) {
            try {
                JsonNode detailNode = objectMapper.readTree(request.getDetail());
                product.setDetail(detailNode);
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Format JSON của 'detail' không hợp lệ");
            }
        }
        if (request.getSlug() != null) product.setSlug(request.getSlug());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getRelatedName() != null) product.setRelatedName(request.getRelatedName());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            String imageUrl = cloudinaryUtil.uploadThumbnail(request.getThumbnail(), product.getSlug());
            product.setThumbnail(imageUrl);
        }

        if (request.getCategoryId() != null) {
            var categories = categoryService.getAllParents(request.getCategoryId());
            product.setCategories(categories);
        }

        // Xử lý Attributes
        if (request.getFilters() != null && !request.getFilters().isBlank()) {
            // Xóa cũ
            List<ProductAttributeValueEntity> oldPavs = pavRepository.findByProductId(product.getId());
            if (!oldPavs.isEmpty()) {
                pavRepository.deleteAllInBatch(oldPavs);
                pavRepository.flush(); // Flush ngay để tránh conflict unique constraint
            }
            // Thêm mới
            try {
                List<AttributeRequest> filterList = Arrays.asList(
                        objectMapper.readValue(request.getFilters(), AttributeRequest[].class)
                );
                pavRepository.saveAll(processProductAttributes(filterList, product));
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Format JSON của 'filters' không hợp lệ");
            }
        }

        // Xử lý Sibling
        if (request.getSibling() != null) {
            updateSiblingRelationship(product, request.getSibling());
        }

        // ✅ FIX QUAN TRỌNG: Lưu và Đẩy xuống DB trước khi Refresh
        // Nếu thiếu bước này, refresh() sẽ xóa sạch các thay đổi Name, Desc, Sibling... vừa set ở trên
        ProductEntity savedProduct = productRepository.save(product);
        productRepository.flush();

        // Load lại data mới nhất từ DB
        entityManager.refresh(savedProduct);

        return mapToDetail(savedProduct);
    }

    // --------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------

    // ✅ FIX: Thêm Set để lọc trùng nếu input gửi lên [Red, Red]
    private List<ProductAttributeValueEntity> processProductAttributes(List<AttributeRequest> filters, ProductEntity product) {
        List<ProductAttributeValueEntity> list = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>(); // Chìa khóa để check trùng

        for (AttributeRequest attrReq : filters) {
            AttributeEntity attribute = attributeRepository.findByCodeAndIsFilterTrue(attrReq.getCode()).orElseThrow(
                    () -> new AppException(ErrorCode.NOT_FOUND, "Filter attribute not found: " + attrReq.getCode())
            );
            for (String value : attrReq.getValues()) {
                // Tạo key unique
                String key = attrReq.getCode() + "-" + value;
                if(processedKeys.contains(key)) continue; // Bỏ qua nếu trùng
                processedKeys.add(key);

                ProductAttributeValueEntity pav = new ProductAttributeValueEntity();
                pav.setProduct(product);
                AttributeValueEntity attributeValue = attributeValueRepository
                        .findByValueAndAttributeId(value, attribute.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Filter value not found: " + value));

                pav.setAttribute(attribute);
                pav.setAttributeValue(attributeValue);
                list.add(pav);
            }
        }
        return list;
    }

    private void updateSiblingRelationship(ProductEntity currentProduct, Long siblingId) {
        ProductEntity sibling = productRepository.findById(siblingId).orElseThrow(
                () -> new AppException(ErrorCode.NOT_FOUND, "Sibling product not found")
        );

        Set<ProductEntity> family = new HashSet<>();
        if (sibling.getRelatedProducts() != null) {
            family.addAll(sibling.getRelatedProducts());
        }
        family.add(sibling);

        currentProduct.setRelatedProducts(new HashSet<>(family));

        for (ProductEntity member : family) {
            Set<ProductEntity> memberRelations = member.getRelatedProducts();
            if (memberRelations == null) {
                memberRelations = new HashSet<>();
                member.setRelatedProducts(memberRelations);
            }
            memberRelations.add(currentProduct);
        }
    }

    private String mapSortField(String field) {
        if (field == null) return "id";
        return switch (field) {
            case "price" -> "discountedPrice";
            case "rating" -> "averageRating";
            case "createdAt" -> "createdAt";
            case "sold" -> "sold";
            default -> "id";
        };
    }

    private List<ProductDto> applyPromotions(List<ProductEntity> productEntities) {
        if (productEntities == null || productEntities.isEmpty()) return List.of();
        List<Long> productIds = productEntities.stream().map(ProductEntity::getId).toList();
        Map<Long, List<PromotionEntity>> promoMap = promotionService.getActivePromotionsGroupedByProduct(productIds);
        return productEntities.stream()
                .map(p -> productMapper.toDto(p, promoMap.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    private Page<ProductDto> applyPromotions(Page<ProductEntity> page) {
        if (page.isEmpty()) return Page.empty();
        List<Long> productIds = page.getContent().stream().map(ProductEntity::getId).toList();
        Map<Long, List<PromotionEntity>> promoMap = promotionService.getActivePromotionsGroupedByProduct(productIds);
        return page.map(p -> productMapper.toDto(p, promoMap.getOrDefault(p.getId(), List.of())));
    }

    private double calculateBaseScore(ProductEntity p) {
        double rating = p.getAverageRating();
        long totalRating = p.getTotalRating();
        int sold = p.getSold();
        int stock = p.getStock();
        int reserved = p.getReservedStock();
        LocalDateTime createdAt = p.getCreatedAt();

        double availableStock = Math.max(stock - reserved, 0);
        double stockScore = availableStock > 0 ? 1 : 0;
        double ratingScore = (rating / 5.0) * Math.log10(totalRating + 1) * 60;
        double soldScore = Math.log10(sold + 1) * 30;

        long daysOld = java.time.Duration.between(createdAt, LocalDateTime.now()).toDays();
        double recencyScore = (daysOld < 90) ? (30 - daysOld * 0.33) : 0;

        return ratingScore + soldScore + recencyScore + (stockScore * 10);
    }

    private ProductDetailDto mapToDetail(ProductEntity product) {
        Map<Long, List<PromotionEntity>> promoMap =
                promotionService.getActivePromotionsGroupedByProduct(List.of(product.getId()));

        // Cần đảm bảo relatedProducts được fetch (Lazy loading) trước khi map
        // Hoặc dùng DTO projection trong Repository để tối ưu
        List<ProductDetailDto.SiblingDto> siblings = new ArrayList<>();
        if (product.getRelatedProducts() != null) {
            siblings = product.getRelatedProducts().stream()
                    .map(sibling -> ProductDetailDto.SiblingDto.builder()
                            .id(sibling.getId())
                            .name(sibling.getName())
                            .related_name(sibling.getRelatedName())
                            .slug(sibling.getSlug())
                            .thumbnail(sibling.getThumbnail())
                            .build())
                    .toList();
        }

        ProductDetailDto productDetailDto = productMapper.toDetailDto(product, promoMap);
        productDetailDto.setSiblings(siblings);
        productDetailDto.setBreadcrumb(categoryService.getBreadcrumbByProductSlug(product.getSlug()));
        return productDetailDto;
    }
}