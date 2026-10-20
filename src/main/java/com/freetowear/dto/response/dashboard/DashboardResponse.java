package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private int periodDays;
    private DashboardKpisResponse kpis;
    private MonthlyTrendResponse revenue12Months;
    private MonthlyTrendResponse rating12Months;
    private List<CategoryRevenueResponse> categoryRevenue;
    private List<StateRevenueResponse> stateRevenue;
    private FreightSummaryResponse freightSummary;
}
