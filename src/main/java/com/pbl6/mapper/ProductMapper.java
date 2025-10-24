package com.pbl6.mapper;

import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.ProductEntity;
import com.pbl6.entities.PromotionEntity;
import com.pbl6.entities.ReviewEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final PromotionMapper promotionMapper;
    private final VariantMapper variantMapper;
    private final MediaMapper mediaMapper;

    public ProductDto toDto(ProductEntity e, List<PromotionEntity> promos) {
        return ProductDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .slug(e.getSlug())
                .thumbnail(e.getThumbnail())
                .price(e.getPrice())
                .isActive(e.getIsActive())
                .promotions(promos.stream().map(promotionMapper::toDto).toList())
                .special_price(e.getDiscountedPrice())
                .stock(e.getStock() != null ? e.getStock() : 0)
                .reserved_stock(e.getReservedStock())
                .available_stock(e.getAvailableStock())
                .rating(new ProductDto.RatingSummary(
                        e.getTotalRating(),
                        e.getAverageRating()
                ))
                .build();
    }

    public ProductDto toDto(ProductEntity e) {
        return ProductDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .slug(e.getSlug())
                .thumbnail(e.getThumbnail())
                .price(e.getPrice())
                .isActive(e.getIsActive())
                .promotions(null)
                .special_price(e.getDiscountedPrice())
                .stock(e.getStock() != null ? e.getStock() : 0)
                .reserved_stock(e.getReservedStock())
                .available_stock(e.getAvailableStock())
                .rating(new ProductDto.RatingSummary(
                        e.getTotalRating(),
                        e.getAverageRating()
                ))
                .build();
    }

    public ProductDetailDto toDetailDto(ProductEntity product,
                                  Map<Long, List<PromotionEntity>> promoMap) {

        List<PromotionEntity> promos = promoMap.getOrDefault(product.getId(), List.of());
        ProductDetailDto.RatingSummary rating = calculateRatingSummary(product.getReviews());
        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .thumbnail(product.getThumbnail())
                .detail(product.getDetail())
                .isAvailable(product.getAvailableStock() > 0)
                .isActive(product.getIsActive())
                .rating(rating)
                .variants(variantMapper.toDtoList(product.getVariants()))
                .medias(mediaMapper.toDtoList(product.getMedias()))
                .promotions(promos.isEmpty() ? null :
                        promos.stream().map(promotionMapper::toDto).toList())
                .build();
    }

    private ProductDetailDto.RatingSummary calculateRatingSummary(List<ReviewEntity> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new ProductDetailDto.RatingSummary(0, 0, 0, 0, 0, 0, 0.0);
        }

        long star1 = reviews.stream().filter(r -> r.getRating() == 1).count();
        long star2 = reviews.stream().filter(r -> r.getRating() == 2).count();
        long star3 = reviews.stream().filter(r -> r.getRating() == 3).count();
        long star4 = reviews.stream().filter(r -> r.getRating() == 4).count();
        long star5 = reviews.stream().filter(r -> r.getRating() == 5).count();

        long total = reviews.size();
        double average = reviews.stream()
                .mapToInt(ReviewEntity::getRating)
                .average()
                .orElse(0.0);

        return new ProductDetailDto.RatingSummary(total, star1, star2, star3, star4, star5, average);
    }

}
