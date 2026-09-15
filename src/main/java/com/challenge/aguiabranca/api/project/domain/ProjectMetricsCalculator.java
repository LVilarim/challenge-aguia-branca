package com.challenge.aguiabranca.api.project.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class ProjectMetricsCalculator {
    public ProjectMetrics calculate(BigDecimal investment, BigDecimal financialReturn) {
        BigDecimal normalizedInvestment = money(investment == null ? BigDecimal.ZERO : investment);
        BigDecimal normalizedReturn = money(financialReturn == null ? BigDecimal.ZERO : financialReturn);
        BigDecimal profit = normalizedReturn.subtract(normalizedInvestment).setScale(2, RoundingMode.HALF_UP);
        BigDecimal roi = normalizedInvestment.signum() == 0 ? null
                : profit.multiply(BigDecimal.valueOf(100)).divide(normalizedInvestment, 2, RoundingMode.HALF_UP);
        return new ProjectMetrics(profit, roi);
    }

    public BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

