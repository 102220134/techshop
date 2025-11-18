package com.pbl6.controllers.privates;

import com.pbl6.dtos.response.ApiResponseDto;
import com.pbl6.dtos.response.dashboard.*;
import com.pbl6.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// ... các import khác

@RestController
@RequestMapping("/api/private/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard Statistics", description = "Các API thống kê và báo cáo cho trang quản trị E-commerce") // Mô tả chung cho Controller
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/summary")
    @Operation(summary = "Lấy số liệu tổng quan (Summary Cards)",
            description = "Cung cấp tổng doanh thu, số đơn hàng, số sản phẩm bán ra và khách hàng mới trong khoảng thời gian xác định. Mặc định là hôm nay.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ApiResponseDto<DashboardSummaryDto> getSummary(
            @Parameter(description = "Ngày bắt đầu (Định dạng YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (Định dạng YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return new ApiResponseDto<>(dashboardService.getDashboardSummary(startDate, endDate));
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/revenue-chart")
    @Operation(summary = "Lấy dữ liệu vẽ biểu đồ doanh thu theo thời gian",
            description = "Cung cấp chuỗi dữ liệu doanh thu và số lượng đơn hàng được nhóm theo Ngày, Tháng, hoặc Năm.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ApiResponseDto<List<ChartDataPointDTO>> getRevenueChart(
            @Parameter(description = "Ngày bắt đầu (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Khoảng thời gian nhóm (day, month, year)", example = "day")
            @RequestParam(defaultValue = "day") String period
    ) {
        return new ApiResponseDto<>(dashboardService.getRevenueChartData(startDate, endDate, period));
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/order-status-breakdown")
    @Operation(summary = "Lấy dữ liệu phân tích trạng thái đơn hàng (Biểu đồ Tròn)",
            description = "Tổng hợp số lượng đơn hàng theo từng trạng thái (DELIVERED, PENDING, CANCELED,...) trong khoảng thời gian được chỉ định.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về danh sách các trạng thái và số lượng tương ứng.")
    public ApiResponseDto<List<BreakdownDTO>> getOrderStatusBreakdown(
            @Parameter(description = "Ngày bắt đầu (Định dạng YYYY-MM-DD). Mặc định 30 ngày trước.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (Định dạng YYYY-MM-DD). Mặc định là hôm nay.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<BreakdownDTO> response = dashboardService.getOrderStatusBreakdown(startDate, endDate);
        return new ApiResponseDto<>(response);
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/top-products")
    @Operation(summary = "Lấy danh sách sản phẩm bán chạy nhất 🥇",
            description = "Liệt kê N sản phẩm (theo Product ID) có số lượng bán ra cao nhất trong khoảng thời gian. Số liệu được tổng hợp từ order_items của các đơn hàng đã hoàn thành",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về danh sách sản phẩm bán chạy.")
    public ApiResponseDto<List<TopProductDTO>> getTopProducts(
            @Parameter(description = "Ngày bắt đầu (YYYY-MM-DD). Mặc định 30 ngày trước.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (YYYY-MM-DD). Mặc định là hôm nay.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Giới hạn số lượng sản phẩm cần lấy (Top N)", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TopProductDTO> response = dashboardService.getTopSellingProducts(startDate, endDate, limit);
        return new ApiResponseDto<>(response);
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/top-customers")
    @Operation(summary = "Lấy danh sách khách hàng chi tiêu nhiều nhất 👑",
            description = "Liệt kê N khách hàng có tổng chi tiêu cao nhất trong khoảng thời gian được chỉ định.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về danh sách khách hàng VIP.")
    public ApiResponseDto<List<TopCustomerDTO>> getTopCustomers(
            @Parameter(description = "Ngày bắt đầu (YYYY-MM-DD). Mặc định 30 ngày trước.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (YYYY-MM-DD). Mặc định là hôm nay.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Giới hạn số lượng khách hàng cần lấy (Top N)", example = "10")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<TopCustomerDTO> response = dashboardService.getTopSpendingCustomers(startDate, endDate, limit);
        return new ApiResponseDto<>(response);
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/payment-method-breakdown")
    @Operation(summary = "Lấy dữ liệu phân tích phương thức thanh toán đơn hàng (Biểu đồ Tròn)",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ApiResponseDto<List<BreakdownDTO>> getOrderPaymentMethodBreakdown(
            @Parameter(description = "Ngày bắt đầu (Định dạng YYYY-MM-DD). Mặc định 30 ngày trước.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (Định dạng YYYY-MM-DD). Mặc định là hôm nay.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<BreakdownDTO> response = dashboardService.getOrderPaymentMethodBreakdown(startDate, endDate);
        return new ApiResponseDto<>(response);
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/receive-method-breakdown")
    @Operation(summary = "Lấy dữ liệu phân tích phương thức nhận hàng (Biểu đồ Tròn)",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ApiResponseDto<List<BreakdownDTO>> getOrderReceiveBreakdown(
            @Parameter(description = "Ngày bắt đầu (Định dạng YYYY-MM-DD). Mặc định 30 ngày trước.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (Định dạng YYYY-MM-DD). Mặc định là hôm nay.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<BreakdownDTO> response = dashboardService.getOrderReceiveBreakdown(startDate, endDate);
        return new ApiResponseDto<>(response);
    }

    @PreAuthorize("hasAuthority('DASHBOARD_READ')")
    @GetMapping("/online-offline-breakdown")
    @Operation(summary = "Lấy dữ liệu phân tích kênh đơn hàng (Biểu đồ Tròn)",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    public ApiResponseDto<List<BreakdownDTO>> getOrderTypeBreakdown(
            @Parameter(description = "Ngày bắt đầu (Định dạng YYYY-MM-DD). Mặc định 30 ngày trước.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Ngày kết thúc (Định dạng YYYY-MM-DD). Mặc định là hôm nay.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<BreakdownDTO> response = dashboardService.getOrderTypeBreakdown(startDate, endDate);
        return new ApiResponseDto<>(response);
    }
}
