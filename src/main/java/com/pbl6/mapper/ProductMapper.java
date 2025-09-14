package com.pbl6.mapper;

import com.pbl6.dtos.projection.ProductProjection;
import com.pbl6.dtos.response.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(ProductProjection p){
        double score = (p.getSold() * 0.5) + (p.getTotal() * 0.2) + (p.getAverage() * 20 * 0.2);
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .slug(p.getSlug())
                .thumbnail(p.getThumbnail())
                .price(p.getPrice())
                .stock(p.getStock())
                .reservedStock(p.getReservedStock())
                .availableStock(p.getStock()-p.getReservedStock())
                .sold(p.getSold())
                .score(score)
                .rating(
                        new ProductDto.RatingSummary(
                                p.getTotal(),
                                p.getAverage() == null ? 0 : p.getAverage()
                        ))
                .build();
    }
}
