package com.pbl6.mapper;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.PromotionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final PromotionMapper promotionMapper;

    /**
     * Mapper nâng cao có tính giá giảm và promotion.
     */
    public ProductDto toDto(ProductProjection projection,
                            BigDecimal finalPrice,
                            List<PromotionEntity> promotions) {
        if (projection == null) return null;

        PromotionEntity appliedPromotion = (promotions != null && !promotions.isEmpty())
                ? promotions.get(0) // nếu muốn lấy best promotion, có thể xử lý ngoài
                : null;

        return ProductDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .slug(projection.getSlug())
                .thumbnail(projection.getThumbnail())
                .price(projection.getPrice())
                .special_price(finalPrice)
                .promotions(promotions.isEmpty() ? null : promotions.stream().map(promotionMapper::toDto).toList())
                .stock(projection.getStock() != null ? projection.getStock() : 0)
                .reserved_stock(projection.getReservedStock() != null ? projection.getReservedStock() : 0)
                .available_stock(projection.getAvailableStock())
                .sold(projection.getSold() != null ? projection.getSold() : 0)
                .rating(new ProductDto.RatingSummary(
                        projection.getTotal() != null ? projection.getTotal() : 0L,
                        projection.getAverage() != null ? projection.getAverage() : 0.0
                ))
                .build();
    }
}
