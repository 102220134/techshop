package com.pbl6.dtos.response;

import com.pbl6.dtos.response.product.MediaDto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private String content;
    private CustomerDto customer;
    private Long productId;
    private LocalDateTime createdAt;
    private int rating;
    private List<MediaDto> medias;
    private boolean isPurchased;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerDto {
        private Long id;
        private String name;
    }
}

