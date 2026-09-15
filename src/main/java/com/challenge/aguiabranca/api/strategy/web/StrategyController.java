package com.challenge.aguiabranca.api.strategy.web;

import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.common.pagination.Pageables;
import com.challenge.aguiabranca.api.security.principal.CurrentUserProvider;
import com.challenge.aguiabranca.api.strategy.application.StrategyService;
import com.challenge.aguiabranca.api.strategy.domain.StrategyStatus;
import com.challenge.aguiabranca.api.strategy.dto.ChangeStrategyActivationRequest;
import com.challenge.aguiabranca.api.strategy.dto.CreateStrategyRequest;
import com.challenge.aguiabranca.api.strategy.dto.StrategyResponse;
import com.challenge.aguiabranca.api.strategy.dto.UpdateStrategyRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/strategies")
@Tag(name = "Strategies")
public class StrategyController {
    private static final Set<String> SORTS = Set.of("createdAt", "updatedAt", "category", "campaign", "validFrom", "status");
    private final StrategyService service;
    private final CurrentUserProvider currentUser;

    public StrategyController(StrategyService service, CurrentUserProvider currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('LIDER')")
    ResponseEntity<StrategyResponse> create(@Valid @RequestBody CreateStrategyRequest request) {
        StrategyResponse created = service.create(request, currentUser.id());
        return ResponseEntity.created(URI.create("/api/v1/strategies/" + created.id())).body(created);
    }

    @GetMapping("/current")
    StrategyResponse current() { return service.current(); }

    @GetMapping
    PageResponse<StrategyResponse> list(
            @RequestParam(required = false) StrategyStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String campaign,
            @RequestParam(required = false) Instant validFrom,
            @RequestParam(required = false) Instant validUntil,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return service.list(status, category, campaign, validFrom, validUntil,
                Pageables.of(page, size, sort, SORTS, "createdAt"));
    }

    @GetMapping("/{id}")
    StrategyResponse get(@PathVariable String id) { return service.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    StrategyResponse update(@PathVariable String id, @Valid @RequestBody UpdateStrategyRequest request) {
        return service.update(id, request, currentUser.id());
    }

    @PatchMapping("/{id}/activation")
    @PreAuthorize("hasRole('LIDER')")
    StrategyResponse activation(@PathVariable String id, @Valid @RequestBody ChangeStrategyActivationRequest request) {
        return service.changeActivation(id, request, currentUser.id());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIDER')")
    ResponseEntity<Void> archive(@PathVariable String id) {
        service.archive(id, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}

