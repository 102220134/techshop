package com.pbl6.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;


@Getter
@AllArgsConstructor
@Schema(name = "CategoryDto")
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
    private String categoryType;
    private String logo;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CategoryDto> children;
    public void setChildren(List<CategoryDto> children) {
        this.children = children;
    }
}
