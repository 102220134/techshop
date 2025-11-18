package com.pbl6.dtos.response.dashboard;

// dto/response/DashboardSummaryResponse.java
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Đối tượng tóm tắt các số liệu chính của Dashboard")
public class DashboardSummaryDto {

    @Schema(description = "Tổng doanh thu đã giao thành công (VNĐ)", example = "15000000")
    private Long totalRevenue;

    @Schema(description = "Tổng số đơn hàng đã đặt trong khoảng thời gian", example = "120")
    private Long totalOrders;

    @Schema(description = "Số lượng sản phẩm bán ra", example = "350")
    private Long totalProductsSold;

    @Schema(description = "Số khách hàng mới đăng ký", example = "15")
    private Long newCustomers;
}
