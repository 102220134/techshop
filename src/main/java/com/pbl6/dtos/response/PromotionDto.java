package com.pbl6.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class PromotionDto {
    private Long id;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private String startDate;
    private String endDate;
    private String status;
    private String scope;
    @JsonIgnore
    private BigDecimal specialPrice;
}
