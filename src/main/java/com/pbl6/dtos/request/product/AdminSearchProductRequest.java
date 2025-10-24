package com.pbl6.dtos.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminSearchProductRequest {
    private Long categoryId = 3L;
    private String keyword;
    @Schema(allowableValues = {"id", "price", "create_at", "rating", "sold"})
    private String order = "id";
    @Schema(allowableValues = {"asc", "desc"})
    private String dir = "asc";
    private Integer page = 1;

    private Integer size = 20;

    private BigDecimal price_from = BigDecimal.valueOf(0);

    private BigDecimal price_to = BigDecimal.valueOf(1000000000);
}
