package com.pbl6.services;

import com.pbl6.dtos.response.dashboard.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardSummaryDto getDashboardSummary(LocalDate startDate, LocalDate endDate);
    List<ChartDataPointDTO> getRevenueChartData(
            LocalDate startDate,
            LocalDate endDate,
            String period // "day" | "month" | "year"
    );

    List<BreakdownDTO> getOrderStatusBreakdown(
            LocalDate startDate,
            LocalDate endDate
    );

    List<TopProductDTO> getTopSellingProducts(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    );

    List<TopCustomerDTO> getTopSpendingCustomers(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    );

    List<BreakdownDTO> getOrderPaymentMethodBreakdown(
            LocalDate startDate,
            LocalDate endDate
    );

    List<BreakdownDTO> getOrderReceiveBreakdown(
            LocalDate startDate,
            LocalDate endDate
    );

    List<BreakdownDTO> getOrderTypeBreakdown(
            LocalDate startDate,
            LocalDate endDate
    );
}
