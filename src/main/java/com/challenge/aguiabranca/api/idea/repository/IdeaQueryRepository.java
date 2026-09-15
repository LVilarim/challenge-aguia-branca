package com.challenge.aguiabranca.api.idea.repository;

import com.challenge.aguiabranca.api.idea.domain.IdeaDocument;
import com.challenge.aguiabranca.api.idea.domain.IdeaPriority;
import com.challenge.aguiabranca.api.idea.domain.IdeaStatus;
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
public class IdeaQueryRepository {
    private final MongoTemplate mongo;

    public IdeaQueryRepository(MongoTemplate mongo) { this.mongo = mongo; }

    public Page<IdeaDocument> find(IdeaStatus status, IdeaPriority priority, String strategyId,
                                   String authorUserId, Instant from, Instant to, String text, Pageable pageable) {
        var criteria = new ArrayList<Criteria>();
        if (status == null) criteria.add(Criteria.where("status").ne(IdeaStatus.RASCUNHO));
        else criteria.add(Criteria.where("status").is(status));
        if (priority != null) criteria.add(Criteria.where("priority").is(priority));
        if (strategyId != null && !strategyId.isBlank()) criteria.add(Criteria.where("strategyId").is(strategyId));
        if (authorUserId != null && !authorUserId.isBlank()) criteria.add(Criteria.where("authorUserId").is(authorUserId));
        if (from != null) criteria.add(Criteria.where("createdAt").gte(from));
        if (to != null) criteria.add(Criteria.where("createdAt").lte(to));
        if (text != null && !text.isBlank()) {
            String regex = Pattern.quote(text.trim());
            criteria.add(new Criteria().orOperator(Criteria.where("title").regex(regex, "i"),
                    Criteria.where("description").regex(regex, "i"), Criteria.where("problem").regex(regex, "i")));
        }
        Query query = new Query(new Criteria().andOperator(criteria));
        long total = mongo.count(query, IdeaDocument.class);
        query.with(pageable);
        return new PageImpl<>(mongo.find(query, IdeaDocument.class), pageable, total);
    }
}

