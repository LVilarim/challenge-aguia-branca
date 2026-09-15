package com.challenge.aguiabranca.api.dashboard.dto;

import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectMetrics;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectDashboardResponse(
        String projectId, String name, String strategyId, ProjectStatus status,
        int progressPercent, LocalDate plannedEndDate, LocalDate actualEndDate,
        BigDecimal investment, BigDecimal financialReturn, BigDecimal profit,
        BigDecimal roiPercent, BigDecimal productivityGainPercent, boolean delayed) {
    public static ProjectDashboardResponse from(ProjectDocument project, ProjectMetrics metrics, LocalDate today) {
        boolean delayed = project.getPlannedEndDate() != null && project.getPlannedEndDate().isBefore(today)
                && project.getStatus() != ProjectStatus.CONCLUIDO && project.getStatus() != ProjectStatus.CANCELADO;
        return new ProjectDashboardResponse(project.getId(), project.getName(), project.getStrategyId(),
                project.getStatus(), project.getProgressPercent(), project.getPlannedEndDate(), project.getActualEndDate(),
                project.getInvestment(), project.getFinancialReturn(), metrics.profit(), metrics.roiPercent(),
                project.getProductivityGainPercent(), delayed);
    }
}

