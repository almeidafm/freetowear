package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpisResponse {

    private KpiMetricResponse grossRevenue;
    private KpiMetricResponse netRevenue;
    private KpiMetricResponse ordersCount;
    private KpiMetricResponse averageOrderValue;
    private KpiMetricResponse newCustomers;
    private KpiMetricResponse repeatPurchaseRate;
    private KpiMetricResponse cancellationRate;
    private KpiMetricResponse averageDeliveryTimeDays;
    private KpiMetricResponse averageRating;
}
