package com.challenge.aguiabranca.api.project.application;

import com.challenge.aguiabranca.api.common.error.BusinessRuleException;
import com.challenge.aguiabranca.api.common.error.ResourceConflictException;
import com.challenge.aguiabranca.api.common.error.ResourceNotFoundException;
import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.idea.application.IdeaService;
import com.challenge.aguiabranca.api.idea.domain.IdeaStatus;
import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectMetricsCalculator;
import com.challenge.aguiabranca.api.project.domain.ProjectStage;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import com.challenge.aguiabranca.api.project.dto.CreateProjectRequest;
import com.challenge.aguiabranca.api.project.dto.ProjectResponse;
import com.challenge.aguiabranca.api.project.dto.RegisterProjectResultRequest;
import com.challenge.aguiabranca.api.project.dto.UpdateProgressRequest;
import com.challenge.aguiabranca.api.project.dto.UpdateProjectRequest;
import com.challenge.aguiabranca.api.project.repository.ProjectQueryRepository;
import com.challenge.aguiabranca.api.project.repository.ProjectRepository;
import com.challenge.aguiabranca.api.strategy.application.StrategyService;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final ProjectQueryRepository queries;
    private final StrategyService strategies;
    private final IdeaService ideas;
    private final ProjectMetricsCalculator metrics;
    private final Clock clock;

    public ProjectService(ProjectRepository projects, ProjectQueryRepository queries, StrategyService strategies,
                          IdeaService ideas, ProjectMetricsCalculator metrics, Clock clock) {
        this.projects = projects;
        this.queries = queries;
        this.strategies = strategies;
        this.ideas = ideas;
        this.metrics = metrics;
        this.clock = clock;
    }

    public ProjectResponse create(CreateProjectRequest request, String managerId) {
        var current = strategies.currentDocument();
        if (!current.getId().equals(request.strategyId())) {
            throw new BusinessRuleException("STRATEGY_NOT_CURRENT", "O projeto deve estar vinculado à estratégia vigente.");
        }
        if (request.sourceIdeaId() != null && !request.sourceIdeaId().isBlank()) {
            var idea = ideas.find(request.sourceIdeaId());
            if (idea.getStatus() != IdeaStatus.APROVADA) {
                throw new BusinessRuleException("IDEA_NOT_APPROVED", "Somente ideia aprovada pode originar um projeto.");
            }
            if (!idea.getStrategyId().equals(request.strategyId())) {
                throw new BusinessRuleException("STRATEGY_MISMATCH", "Ideia e projeto devem pertencer à mesma estratégia.");
            }
            if (projects.existsBySourceIdeaId(request.sourceIdeaId())) {
                throw new ResourceConflictException("IDEA_ALREADY_HAS_PROJECT", "A ideia já originou um projeto.");
            }
        }
        ProjectDocument project = new ProjectDocument(request.name(), request.description(), request.strategyId(),
                request.sourceIdeaId(), managerId, request.stage(), request.plannedStartDate(),
                request.plannedEndDate(), request.investment());
        return response(projects.save(project));
    }

    public ProjectDocument find(String id) {
        return projects.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projeto"));
    }

    public ProjectResponse get(String id) { return response(find(id)); }

    public PageResponse<ProjectResponse> list(String strategyId, String sourceIdeaId, String managerUserId,
                                               ProjectStage stage, ProjectStatus status, LocalDate from, LocalDate to,
                                               Boolean delayed, Pageable pageable) {
        var page = queries.find(strategyId, sourceIdeaId, managerUserId, stage, status, from, to, delayed,
                LocalDate.now(clock), pageable).map(this::response);
        return PageResponse.from(page);
    }

    public ProjectResponse update(String id, UpdateProjectRequest request) {
        ProjectDocument project = find(id);
        requireVersion(project, request.version());
        project.changePlan(request.name(), request.description(), request.stage(), request.plannedStartDate(),
                request.plannedEndDate(), request.investment());
        return response(projects.save(project));
    }

    public ProjectResponse progress(String id, UpdateProgressRequest request, String actor) {
        ProjectDocument project = find(id);
        requireVersion(project, request.version());
        project.updateProgress(request.stage(), request.status(), request.progressPercent(), request.note(), actor,
                clock.instant(), LocalDate.now(clock));
        return response(projects.save(project));
    }

    public ProjectResponse results(String id, RegisterProjectResultRequest request, String actor) {
        ProjectDocument project = find(id);
        requireVersion(project, request.version());
        project.registerResults(request.actualEndDate(), request.financialReturn(), request.productivityGainPercent(),
                request.resultsSummary(), actor, clock.instant());
        return response(projects.save(project));
    }

    public void archive(String id, Long version, String actor) {
        ProjectDocument project = find(id);
        requireVersion(project, version);
        project.archive(actor, clock.instant());
        projects.save(project);
    }

    private ProjectResponse response(ProjectDocument project) {
        return ProjectResponse.from(project, metrics.calculate(project.getInvestment(), project.getFinancialReturn()));
    }

    private static void requireVersion(ProjectDocument project, Long supplied) {
        if (supplied == null || !supplied.equals(project.getVersion())) {
            throw new ResourceConflictException("STALE_VERSION", "A versão informada está desatualizada.");
        }
    }
}

