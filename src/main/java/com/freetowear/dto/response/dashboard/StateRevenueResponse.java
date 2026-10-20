package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StateRevenueResponse {

    private String state;
    private BigDecimal revenue;
    private Long ordersCount;
}
