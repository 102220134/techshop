package com.pbl6.dtos.request.inventory;

import lombok.Data;

@Data
public class SearchInventoryRequest {
    private Long locationId;
    private Long categoryId;
    private String keyword;

    private String order = "id";
    private String dir = "asc";
    private int page = 1;
    private int size = 20;
}
