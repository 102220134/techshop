package com.pbl6.dtos.request.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request gửi đánh giá sản phẩm")
public class CreateReviewRequest {

    @NotNull
    @Min(1)
    @Max(5)
    @Schema(description = "Số sao đánh giá (1-5)", example = "4")
    private Short rating;

    @NotBlank
    @Schema(description = "Nội dung đánh giá", example = "Điện thoại nét.")
    private String content;

    @Schema(description = "Danh sách ảnh (validate chỉ cho up ảnh)")
    private List<MultipartFile> medias;
}
