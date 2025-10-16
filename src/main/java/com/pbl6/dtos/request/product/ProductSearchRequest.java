package com.pbl6.dtos.request.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductSearchRequest {
    private String q;

    private Integer page = 1;

    private Integer size = 20;
}
