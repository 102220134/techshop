package com.pbl6.dtos.projection;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;

public interface ProductProjection {
    Long getId();
    String getName();
    String getDescription();
    String getSlug();
    String getThumbnail();
    BigDecimal getPrice();
    int getStock();
    int getReservedStock();
    int getSold();
    long getTotal();
    Double getAverage();
}

