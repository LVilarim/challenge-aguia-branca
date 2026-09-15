package com.challenge.aguiabranca.api.idea.web;

import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.common.pagination.Pageables;
import com.challenge.aguiabranca.api.idea.application.IdeaService;
import com.challenge.aguiabranca.api.idea.domain.IdeaPriority;
import com.challenge.aguiabranca.api.idea.domain.IdeaStatus;
import com.challenge.aguiabranca.api.idea.dto.CreateIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.DecideIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.IdeaResponse;
import com.challenge.aguiabranca.api.idea.dto.PrioritizeIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.UpdateIdeaRequest;
import com.challenge.aguiabranca.api.idea.dto.VersionRequest;
import com.challenge.aguiabranca.api.security.principal.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.net.URI;
import java.time.Instant;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/api/v1/ideas")
@Tag(name = "Ideas")
@Validated
public class IdeaController {
    private static final Set<String> SORTS = Set.of("createdAt", "updatedAt", "title", "status", "priority", "submittedAt", "decidedAt");
    private final IdeaService service;
    private final CurrentUserProvider currentUser;

    public IdeaController(IdeaService service, CurrentUserProvider currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    ResponseEntity<IdeaResponse> create(@Valid @RequestBody CreateIdeaRequest request) {
        IdeaResponse created = service.create(request, currentUser.id());
        return ResponseEntity.created(URI.create("/api/v1/ideas/" + created.id())).body(created);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('OPERADOR')")
    PageResponse<IdeaResponse> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return service.mine(currentUser.id(), Pageables.of(page, size, sort, SORTS, "createdAt"));
    }

    @GetMapping
    @PreAuthorize("hasRole('GESTOR')")
    PageResponse<IdeaResponse> list(
            @RequestParam(required = false) IdeaStatus status,
            @RequestParam(required = false) IdeaPriority priority,
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String authorUserId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return service.list(status, priority, strategyId, authorUserId, from, to, text,
                Pageables.of(page, size, sort, SORTS, "createdAt"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR','GESTOR')")
    IdeaResponse get(@PathVariable String id) {
        return service.get(id, currentUser.id(), currentUser.role());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    IdeaResponse update(@PathVariable String id, @Valid @RequestBody UpdateIdeaRequest request) {
        return service.update(id, request, currentUser.id());
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('OPERADOR')")
    IdeaResponse submit(@PathVariable String id, @Valid @RequestBody VersionRequest request) {
        return service.submit(id, request, currentUser.id());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    ResponseEntity<Void> archive(@PathVariable String id, @RequestParam @PositiveOrZero Long version) {
        service.archive(id, version, currentUser.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/evaluation")
    @PreAuthorize("hasRole('GESTOR')")
    IdeaResponse evaluation(@PathVariable String id, @Valid @RequestBody VersionRequest request) {
        return service.startEvaluation(id, request);
    }

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('GESTOR')")
    IdeaResponse priority(@PathVariable String id, @Valid @RequestBody PrioritizeIdeaRequest request) {
        return service.prioritize(id, request);
    }

    @PostMapping("/{id}/approval")
    @PreAuthorize("hasRole('GESTOR')")
    IdeaResponse approval(@PathVariable String id, @Valid @RequestBody DecideIdeaRequest request) {
        return service.approve(id, request, currentUser.id());
    }

    @PostMapping("/{id}/rejection")
    @PreAuthorize("hasRole('GESTOR')")
    IdeaResponse rejection(@PathVariable String id, @Valid @RequestBody DecideIdeaRequest request) {
        return service.reject(id, request, currentUser.id());
    }
}
