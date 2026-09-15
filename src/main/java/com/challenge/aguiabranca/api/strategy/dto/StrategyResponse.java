package com.challenge.aguiabranca.api.strategy.dto;

import com.challenge.aguiabranca.api.strategy.domain.StrategyDocument;
import com.challenge.aguiabranca.api.strategy.domain.StrategyStatus;
import java.time.Instant;

public record StrategyResponse(
        String id, String category, String campaign, String description, StrategyStatus status,
        Instant validFrom, Instant validUntil, Long version, String createdBy, String updatedBy,
        Instant createdAt, Instant updatedAt) {
    public static StrategyResponse from(StrategyDocument strategy) {
        return new StrategyResponse(strategy.getId(), strategy.getCategory(), strategy.getCampaign(),
                strategy.getDescription(), strategy.getStatus(), strategy.getValidFrom(), strategy.getValidUntil(),
                strategy.getVersion(), strategy.getCreatedBy(), strategy.getUpdatedBy(),
                strategy.getCreatedAt(), strategy.getUpdatedAt());
    }
}

