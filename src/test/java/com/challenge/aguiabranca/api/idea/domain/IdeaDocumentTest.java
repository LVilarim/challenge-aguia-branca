package com.challenge.aguiabranca.api.idea.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class IdeaDocumentTest {
    private static final Instant NOW = Instant.parse("2026-09-15T12:00:00Z");

    @Test
    void followsTheCompleteApprovalFlow() {
        IdeaDocument idea = idea();
        Instant now = Instant.parse("2026-09-15T12:00:00Z");
        idea.submit(now);
        idea.startEvaluation();
        idea.prioritize(IdeaPriority.ALTA, "Alinhada à estratégia");
        idea.approve(null, "Benefício e viabilidade confirmados", "manager", now.plusSeconds(60));
        assertThat(idea.getStatus()).isEqualTo(IdeaStatus.APROVADA);
        assertThat(idea.getPriority()).isEqualTo(IdeaPriority.ALTA);
        assertThat(idea.getDecidedBy()).isEqualTo("manager");
    }

    @Test
    void cannotSkipEvaluation() {
        assertThatThrownBy(() -> idea().approve(IdeaPriority.ALTA, "Comentário válido para decisão", "manager", NOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Transição inválida");
    }

    @Test
    void decisionRequiresPriority() {
        IdeaDocument idea = idea();
        idea.submit(NOW);
        idea.startEvaluation();
        assertThatThrownBy(() -> idea.reject("Comentário válido para rejeição", "manager", NOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("prioridade");
    }

    private IdeaDocument idea() {
        return new IdeaDocument("Uma ideia válida", "Descrição detalhada o bastante para a ideia",
                "Problema operacional relevante", "Redução de custo", "strategy", "operator");
    }
}
