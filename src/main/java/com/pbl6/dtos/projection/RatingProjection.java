package com.pbl6.dtos.projection;

public interface RatingProjection {
    Long getProductId();
    Long getTotal();
    Long getStar1();
    Long getStar2();
    Long getStar3();
    Long getStar4();
    Long getStar5();
    Double getAverage();
}