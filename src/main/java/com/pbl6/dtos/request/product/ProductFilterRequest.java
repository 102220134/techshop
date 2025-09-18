package com.pbl6.dtos.request.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

        private String order = "id";

        private String dir = "asc";

        private Integer page = 1;

        private Integer size = 20;

        private BigDecimal price_from = BigDecimal.valueOf(0);

        private BigDecimal price_to = BigDecimal.valueOf(1000000000);

        private Map<String, List<String>> filter = new HashMap<>();
}

