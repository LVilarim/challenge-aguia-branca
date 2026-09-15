package com.challenge.aguiabranca.api.idea.application;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import com.challenge.aguiabranca.api.common.error.ResourceConflictException;
import com.challenge.aguiabranca.api.common.error.ResourceNotFoundException;
import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.idea.domain.IdeaDocument;
import com.challenge.aguiabranca.api.idea.domain.IdeaPriority;
import com.challenge.aguiabranca.api.idea.domain.IdeaStatus;
import com.challenge.aguiabranca.api.idea.dto.CreateIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.DecideIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.IdeaResponse;
import com.challenge.aguiabranca.api.idea.dto.PrioritizeIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.UpdateIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.VersionRequest;
import com.challenge.aguiabranca.api.idea.repository.IdeaQueryRepository;
import com.challenge.aguiabranca.api.idea.repository.IdeaRepository;
import com.challenge.aguiabranca.api.strategy.application.StrategyService;
import com.challenge.aguiabranca.api.user.domain.Role;
import java.time.Clock;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class IdeaService {
    private final IdeaRepository ideas;
    private final IdeaQueryRepository queries;
    private final StrategyService strategies;
    private final Clock clock;

    public IdeaService(IdeaRepository ideas, IdeaQueryRepository queries, StrategyService strategies, Clock clock) {
        this.ideas = ideas;
        this.queries = queries;
        this.strategies = strategies;
        this.clock = clock;
    }

    public IdeaResponse create(CreateIdeaRequest request, String authorId) {
        var current = strategies.currentDocument();
        if (!current.getId().equals(request.strategyId())) {
            throw new BusinessRuleException("STRATEGY_NOT_CURRENT", "A ideia deve estar vinculada à estratégia vigente.");
        }
        IdeaDocument idea = ideas.save(new IdeaDocument(request.title(), request.description(), request.problem(),
                request.expectedBenefit(), request.strategyId(), authorId));
        return IdeaResponse.from(idea);
    }

    public IdeaDocument find(String id) {
        return ideas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ideia"));
    }

    public IdeaResponse get(String id, String userId, Role role) {
        IdeaDocument idea = role == Role.OPERADOR
                ? ideas.findByIdAndAuthorUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Ideia"))
                : find(id);
        return IdeaResponse.from(idea);
    }

    public PageResponse<IdeaResponse> mine(String authorId, Pageable pageable) {
        return PageResponse.from(ideas.findByAuthorUserId(authorId, pageable).map(IdeaResponse::from));
    }

    public PageResponse<IdeaResponse> list(IdeaStatus status, IdeaPriority priority, String strategyId,
                                            String authorId, Instant from, Instant to, String text, Pageable pageable) {
        return PageResponse.from(queries.find(status, priority, strategyId, authorId, from, to, text, pageable)
                .map(IdeaResponse::from));
    }

    public IdeaResponse update(String id, UpdateIdeaRequest request, String authorId) {
        IdeaDocument idea = owned(id, authorId);
        requireVersion(idea, request.version());
        idea.update(request.title(), request.description(), request.problem(), request.expectedBenefit());
        return IdeaResponse.from(ideas.save(idea));
    }

    public IdeaResponse submit(String id, VersionRequest request, String authorId) {
        IdeaDocument idea = owned(id, authorId);
        requireVersion(idea, request.version());
        idea.submit(clock.instant());
        return IdeaResponse.from(ideas.save(idea));
    }

    public void archive(String id, Long version, String authorId) {
        IdeaDocument idea = owned(id, authorId);
        requireVersion(idea, version);
        idea.archive();
        ideas.save(idea);
    }

    public IdeaResponse startEvaluation(String id, VersionRequest request) {
        IdeaDocument idea = find(id);
        requireVersion(idea, request.version());
        idea.startEvaluation();
        return IdeaResponse.from(ideas.save(idea));
    }

    public IdeaResponse prioritize(String id, PrioritizeIdeaRequest request) {
        IdeaDocument idea = find(id);
        requireVersion(idea, request.version());
        idea.prioritize(request.priority(), request.comment());
        return IdeaResponse.from(ideas.save(idea));
    }

    public IdeaResponse approve(String id, DecideIdeaRequest request, String managerId) {
        IdeaDocument idea = find(id);
        requireVersion(idea, request.version());
        idea.approve(request.priority(), request.comment(), managerId, clock.instant());
        return IdeaResponse.from(ideas.save(idea));
    }

    public IdeaResponse reject(String id, DecideIdeaRequest request, String managerId) {
        IdeaDocument idea = find(id);
        requireVersion(idea, request.version());
        idea.reject(request.comment(), managerId, clock.instant());
        return IdeaResponse.from(ideas.save(idea));
    }

    private IdeaDocument owned(String id, String authorId) {
        return ideas.findByIdAndAuthorUserId(id, authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Ideia"));
    }

    private static void requireVersion(IdeaDocument idea, Long supplied) {
        if (supplied == null || !supplied.equals(idea.getVersion())) {
            throw new ResourceConflictException("STALE_VERSION", "A versão informada está desatualizada.");
        }
    }
}

