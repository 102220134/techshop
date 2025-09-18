package com.pbl6.dtos.projection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductProjection {
    Long getId();
    String getName();
    String getDescription();
    String getSlug();
    String getThumbnail();
    ObjectNode getDetail();
    BigDecimal getPrice();
    Integer getStock();
    Integer getReservedStock();
    Integer getSold();
    Long getTotal();
    Double getAverage();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    // Computed properties
    default Integer getAvailableStock() {
        return (getStock() != null ? getStock() : 0) - (getReservedStock() != null ? getReservedStock() : 0);
    }
}

