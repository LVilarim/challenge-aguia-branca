package com.challenge.aguiabranca.api.dashboard.repository;

import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardRepository {
    private final MongoTemplate mongo;

    public DashboardRepository(MongoTemplate mongo) { this.mongo = mongo; }

    public List<ProjectDocument> find(String strategyId, String projectId, Instant from, Instant to) {
        return mongo.find(query(strategyId, projectId, from, to), ProjectDocument.class);
    }

    public Page<ProjectDocument> find(String strategyId, String projectId, Instant from, Instant to, Pageable pageable) {
        Query query = query(strategyId, projectId, from, to);
        long total = mongo.count(query, ProjectDocument.class);
        query.with(pageable);
        return new PageImpl<>(mongo.find(query, ProjectDocument.class), pageable, total);
    }

    private Query query(String strategyId, String projectId, Instant from, Instant to) {
        var criteria = new ArrayList<Criteria>();
        criteria.add(Criteria.where("active").is(true));
        criteria.add(Criteria.where("status").ne(ProjectStatus.CANCELADO));
        if (strategyId != null && !strategyId.isBlank()) criteria.add(Criteria.where("strategyId").is(strategyId));
        if (projectId != null && !projectId.isBlank()) criteria.add(Criteria.where("_id").is(projectId));
        if (from != null) criteria.add(Criteria.where("createdAt").gte(from));
        if (to != null) criteria.add(Criteria.where("createdAt").lte(to));
        return new Query(new Criteria().andOperator(criteria));
    }
}
