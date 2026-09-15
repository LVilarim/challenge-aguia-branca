package com.challenge.aguiabranca.api.dashboard.web;

import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.common.pagination.Pageables;
import com.challenge.aguiabranca.api.dashboard.application.DashboardService;
import com.challenge.aguiabranca.api.dashboard.dto.ChartGroup;
import com.challenge.aguiabranca.api.dashboard.dto.ChartMetric;
import com.challenge.aguiabranca.api.dashboard.dto.ChartResponse;
import com.challenge.aguiabranca.api.dashboard.dto.DashboardSummaryResponse;
import com.challenge.aguiabranca.api.dashboard.dto.ProjectDashboardResponse;
import com.challenge.aguiabranca.api.dashboard.dto.StrategyDashboardResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
@PreAuthorize("hasRole('LIDER')")
public class DashboardController {
    private static final Set<String> SORTS = Set.of("createdAt", "name", "status", "progressPercent", "investment", "financialReturn", "plannedEndDate");
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @GetMapping("/summary")
    DashboardSummaryResponse summary(
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return dashboard.summary(strategyId, projectId, from, to);
    }

    @GetMapping("/by-strategy")
    List<StrategyDashboardResponse> byStrategy(
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return dashboard.byStrategy(strategyId, projectId, from, to);
    }

    @GetMapping("/by-project")
    PageResponse<ProjectDashboardResponse> byProject(
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return dashboard.byProject(strategyId, projectId, from, to,
                Pageables.of(page, size, sort, SORTS, "createdAt"));
    }

    @GetMapping("/charts")
    ChartResponse charts(
            @RequestParam ChartMetric metric,
            @RequestParam ChartGroup groupBy,
            @RequestParam(required = false) String strategyId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        return dashboard.charts(metric, groupBy, strategyId, projectId, from, to);
    }
}
