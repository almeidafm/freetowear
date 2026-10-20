package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRevenueResponse {

    private String categoryName;
    private BigDecimal revenue;
    private Long itemsSold;
}
