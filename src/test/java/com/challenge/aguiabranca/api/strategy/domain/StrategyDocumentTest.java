package com.challenge.aguiabranca.api.strategy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StrategyDocumentTest {
    @Test
    void followsActivationLifecycle() {
        StrategyDocument strategy = strategy();
        Instant start = Instant.parse("2026-09-15T12:00:00Z");
        strategy.activate(start, "leader");
        assertThat(strategy.getStatus()).isEqualTo(StrategyStatus.ATIVA);
        strategy.close(start.plusSeconds(3600), "leader");
        assertThat(strategy.getStatus()).isEqualTo(StrategyStatus.ENCERRADA);
        assertThat(strategy.getValidUntil()).isEqualTo(start.plusSeconds(3600));
    }

    @Test
    void refusesInvalidDateRange() {
        assertThatThrownBy(() -> new StrategyDocument("Categoria", "Campanha", "Descrição suficientemente longa",
                Instant.parse("2026-09-16T00:00:00Z"), Instant.parse("2026-09-15T00:00:00Z"), "leader"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("data final");
    }

    @Test
    void activeStrategyMustBeClosedBeforeArchive() {
        StrategyDocument strategy = strategy();
        strategy.activate(Instant.parse("2026-09-15T12:00:00Z"), "leader");
        assertThatThrownBy(() -> strategy.archive("leader")).isInstanceOf(BusinessRuleException.class);
    }

    private StrategyDocument strategy() {
        return new StrategyDocument("Eficiência", "Operação 2030", "Uma orientação estratégica detalhada.",
                null, null, "leader");
    }
}
