package com.challenge.aguiabranca.api.strategy.domain;

import java.time.Instant;

public record StrategySnapshot(
        String id, Instant occurredAt, String category, String campaign, String description,
        StrategyStatus status, Instant validFrom, Instant validUntil) {
    public static StrategySnapshot from(StrategyDocument strategy, Instant occurredAt) {
        return new StrategySnapshot(strategy.getId(), occurredAt, strategy.getCategory(), strategy.getCampaign(),
                strategy.getDescription(), strategy.getStatus(), strategy.getValidFrom(), strategy.getValidUntil());
    }
}

