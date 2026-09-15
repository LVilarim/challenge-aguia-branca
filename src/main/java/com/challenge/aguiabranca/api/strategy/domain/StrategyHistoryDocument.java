package com.challenge.aguiabranca.api.strategy.domain;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("strategy_history")
@CompoundIndex(name = "strategy_version_unique", def = "{'strategyId': 1, 'strategyVersion': 1}", unique = true)
public class StrategyHistoryDocument {
    @Id private String id;
    private String strategyId;
    private long strategyVersion;
    @Indexed private StrategyEventType eventType;
    @Indexed private Instant occurredAt;
    private String actorUserId;
    private StrategySnapshot snapshot;

    public StrategyHistoryDocument() {}

    public StrategyHistoryDocument(StrategyDocument strategy, StrategyEventType eventType, Instant occurredAt, String actorUserId) {
        this.strategyId = strategy.getId();
        this.strategyVersion = strategy.getVersion();
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.actorUserId = actorUserId;
        this.snapshot = StrategySnapshot.from(strategy, occurredAt);
    }

    public String getId() { return id; }
    public String getStrategyId() { return strategyId; }
    public long getStrategyVersion() { return strategyVersion; }
    public StrategyEventType getEventType() { return eventType; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getActorUserId() { return actorUserId; }
    public StrategySnapshot getSnapshot() { return snapshot; }
}

