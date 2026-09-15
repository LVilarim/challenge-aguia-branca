package com.challenge.aguiabranca.api.project.domain;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document("projects")
@CompoundIndexes({
        @CompoundIndex(name = "strategy_status_idx", def = "{'strategyId': 1, 'status': 1}"),
        @CompoundIndex(name = "manager_created_idx", def = "{'managerUserId': 1, 'createdAt': -1}")
})
public class ProjectDocument {
    @Id private String id;
    private String name;
    private String description;
    @Indexed private String strategyId;
    @Indexed(unique = true, sparse = true) private String sourceIdeaId;
    @Indexed private String managerUserId;
    private ProjectStage stage;
    private ProjectStatus status = ProjectStatus.PLANEJADO;
    private LocalDate plannedStartDate;
    @Indexed private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    @Field(targetType = FieldType.DECIMAL128) private BigDecimal investment;
    @Field(targetType = FieldType.DECIMAL128) private BigDecimal financialReturn;
    @Field(targetType = FieldType.DECIMAL128) private BigDecimal productivityGainPercent;
    private int progressPercent;
    private String resultsSummary;
    private boolean active = true;
    private List<ProgressEntry> progressHistory = new ArrayList<>();
    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    public ProjectDocument() {}

    public ProjectDocument(String name, String description, String strategyId, String sourceIdeaId,
                           String managerUserId, ProjectStage stage, LocalDate plannedStartDate,
                           LocalDate plannedEndDate, BigDecimal investment) {
        this.name = clean(name);
        this.description = clean(description);
        this.strategyId = strategyId;
        this.sourceIdeaId = blankToNull(sourceIdeaId);
        this.managerUserId = managerUserId;
        this.stage = stage;
        changePlan(name, description, stage, plannedStartDate, plannedEndDate, investment);
        this.status = ProjectStatus.PLANEJADO;
        this.progressPercent = 0;
    }

    public void changePlan(String name, String description, ProjectStage stage, LocalDate plannedStartDate,
                           LocalDate plannedEndDate, BigDecimal investment) {
        if (status == ProjectStatus.CONCLUIDO || status == ProjectStatus.CANCELADO) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Projeto finalizado não pode ter o plano alterado.");
        }
        validateDates(plannedStartDate, plannedEndDate);
        requireNonNegative(investment, "O investimento não pode ser negativo.");
        this.name = clean(name);
        this.description = clean(description);
        this.stage = stage;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.investment = money(investment);
    }

    public void updateProgress(ProjectStage stage, ProjectStatus nextStatus, int progressPercent,
                               String note, String actor, Instant now, LocalDate today) {
        if (nextStatus == ProjectStatus.CONCLUIDO) {
            throw new BusinessRuleException("RESULTS_REQUIRED", "Conclua o projeto registrando os resultados.");
        }
        validateTransition(nextStatus);
        if (progressPercent < 0 || progressPercent > 100) {
            throw new BusinessRuleException("INVALID_PROGRESS", "O progresso deve estar entre 0 e 100.");
        }
        if (status == ProjectStatus.PLANEJADO && nextStatus == ProjectStatus.EM_ANDAMENTO && actualStartDate == null) {
            actualStartDate = today;
        }
        this.stage = stage;
        this.status = nextStatus;
        this.progressPercent = progressPercent;
        this.progressHistory.add(new ProgressEntry(now, actor, stage, nextStatus, progressPercent, blankToNull(note)));
        if (nextStatus == ProjectStatus.CANCELADO) active = false;
    }

    public void registerResults(LocalDate actualEndDate, BigDecimal financialReturn,
                                BigDecimal productivityGainPercent, String resultsSummary,
                                String actor, Instant now) {
        if (status != ProjectStatus.EM_ANDAMENTO && status != ProjectStatus.CONCLUIDO) {
            throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Somente projeto em andamento ou concluído aceita resultados.");
        }
        if (actualStartDate != null && actualEndDate.isBefore(actualStartDate)) {
            throw new BusinessRuleException("INVALID_DATE_RANGE", "A data real de fim não pode anteceder o início.");
        }
        requireNonNegative(financialReturn, "O retorno financeiro não pode ser negativo.");
        if (productivityGainPercent != null && (productivityGainPercent.signum() < 0
                || productivityGainPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BusinessRuleException("INVALID_PERCENTAGE", "O ganho de produtividade deve estar entre 0 e 100.");
        }
        this.actualEndDate = actualEndDate;
        this.financialReturn = money(financialReturn);
        this.productivityGainPercent = productivityGainPercent == null ? null
                : productivityGainPercent.setScale(2, RoundingMode.HALF_UP);
        this.resultsSummary = clean(resultsSummary);
        this.status = ProjectStatus.CONCLUIDO;
        this.stage = ProjectStage.ENCERRAMENTO;
        this.progressPercent = 100;
        this.progressHistory.add(new ProgressEntry(now, actor, stage, status, 100, "Resultados registrados"));
    }

    public void archive(String actor, Instant now) {
        if (!active && status == ProjectStatus.CANCELADO) return;
        if (status == ProjectStatus.CONCLUIDO) {
            active = false;
            progressHistory.add(new ProgressEntry(now, actor, stage, status, progressPercent, "Projeto arquivado"));
            return;
        }
        status = ProjectStatus.CANCELADO;
        active = false;
        progressHistory.add(new ProgressEntry(now, actor, stage, status, progressPercent, "Projeto cancelado"));
    }

    private void validateTransition(ProjectStatus next) {
        if (next == status) return;
        boolean allowed = switch (status) {
            case PLANEJADO -> next == ProjectStatus.EM_ANDAMENTO || next == ProjectStatus.CANCELADO;
            case EM_ANDAMENTO -> next == ProjectStatus.PAUSADO || next == ProjectStatus.CANCELADO;
            case PAUSADO -> next == ProjectStatus.EM_ANDAMENTO || next == ProjectStatus.CANCELADO;
            case CONCLUIDO, CANCELADO -> false;
        };
        if (!allowed) throw new BusinessRuleException("INVALID_STATE_TRANSITION",
                "Transição inválida de " + status + " para " + next + ".");
    }

    private static void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessRuleException("INVALID_DATE_RANGE", "A data final planejada não pode anteceder a inicial.");
        }
    }

    private static void requireNonNegative(BigDecimal value, String message) {
        if (value == null || value.signum() < 0) throw new BusinessRuleException("NEGATIVE_VALUE", message);
    }

    private static BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private static String clean(String value) { return value.trim(); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStrategyId() { return strategyId; }
    public String getSourceIdeaId() { return sourceIdeaId; }
    public String getManagerUserId() { return managerUserId; }
    public ProjectStage getStage() { return stage; }
    public ProjectStatus getStatus() { return status; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public LocalDate getActualStartDate() { return actualStartDate; }
    public LocalDate getActualEndDate() { return actualEndDate; }
    public BigDecimal getInvestment() { return investment; }
    public BigDecimal getFinancialReturn() { return financialReturn; }
    public BigDecimal getProductivityGainPercent() { return productivityGainPercent; }
    public int getProgressPercent() { return progressPercent; }
    public String getResultsSummary() { return resultsSummary; }
    public boolean isActive() { return active; }
    public List<ProgressEntry> getProgressHistory() { return List.copyOf(progressHistory); }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
