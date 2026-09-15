package com.challenge.aguiabranca.api.dashboard.repository;

import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.Decimal128;
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

    public AggregationResult aggregate(String strategyId, String projectId, Instant from, Instant to, LocalDate today) {
        Date todayDate = Date.from(today.atStartOfDay().toInstant(ZoneOffset.UTC));
        Document completed = new Document("$cond", List.of(
                new Document("$eq", List.of("$status", ProjectStatus.CONCLUIDO.name())), 1, 0));
        Document active = new Document("$cond", List.of(
                new Document("$in", List.of("$status", List.of(ProjectStatus.EM_ANDAMENTO.name(), ProjectStatus.PAUSADO.name()))), 1, 0));
        Document delayedCondition = new Document("$and", List.of(
                new Document("$ne", java.util.Arrays.asList("$plannedEndDate", null)),
                new Document("$lt", List.of("$plannedEndDate", todayDate)),
                new Document("$not", List.of(new Document("$in", List.of("$status",
                        List.of(ProjectStatus.CONCLUIDO.name(), ProjectStatus.CANCELADO.name())))))));
        Document delayed = new Document("$cond", List.of(delayedCondition, 1, 0));
        Document hasDuration = new Document("$and", List.of(
                new Document("$ne", java.util.Arrays.asList("$actualStartDate", null)),
                new Document("$ne", java.util.Arrays.asList("$actualEndDate", null))));
        Document duration = new Document("$cond", java.util.Arrays.asList(hasDuration,
                new Document("$dateDiff", new Document("startDate", "$actualStartDate")
                        .append("endDate", "$actualEndDate").append("unit", "day")), null));
        Document group = new Document("$group", new Document("_id", null)
                .append("projectCount", new Document("$sum", 1))
                .append("completedProjectCount", new Document("$sum", completed))
                .append("activeProjectCount", new Document("$sum", active))
                .append("delayedProjectCount", new Document("$sum", delayed))
                .append("totalInvestment", new Document("$sum", new Document("$ifNull", List.of("$investment", 0))))
                .append("totalFinancialReturn", new Document("$sum", new Document("$ifNull", List.of("$financialReturn", 0))))
                .append("averageProgressPercent", new Document("$avg", "$progressPercent"))
                .append("averageProductivityGainPercent", new Document("$avg", "$productivityGainPercent"))
                .append("averageDurationDays", new Document("$avg", duration)));
        List<Document> pipeline = List.of(new Document("$match", query(strategyId, projectId, from, to).getQueryObject()), group);
        Document result = mongo.getCollection("projects").aggregate(pipeline).first();
        if (result == null) return AggregationResult.empty();
        return new AggregationResult(longValue(result.get("projectCount")), longValue(result.get("completedProjectCount")),
                longValue(result.get("activeProjectCount")), longValue(result.get("delayedProjectCount")),
                decimal(result.get("totalInvestment")), decimal(result.get("totalFinancialReturn")),
                decimal(result.get("averageProgressPercent")), decimal(result.get("averageProductivityGainPercent")),
                decimal(result.get("averageDurationDays")));
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

    private static BigDecimal decimal(Object value) {
        if (value == null) return null;
        if (value instanceof Decimal128 decimal) return decimal.bigDecimalValue().setScale(2, java.math.RoundingMode.HALF_UP);
        if (value instanceof Number number) return BigDecimal.valueOf(number.doubleValue()).setScale(2, java.math.RoundingMode.HALF_UP);
        return new BigDecimal(value.toString()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static long longValue(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }

    public record AggregationResult(
            long projectCount, long completedProjectCount, long activeProjectCount, long delayedProjectCount,
            BigDecimal totalInvestment, BigDecimal totalFinancialReturn, BigDecimal averageProgressPercent,
            BigDecimal averageProductivityGainPercent, BigDecimal averageDurationDays) {
        static AggregationResult empty() {
            return new AggregationResult(0, 0, 0, 0, BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2),
                    null, null, null);
        }
    }
}
