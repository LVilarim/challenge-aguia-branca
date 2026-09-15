package com.challenge.aguiabranca.api.strategy.repository;

import com.challenge.aguiabranca.api.strategy.domain.StrategyEventType;
import com.challenge.aguiabranca.api.strategy.domain.StrategyHistoryDocument;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StrategyHistoryRepository extends MongoRepository<StrategyHistoryDocument, String> {
    Page<StrategyHistoryDocument> findByStrategyId(String strategyId, Pageable pageable);
    Page<StrategyHistoryDocument> findByStrategyIdAndEventTypeAndOccurredAtBetween(
            String strategyId, StrategyEventType eventType, Instant from, Instant to, Pageable pageable);
}

