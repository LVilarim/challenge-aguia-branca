package com.challenge.aguiabranca.api.dashboard.dto;

import java.math.BigDecimal;

public record StrategyDashboardResponse(
        String strategyId, String category, String campaign, long projectCount,
        BigDecimal totalInvestment, BigDecimal totalFinancialReturn, BigDecimal profit,
        BigDecimal roiPercent, BigDecimal averageProgressPercent,
        BigDecimal averageProductivityGainPercent, BigDecimal averageDurationDays) {}

