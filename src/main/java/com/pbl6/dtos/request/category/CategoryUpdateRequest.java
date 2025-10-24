package com.pbl6.dtos.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryUpdateRequest {

    private String name;

    private String slug;

    private Long parentId;

    private String categoryType;

    private Boolean isActive;

    private MultipartFile logo;
}
