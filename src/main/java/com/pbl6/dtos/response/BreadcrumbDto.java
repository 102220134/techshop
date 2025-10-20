package com.pbl6.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "BreadcrumbDto")
public class BreadcrumbDto {

    List<BreadcrumbItem> items;
    BreadcrumbItem current;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    static public class BreadcrumbItem{
        private String name;
        private String slug;
    }
}
