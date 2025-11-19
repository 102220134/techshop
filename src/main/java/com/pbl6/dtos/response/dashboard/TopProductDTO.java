package com.pbl6.dtos.response.dashboard;

// dto/response/TopProductDTO.java
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {

    private Long productId;

    private String productName;

    private String thumbnail;

    private Long totalSold;

    private Long totalRevenue;
}