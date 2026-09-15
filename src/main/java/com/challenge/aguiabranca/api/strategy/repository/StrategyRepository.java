package com.challenge.aguiabranca.api.strategy.repository;

import com.challenge.aguiabranca.api.strategy.domain.StrategyDocument;
import com.challenge.aguiabranca.api.strategy.domain.StrategyStatus;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StrategyRepository extends MongoRepository<StrategyDocument, String> {
    Optional<StrategyDocument> findFirstByStatus(StrategyStatus status);
}

