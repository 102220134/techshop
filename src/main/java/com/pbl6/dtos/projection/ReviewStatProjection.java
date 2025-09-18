package com.pbl6.dtos.projection;

public interface ReviewStatProjection {
    Long getTotal();
    Long star1();
    Long star2();
    Long star3();
    Long star4();
    Long star5();
    Double getAverage();
}
