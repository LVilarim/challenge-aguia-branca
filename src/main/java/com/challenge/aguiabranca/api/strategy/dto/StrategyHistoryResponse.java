package com.challenge.aguiabranca.api.strategy.dto;

import com.challenge.aguiabranca.api.strategy.domain.StrategyEventType;
import com.challenge.aguiabranca.api.strategy.domain.StrategyHistoryDocument;
import com.challenge.aguiabranca.api.strategy.domain.StrategySnapshot;
import java.time.Instant;

public record StrategyHistoryResponse(
        String id, String strategyId, long version, StrategyEventType eventType,
        Instant occurredAt, String actorUserId, StrategySnapshot snapshot) {
    public static StrategyHistoryResponse from(StrategyHistoryDocument history) {
        return new StrategyHistoryResponse(history.getId(), history.getStrategyId(), history.getStrategyVersion(),
                history.getEventType(), history.getOccurredAt(), history.getActorUserId(), history.getSnapshot());
    }
}

