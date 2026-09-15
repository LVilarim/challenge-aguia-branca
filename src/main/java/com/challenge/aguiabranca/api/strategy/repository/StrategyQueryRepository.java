package com.challenge.aguiabranca.api.strategy.repository;

import com.challenge.aguiabranca.api.strategy.domain.StrategyDocument;
import com.challenge.aguiabranca.api.strategy.domain.StrategyStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class StrategyQueryRepository {
    private final MongoTemplate mongo;

    public StrategyQueryRepository(MongoTemplate mongo) { this.mongo = mongo; }

    public Page<StrategyDocument> find(StrategyStatus status, String category, String campaign,
                                       Instant validFrom, Instant validUntil, Pageable pageable) {
        var criteria = new ArrayList<Criteria>();
        if (status != null) criteria.add(Criteria.where("status").is(status));
        if (category != null && !category.isBlank()) criteria.add(Criteria.where("category").regex(Pattern.quote(category.trim()), "i"));
        if (campaign != null && !campaign.isBlank()) criteria.add(Criteria.where("campaign").regex(Pattern.quote(campaign.trim()), "i"));
        if (validFrom != null) criteria.add(Criteria.where("validFrom").gte(validFrom));
        if (validUntil != null) criteria.add(Criteria.where("validUntil").lte(validUntil));
        Query query = new Query();
        if (!criteria.isEmpty()) query.addCriteria(new Criteria().andOperator(criteria));
        long total = mongo.count(query, StrategyDocument.class);
        query.with(pageable);
        return new PageImpl<>(mongo.find(query, StrategyDocument.class), pageable, total);
    }
}
