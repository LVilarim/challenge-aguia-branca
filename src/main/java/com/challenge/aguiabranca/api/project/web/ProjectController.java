package com.challenge.aguiabranca.api.project.web;

import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.common.pagination.Pageables;
import com.challenge.aguiabranca.api.project.application.ProjectService;
import com.challenge.aguiabranca.api.project.domain.ProjectStage;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import com.challenge.aguiabranca.api.project.dto.CreateProjectRequest;
import com.challenge.aguiabranca.api.project.dto.ProjectResponse;
import com.challenge.aguiabranca.api.project.dto.RegisterProjectResultRequest;
import com.challenge.aguiabranca.api.project.dto.UpdateProgressRequest;
import com.challenge.aguiabranca.api.project.dto.UpdateProjectRequest;
import com.challenge.aguiabranca.api.security.principal.CurrentUserProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.net.URI;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects")
@Validated
public class ProjectController {
    private static final Set<String> SORTS = Set.of("createdAt", "updatedAt", "name", "status", "stage", "plannedEndDate", "progressPercent", "investment");
    private final ProjectService service;
    private final CurrentUserProvider currentUser;

    public ProjectController(ProjectService service, CurrentUserProvider currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse created = service.create(request, currentUser.id());
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','LIDER')")
    PageResponse<ProjectResponse> list(
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String sourceIdeaId,
            @RequestParam(required = false) String managerUserId,
            @RequestParam(required = false) ProjectStage stage,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Boolean delayed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return service.list(strategyId, sourceIdeaId, managerUserId, stage, status, from, to, delayed,
                Pageables.of(page, size, sort, SORTS, "createdAt"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','LIDER')")
    ProjectResponse get(@PathVariable String id) { return service.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    ProjectResponse update(@PathVariable String id, @Valid @RequestBody UpdateProjectRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasRole('GESTOR')")
    ProjectResponse progress(@PathVariable String id, @Valid @RequestBody UpdateProgressRequest request) {
        return service.progress(id, request, currentUser.id());
    }

    @PostMapping("/{id}/results")
    @PreAuthorize("hasRole('GESTOR')")
    ProjectResponse results(@PathVariable String id, @Valid @RequestBody RegisterProjectResultRequest request) {
        return service.results(id, request, currentUser.id());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    ResponseEntity<Void> archive(@PathVariable String id, @RequestParam @PositiveOrZero Long version) {
        service.archive(id, version, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
