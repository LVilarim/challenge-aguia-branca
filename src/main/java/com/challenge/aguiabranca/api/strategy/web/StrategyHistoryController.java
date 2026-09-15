package com.challenge.aguiabranca.api.strategy.web;

import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.common.pagination.Pageables;
import com.challenge.aguiabranca.api.strategy.application.StrategyHistoryService;
import com.challenge.aguiabranca.api.strategy.domain.StrategyEventType;
import com.challenge.aguiabranca.api.strategy.dto.StrategyHistoryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Strategy History")
public class StrategyHistoryController {
    private final StrategyHistoryService history;

    public StrategyHistoryController(StrategyHistoryService history) { this.history = history; }

    @GetMapping("/strategies/{id}/history")
    PageResponse<StrategyHistoryResponse> list(
            @PathVariable String id,
            @RequestParam(required = false) StrategyEventType eventType,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort) {
        return history.list(id, eventType, from, to,
                Pageables.of(page, size, sort, Set.of("occurredAt", "eventType", "strategyVersion"), "occurredAt"));
    }

    @GetMapping("/strategy-history/{historyId}")
    StrategyHistoryResponse get(@PathVariable String historyId) { return history.get(historyId); }
}
