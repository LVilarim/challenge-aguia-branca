package com.challenge.aguiabranca.api.idea.dto;

import com.challenge.aguiabranca.api.idea.domain.IdeaDocument;
import com.challenge.aguiabranca.api.idea.domain.IdeaPriority;
import com.challenge.aguiabranca.api.idea.domain.IdeaStatus;
import java.time.Instant;

public record IdeaResponse(
        String id, String title, String description, String problem, String expectedBenefit,
        String strategyId, String authorUserId, IdeaStatus status, IdeaPriority priority,
        String managerComment, Instant submittedAt, Instant decidedAt, String decidedBy,
        Long version, Instant createdAt, Instant updatedAt) {
    public static IdeaResponse from(IdeaDocument idea) {
        return new IdeaResponse(idea.getId(), idea.getTitle(), idea.getDescription(), idea.getProblem(),
                idea.getExpectedBenefit(), idea.getStrategyId(), idea.getAuthorUserId(), idea.getStatus(),
                idea.getPriority(), idea.getManagerComment(), idea.getSubmittedAt(), idea.getDecidedAt(),
                idea.getDecidedBy(), idea.getVersion(), idea.getCreatedAt(), idea.getUpdatedAt());
    }
}
