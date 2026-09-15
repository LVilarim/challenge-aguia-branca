package com.challenge.aguiabranca.api.strategy.application;

import com.challenge.aguiabranca.api.common.error.ResourceNotFoundException;
import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.strategy.domain.StrategyDocument;
import com.challenge.aguiabranca.api.strategy.domain.StrategyEventType;
import com.challenge.aguiabranca.api.strategy.domain.StrategyHistoryDocument;
import com.challenge.aguiabranca.api.strategy.dto.StrategyHistoryResponse;
import com.challenge.aguiabranca.api.strategy.repository.StrategyHistoryRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class StrategyHistoryService {
    private final StrategyHistoryRepository history;
    private final MongoTemplate mongo;
    private final Clock clock;

    public StrategyHistoryService(StrategyHistoryRepository history, MongoTemplate mongo, Clock clock) {
        this.history = history;
        this.mongo = mongo;
        this.clock = clock;
    }

    public void record(StrategyDocument strategy, StrategyEventType event, String actor) {
        history.save(new StrategyHistoryDocument(strategy, event, clock.instant(), actor));
    }

    public PageResponse<StrategyHistoryResponse> list(String strategyId, StrategyEventType eventType,
                                                       Instant from, Instant to, Pageable pageable) {
        var criteria = new ArrayList<Criteria>();
        criteria.add(Criteria.where("strategyId").is(strategyId));
        if (eventType != null) criteria.add(Criteria.where("eventType").is(eventType));
        if (from != null) criteria.add(Criteria.where("occurredAt").gte(from));
        if (to != null) criteria.add(Criteria.where("occurredAt").lte(to));
        Query query = new Query(new Criteria().andOperator(criteria));
        long total = mongo.count(query, StrategyHistoryDocument.class);
        query.with(pageable);
        var page = new PageImpl<>(mongo.find(query, StrategyHistoryDocument.class), pageable, total)
                .map(StrategyHistoryResponse::from);
        return PageResponse.from(page);
    }

    public StrategyHistoryResponse get(String id) {
        return history.findById(id).map(StrategyHistoryResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Histórico de estratégia"));
    }
}

