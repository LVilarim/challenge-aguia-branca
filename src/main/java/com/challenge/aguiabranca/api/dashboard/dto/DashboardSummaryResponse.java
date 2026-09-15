package com.challenge.aguiabranca.api.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record DashboardSummaryResponse(
        long projectCount,
        long completedProjectCount,
        long activeProjectCount,
        long delayedProjectCount,
        BigDecimal totalInvestment,
        BigDecimal totalFinancialReturn,
        BigDecimal totalProfit,
        BigDecimal aggregateRoiPercent,
        BigDecimal averageProgressPercent,
        BigDecimal averageProductivityGainPercent,
        BigDecimal averageDurationDays,
        Instant generatedAt,
        Map<String, Object> appliedFilters) {}

