package com.pbl6.dtos.response.dashboard;

// dto/response/ChartDataPointDTO.java
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Một điểm dữ liệu trên biểu đồ (thời gian, doanh thu, đơn hàng)")
public class ChartDataPointDTO {

    @Schema(description = "Nhãn thời gian (VD: '2023-11-18' nếu nhóm theo ngày)", example = "2023-11-18")
    private String label;

    @Schema(description = "Doanh thu trong khoảng thời gian này", example = "5000000")
    private BigDecimal revenue;

    @Schema(description = "Số lượng đơn hàng trong khoảng thời gian này", example = "10")
    private Long orderCount;
}
