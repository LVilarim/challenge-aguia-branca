package com.challenge.aguiabranca.api.strategy.application;

import com.challenge.aguiabranca.api.common.error.ResourceConflictException;
import com.challenge.aguiabranca.api.common.error.ResourceNotFoundException;
import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.strategy.domain.ActivationAction;
import com.challenge.aguiabranca.api.strategy.domain.StrategyDocument;
import com.challenge.aguiabranca.api.strategy.domain.StrategyEventType;
import com.challenge.aguiabranca.api.strategy.domain.StrategyStatus;
import com.challenge.aguiabranca.api.strategy.dto.ChangeStrategyActivationRequest;
import com.challenge.aguiabranca.api.strategy.dto.CreateStrategyRequest;
import com.challenge.aguiabranca.api.strategy.dto.StrategyResponse;
import com.challenge.aguiabranca.api.strategy.dto.UpdateStrategyRequest;
import com.challenge.aguiabranca.api.strategy.repository.StrategyQueryRepository;
import com.challenge.aguiabranca.api.strategy.repository.StrategyRepository;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StrategyService {
    private final StrategyRepository strategies;
    private final StrategyQueryRepository queries;
    private final StrategyHistoryService history;

    public StrategyService(StrategyRepository strategies, StrategyQueryRepository queries, StrategyHistoryService history) {
        this.strategies = strategies;
        this.queries = queries;
        this.history = history;
    }

    @Transactional
    public StrategyResponse create(CreateStrategyRequest request, String actor) {
        StrategyDocument strategy = strategies.save(new StrategyDocument(request.category(), request.campaign(),
                request.description(), request.validFrom(), request.validUntil(), actor));
        history.record(strategy, StrategyEventType.CREATED, actor);
        return StrategyResponse.from(strategy);
    }

    public StrategyResponse get(String id) { return StrategyResponse.from(find(id)); }

    public StrategyDocument find(String id) {
        return strategies.findById(id).orElseThrow(() -> new ResourceNotFoundException("Estratégia"));
    }

    public StrategyDocument currentDocument() {
        return strategies.findFirstByStatus(StrategyStatus.ATIVA)
                .orElseThrow(() -> new ResourceNotFoundException("Estratégia vigente"));
    }

    public StrategyResponse current() { return StrategyResponse.from(currentDocument()); }

    public PageResponse<StrategyResponse> list(StrategyStatus status, String category, String campaign,
                                                Instant validFrom, Instant validUntil, Pageable pageable) {
        return PageResponse.from(queries.find(status, category, campaign, validFrom, validUntil, pageable)
                .map(StrategyResponse::from));
    }

    @Transactional
    public StrategyResponse update(String id, UpdateStrategyRequest request, String actor) {
        StrategyDocument strategy = find(id);
        requireVersion(strategy, request.version());
        strategy.changeDetails(request.category(), request.campaign(), request.description(),
                request.validFrom(), request.validUntil(), actor);
        strategy = strategies.save(strategy);
        history.record(strategy, StrategyEventType.UPDATED, actor);
        return StrategyResponse.from(strategy);
    }

    @Transactional
    public synchronized StrategyResponse changeActivation(String id, ChangeStrategyActivationRequest request, String actor) {
        StrategyDocument target = find(id);
        requireVersion(target, request.version());
        if (request.action() == ActivationAction.CLOSE) {
            target.close(request.effectiveAt(), actor);
            target = strategies.save(target);
            history.record(target, StrategyEventType.CLOSED, actor);
            return StrategyResponse.from(target);
        }

        strategies.findFirstByStatus(StrategyStatus.ATIVA).filter(active -> !active.getId().equals(id)).ifPresent(active -> {
            active.close(request.effectiveAt(), actor);
            StrategyDocument closed = strategies.save(active);
            history.record(closed, StrategyEventType.CLOSED, actor);
        });
        target.activate(request.effectiveAt(), actor);
        try {
            target = strategies.save(target);
        } catch (DuplicateKeyException ex) {
            throw new ResourceConflictException("ACTIVE_STRATEGY_EXISTS", "Outra estratégia já está ativa.");
        }
        history.record(target, StrategyEventType.ACTIVATED, actor);
        return StrategyResponse.from(target);
    }

    @Transactional
    public void archive(String id, String actor) {
        StrategyDocument strategy = find(id);
        if (strategy.getStatus() == StrategyStatus.ARQUIVADA) return;
        strategy.archive(actor);
        strategy = strategies.save(strategy);
        history.record(strategy, StrategyEventType.ARCHIVED, actor);
    }

    private static void requireVersion(StrategyDocument strategy, Long supplied) {
        if (supplied == null || !supplied.equals(strategy.getVersion())) {
            throw new ResourceConflictException("STALE_VERSION", "A versão informada está desatualizada.");
        }
    }
}
