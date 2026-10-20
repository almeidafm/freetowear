package com.freetowear.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KpiMetricResponse {

    private BigDecimal currentValue;
    private BigDecimal previousValue;
    private Double changePercentage;
    private Boolean isIncrease;

    public static KpiMetricResponse of(BigDecimal current, BigDecimal previous) {
        BigDecimal curr = current != null ? current : BigDecimal.ZERO;
        BigDecimal prev = previous != null ? previous : BigDecimal.ZERO;

        Double change = null;
        if (prev.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = curr.subtract(prev);
            change = diff.divide(prev, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (curr.compareTo(BigDecimal.ZERO) > 0) {
            change = 100.0;
        } else {
            change = 0.0;
        }

        Boolean increase = curr.compareTo(prev) >= 0;
        return new KpiMetricResponse(curr, prev, change, increase);
    }

    public static KpiMetricResponse of(Double current, Double previous) {
        BigDecimal curr = current != null ? BigDecimal.valueOf(current).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal prev = previous != null ? BigDecimal.valueOf(previous).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return of(curr, prev);
    }

    public static KpiMetricResponse of(Long current, Long previous) {
        BigDecimal curr = current != null ? BigDecimal.valueOf(current) : BigDecimal.ZERO;
        BigDecimal prev = previous != null ? BigDecimal.valueOf(previous) : BigDecimal.ZERO;
        return of(curr, prev);
    }
}
