package com.pbl6.dtos.projection;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

public interface ProductProjection {
    Long getId();
    String getName();
    String getDescription();
    String getSlug();
    String getDetail();
    String getThumbnail();
    BigDecimal getPrice();
    Integer getStock();
    Long getSold();

    // Rating summary
    Long getTotal();
    Long getStar1();
    Long getStar2();
    Long getStar3();
    Long getStar4();
    Long getStar5();
    Double getAverage();
}

