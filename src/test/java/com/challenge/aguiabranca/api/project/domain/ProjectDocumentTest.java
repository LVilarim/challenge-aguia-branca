package com.challenge.aguiabranca.api.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ProjectDocumentTest {
    @Test
    void startsAndCompletesWithAuditableResults() {
        ProjectDocument project = project();
        LocalDate today = LocalDate.of(2026, 9, 15);
        project.updateProgress(ProjectStage.EXECUCAO, ProjectStatus.EM_ANDAMENTO, 30,
                "Execução iniciada", "manager", Instant.parse("2026-09-15T12:00:00Z"), today);
        project.registerResults(today.plusDays(10), new BigDecimal("1300"), new BigDecimal("12.5"),
                "Resultados confirmados pela operação", "manager", Instant.parse("2026-09-25T12:00:00Z"));
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.CONCLUIDO);
        assertThat(project.getProgressPercent()).isEqualTo(100);
        assertThat(project.getProgressHistory()).hasSize(2);
    }

    @Test
    void completionThroughProgressIsRejected() {
        ProjectDocument project = project();
        assertThatThrownBy(() -> project.updateProgress(ProjectStage.ENCERRAMENTO, ProjectStatus.CONCLUIDO,
                100, null, "manager", Instant.parse("2026-09-15T12:00:00Z"), LocalDate.of(2026, 9, 15)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("resultados");
    }

    @Test
    void invalidPlannedDatesAreRejected() {
        assertThatThrownBy(() -> new ProjectDocument("Projeto", "Descrição válida do projeto", "strategy", null,
                "manager", ProjectStage.PLANEJAMENTO, LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1),
                BigDecimal.TEN)).isInstanceOf(BusinessRuleException.class);
    }

    private ProjectDocument project() {
        return new ProjectDocument("Projeto de eficiência", "Descrição completa do projeto de eficiência", "strategy",
                null, "manager", ProjectStage.PLANEJAMENTO, LocalDate.of(2026, 9, 15),
                LocalDate.of(2026, 10, 15), new BigDecimal("1000.00"));
    }
}
