package com.pbl6.dtos.response.promotion;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromotionDto {
    private Long id;
    private String name;
    private String description;

    private String discountType;      // "PERCENTAGE" hoặc "AMOUNT"
    private BigDecimal discountValue;
    private BigDecimal maxDiscountValue;

    private Integer priority;
    private Boolean exclusive;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;

    // nếu bạn có nhiều target (PRODUCT, CATEGORY, GLOBAL...)
    private List<PromotionTargetDto> targets;

//    // giá sau giảm (tính toán runtime)
//    private BigDecimal specialPrice;

}
