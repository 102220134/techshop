package com.pbl6.mapper;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.response.ProductDetailDto;
import com.pbl6.dtos.response.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(ProductProjection projection) {
        if (projection == null) {
            return null;
        }
        return ProductDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .slug(projection.getSlug())
                .thumbnail(projection.getThumbnail())
                .price(projection.getPrice())
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

    public ProductDetailDto toDetailDto(ProductProjection projection) {
        if (projection == null) {
            return null;
        }
        return ProductDetailDto.builder()
                .id(projection.getId())
                .name(projection.getName())
                .description(projection.getDescription())
                .slug(projection.getSlug())
                .thumbnail(projection.getThumbnail())
                .detail(projection.getDetail())
                .isAvailable(projection.getAvailableStock()>0)
                .rating(new ProductDetailDto.RatingSummary(
                        projection.getTotal() != null ? projection.getTotal() : 0L,
                        projection.getAverage() != null ? projection.getAverage() : 0.0
                ))
                .build();
    }
}
