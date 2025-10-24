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
public class CategoryCreateRequest {
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String name;
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String slug;
    private Long parentId;
    @NotBlank(message = "REQUIRED_FIELD_MISSING")
    private String categoryType;
    @NotNull(message = "REQUIRED_FIELD_MISSING")
    private Boolean isActive = true;
    private MultipartFile logo;
}
