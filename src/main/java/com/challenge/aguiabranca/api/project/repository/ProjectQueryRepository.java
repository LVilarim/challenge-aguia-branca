package com.challenge.aguiabranca.api.project.repository;

import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectStage;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectQueryRepository {
    private final MongoTemplate mongo;

    public ProjectQueryRepository(MongoTemplate mongo) { this.mongo = mongo; }

    public Page<ProjectDocument> find(String strategyId, String sourceIdeaId, String managerUserId,
                                      ProjectStage stage, ProjectStatus status, LocalDate from, LocalDate to,
                                      Boolean delayed, LocalDate today, Pageable pageable) {
        var criteria = new ArrayList<Criteria>();
        criteria.add(Criteria.where("active").is(true));
        if (strategyId != null && !strategyId.isBlank()) criteria.add(Criteria.where("strategyId").is(strategyId));
        if (sourceIdeaId != null && !sourceIdeaId.isBlank()) criteria.add(Criteria.where("sourceIdeaId").is(sourceIdeaId));
        if (managerUserId != null && !managerUserId.isBlank()) criteria.add(Criteria.where("managerUserId").is(managerUserId));
        if (stage != null) criteria.add(Criteria.where("stage").is(stage));
        if (status != null) criteria.add(Criteria.where("status").is(status));
        if (from != null) criteria.add(Criteria.where("plannedStartDate").gte(from));
        if (to != null) criteria.add(Criteria.where("plannedEndDate").lte(to));
        if (Boolean.TRUE.equals(delayed)) {
            criteria.add(Criteria.where("plannedEndDate").lt(today));
            criteria.add(Criteria.where("status").nin(ProjectStatus.CONCLUIDO, ProjectStatus.CANCELADO));
        }
        Query query = new Query(new Criteria().andOperator(criteria));
        long total = mongo.count(query, ProjectDocument.class);
        query.with(pageable);
        return new PageImpl<>(mongo.find(query, ProjectDocument.class), pageable, total);
    }
}

