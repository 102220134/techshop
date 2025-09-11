package com.pbl6.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductRequest {
    private String slugPath;
    private Boolean includeInactive;
    private String orderBy;
    private String dir;
    private Integer page = 0;
    private Integer size = 20;
}
