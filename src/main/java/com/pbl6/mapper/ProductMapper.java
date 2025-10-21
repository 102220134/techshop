package com.pbl6.mapper;

import com.pbl6.dtos.response.product.ProductDetailDto;
import com.pbl6.dtos.response.product.ProductDto;
import com.pbl6.entities.ProductEntity;
import com.pbl6.entities.PromotionEntity;
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
    public ProductDetailDto toDetailDto(ProductEntity product,
                                  Map<Long, List<PromotionEntity>> promoMap) {

        List<PromotionEntity> promos = promoMap.getOrDefault(product.getId(), List.of());

        return ProductDetailDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .slug(product.getSlug())
                .thumbnail(product.getThumbnail())
                .detail(product.getDetail())
                .isAvailable(product.getAvailableStock() > 0)
                .rating(new ProductDetailDto.RatingSummary(
                        product.getTotalRating(),
                        product.getAverageRating()
                ))
                .variants(variantMapper.toDtoList(product.getVariants()))
                .medias(mediaMapper.toDtoList(product.getMedias()))
                .promotions(promos.isEmpty() ? null :
                        promos.stream().map(promotionMapper::toDto).toList())
                .build();
    }
}
