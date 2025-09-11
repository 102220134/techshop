package com.pbl6.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductDetailDto {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String slug;
    private String thumbnail;
    private BigDecimal price;
    private String currency;
    private String manufacturer;
    private Boolean isParent;
    private List<CategoryDto> categories;
    private List<String> images;
    private List<Option> variants;
    private List<Option> othersInProductGroup;


    // ---------- inner DTOs ----------

    @Getter
    @Setter
    public static class OptionItem {
        private Long productId;
        private String value;
        private BigDecimal price;
        private String currency;
        private String thumbnail;
        private boolean isSelected;
    }

    @Getter
    @Setter
    public static class Option {
        private String code;
        private String label;
        private String scope;
        private List<OptionItem> items;
    }
}

