package com.pbl6.services.impl;

import com.pbl6.dtos.response.dashboard.*;
import com.pbl6.enums.OrderStatus;
import com.pbl6.repositories.OrderItemRepository;
import com.pbl6.repositories.OrderRepository;
import com.pbl6.repositories.UserRepository;
import com.pbl6.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public DashboardSummaryDto getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now();
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999

        Long revenue = orderRepository.sumRevenue(startDateTime, endDateTime, OrderStatus.COMPLETED);
        Long orders = orderRepository.countOrders(startDateTime, endDateTime);
        Long productsSold = orderItemRepository.countProductsSold(startDateTime, endDateTime, OrderStatus.COMPLETED);
        Long newCustomers = userRepository.countNewCustomers(startDateTime, endDateTime);

        return DashboardSummaryDto.builder()
                .totalRevenue(revenue)
                .totalOrders(orders)
                .totalProductsSold(productsSold)
                .newCustomers(newCustomers)
                .build();
    }

    @Override
    public List<ChartDataPointDTO> getRevenueChartData(
            LocalDate startDate,
            LocalDate endDate,
            String period // "day" | "month" | "year"
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30); // Mặc định 30 ngày
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        String sqlFormat;
        switch (period.toUpperCase()) {
            case "MONTH":
                sqlFormat = "%Y-%m"; // Ví dụ: "2023-11"
                break;
            case "YEAR":
                sqlFormat = "%Y";    // Ví dụ: "2023"
                break;
            case "DAY":
            default:
                sqlFormat = "%Y-%m-%d"; // Ví dụ: "2023-11-18"
                break;
        }

        List<Object[]> rawData = orderRepository.findRevenueChartData(
                startDateTime,
                endDateTime,
                sqlFormat
        );

        return rawData.stream()
                .map(row -> ChartDataPointDTO.builder()
                        .label(String.valueOf(row[0]))
                        .revenue(((BigDecimal) row[1]))
                        .orderCount(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BreakdownDTO> getOrderStatusBreakdown(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.countOrdersByStatus(startDateTime, endDateTime);

        return rawData.stream()
                .map(row -> BreakdownDTO.builder()
                        .label(String.valueOf(row[0]))     // Cột 0 là Trạng thái (status)
                        .count((Long) row[1])               // Cột 1 là COUNT(o)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        if (limit <= 0) limit = 10;

        List<Object[]> rawData = orderItemRepository.findTopSellingProductsNative(
                startDateTime,
                endDateTime,
                limit
        );

        return rawData.stream()
                .map(row -> TopProductDTO.builder()
                        .productId(((Number) row[0]).longValue())
                        .productName((String) row[1])
                        .totalSold(((Number) row[2]).longValue())
                        .totalRevenue(((Number) row[3]).longValue())
                        .thumbnail((String) row[4])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TopCustomerDTO> getTopSpendingCustomers(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        if (limit <= 0) limit = 10;

        List<Object[]> rawData = orderRepository.findTopSpendingCustomers(
                startDateTime,
                endDateTime,
                limit
        );

        return rawData.stream()
                .map(row -> TopCustomerDTO.builder()
                        .userId(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .email((String) row[2])
                        .totalOrders(((Number) row[3]).longValue())
                        .totalSpent(((Number) row[4]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BreakdownDTO> getOrderPaymentMethodBreakdown(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.countOrdersByPaymentMethod(startDateTime, endDateTime);

        return rawData.stream()
                .map(row -> BreakdownDTO.builder()
                        .label(String.valueOf(row[0]))
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BreakdownDTO> getOrderReceiveBreakdown(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.countOrdersByReceiveMethod(startDateTime, endDateTime);

        return rawData.stream()
                .map(row -> BreakdownDTO.builder()
                        .label(String.valueOf(row[0]))
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BreakdownDTO> getOrderTypeBreakdown(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawData = orderRepository.countOrdersByChannel(startDateTime, endDateTime);

        return rawData.stream()
                .map(row -> BreakdownDTO.builder()
                        .label(String.valueOf(row[0]))
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }
}
