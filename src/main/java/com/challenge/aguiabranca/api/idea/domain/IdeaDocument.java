package com.challenge.aguiabranca.api.idea.domain;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ideas")
@CompoundIndexes({
        @CompoundIndex(name = "author_created_idx", def = "{'authorUserId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "status_priority_idx", def = "{'status': 1, 'priority': 1}")
})
public class IdeaDocument {
    @Id private String id;
    private String title;
    private String description;
    private String problem;
    private String expectedBenefit;
    @Indexed private String strategyId;
    @Indexed private String authorUserId;
    private IdeaStatus status = IdeaStatus.RASCUNHO;
    private IdeaPriority priority;
    private String managerComment;
    private Instant submittedAt;
    private Instant decidedAt;
    private String decidedBy;
    @Version private Long version;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    public IdeaDocument() {}

    public IdeaDocument(String title, String description, String problem, String expectedBenefit,
                        String strategyId, String authorUserId) {
        this.title = clean(title);
        this.description = clean(description);
        this.problem = clean(problem);
        this.expectedBenefit = cleanNullable(expectedBenefit);
        this.strategyId = strategyId;
        this.authorUserId = authorUserId;
        this.status = IdeaStatus.RASCUNHO;
    }

    public void update(String title, String description, String problem, String expectedBenefit) {
        requireStatus(IdeaStatus.RASCUNHO, IdeaStatus.SUBMETIDA);
        this.title = clean(title);
        this.description = clean(description);
        this.problem = clean(problem);
        this.expectedBenefit = cleanNullable(expectedBenefit);
    }

    public void submit(Instant now) {
        requireStatus(IdeaStatus.RASCUNHO);
        status = IdeaStatus.SUBMETIDA;
        submittedAt = now;
    }

    public void archive() {
        requireStatus(IdeaStatus.RASCUNHO, IdeaStatus.SUBMETIDA);
        status = IdeaStatus.ARQUIVADA;
    }

    public void startEvaluation() {
        requireStatus(IdeaStatus.SUBMETIDA);
        status = IdeaStatus.EM_AVALIACAO;
    }

    public void prioritize(IdeaPriority priority, String comment) {
        requireStatus(IdeaStatus.EM_AVALIACAO);
        this.priority = priority;
        if (comment != null && !comment.isBlank()) this.managerComment = comment.trim();
    }

    public void approve(IdeaPriority priority, String comment, String managerId, Instant now) {
        requireStatus(IdeaStatus.EM_AVALIACAO);
        IdeaPriority finalPriority = priority == null ? this.priority : priority;
        if (finalPriority == null) {
            throw new BusinessRuleException("PRIORITY_REQUIRED", "A prioridade é obrigatória antes da decisão.");
        }
        this.priority = finalPriority;
        decide(IdeaStatus.APROVADA, comment, managerId, now);
    }

    public void reject(String comment, String managerId, Instant now) {
        requireStatus(IdeaStatus.EM_AVALIACAO);
        if (priority == null) {
            throw new BusinessRuleException("PRIORITY_REQUIRED", "A prioridade é obrigatória antes da decisão.");
        }
        decide(IdeaStatus.REJEITADA, comment, managerId, now);
    }

    private void decide(IdeaStatus decision, String comment, String managerId, Instant now) {
        managerComment = clean(comment);
        decidedBy = managerId;
        decidedAt = now;
        status = decision;
    }

    private void requireStatus(IdeaStatus... allowed) {
        for (IdeaStatus candidate : allowed) if (status == candidate) return;
        throw new BusinessRuleException("INVALID_STATE_TRANSITION", "Transição inválida para ideia no estado " + status + ".");
    }

    private static String clean(String value) { return value.trim(); }
    private static String cleanNullable(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getProblem() { return problem; }
    public String getExpectedBenefit() { return expectedBenefit; }
    public String getStrategyId() { return strategyId; }
    public String getAuthorUserId() { return authorUserId; }
    public IdeaStatus getStatus() { return status; }
    public IdeaPriority getPriority() { return priority; }
    public String getManagerComment() { return managerComment; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public String getDecidedBy() { return decidedBy; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

