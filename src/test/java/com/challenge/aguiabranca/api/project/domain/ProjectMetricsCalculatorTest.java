package com.challenge.aguiabranca.api.project.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProjectMetricsCalculatorTest {
    private final ProjectMetricsCalculator calculator = new ProjectMetricsCalculator();

    @Test
    void calculatesProfitAndRoiWithDecimalPrecision() {
        ProjectMetrics result = calculator.calculate(new BigDecimal("1000.00"), new BigDecimal("1250.00"));
        assertThat(result.profit()).isEqualByComparingTo("250.00");
        assertThat(result.roiPercent()).isEqualByComparingTo("25.00");
    }

    @Test
    void returnsNullRoiForZeroInvestment() {
        ProjectMetrics result = calculator.calculate(BigDecimal.ZERO, new BigDecimal("100.00"));
        assertThat(result.profit()).isEqualByComparingTo("100.00");
        assertThat(result.roiPercent()).isNull();
    }

    @Test
    void roundsMoneyAtTheApiBoundary() {
        ProjectMetrics result = calculator.calculate(new BigDecimal("10.005"), new BigDecimal("20.004"));
        assertThat(result.profit()).isEqualByComparingTo("9.99");
        assertThat(result.roiPercent()).isEqualByComparingTo("99.80");
    }
}
