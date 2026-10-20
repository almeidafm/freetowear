package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendResponse {

    private List<String> labels;
    private List<BigDecimal> data;
}
