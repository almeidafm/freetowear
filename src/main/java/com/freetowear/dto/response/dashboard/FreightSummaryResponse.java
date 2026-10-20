package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FreightSummaryResponse {

    private BigDecimal averageFreight;
    private Double freightToRevenuePercentage;
    private Double freeShippingPercentage;
}
