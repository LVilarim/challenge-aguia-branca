package com.challenge.aguiabranca.api.project.dto;

import com.challenge.aguiabranca.api.project.domain.ProgressEntry;
import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectMetrics;
import com.challenge.aguiabranca.api.project.domain.ProjectStage;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ProjectResponse(
        String id, String name, String description, String strategyId, String sourceIdeaId,
        String managerUserId, ProjectStage stage, ProjectStatus status, int progressPercent,
        LocalDate plannedStartDate, LocalDate plannedEndDate, LocalDate actualStartDate, LocalDate actualEndDate,
        BigDecimal investment, BigDecimal financialReturn, BigDecimal profit, BigDecimal roiPercent,
        BigDecimal productivityGainPercent, String resultsSummary, boolean active,
        List<ProgressEntry> progressHistory, Long version, Instant createdAt, Instant updatedAt) {
    public static ProjectResponse from(ProjectDocument project, ProjectMetrics metrics) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(),
                project.getStrategyId(), project.getSourceIdeaId(), project.getManagerUserId(), project.getStage(),
                project.getStatus(), project.getProgressPercent(), project.getPlannedStartDate(), project.getPlannedEndDate(),
                project.getActualStartDate(), project.getActualEndDate(), project.getInvestment(),
                project.getFinancialReturn(), metrics.profit(), metrics.roiPercent(), project.getProductivityGainPercent(),
                project.getResultsSummary(), project.isActive(), project.getProgressHistory(), project.getVersion(),
                project.getCreatedAt(), project.getUpdatedAt());
    }
}
