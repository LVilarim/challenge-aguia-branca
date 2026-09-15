package com.challenge.aguiabranca.api.dashboard.application;

import com.challenge.aguiabranca.api.common.pagination.PageResponse;
import com.challenge.aguiabranca.api.dashboard.dto.ChartGroup;
import com.challenge.aguiabranca.api.dashboard.dto.ChartMetric;
import com.challenge.aguiabranca.api.dashboard.dto.ChartResponse;
import com.challenge.aguiabranca.api.dashboard.dto.ChartSeriesItem;
import com.challenge.aguiabranca.api.dashboard.dto.DashboardSummaryResponse;
import com.challenge.aguiabranca.api.dashboard.dto.ProjectDashboardResponse;
import com.challenge.aguiabranca.api.dashboard.dto.StrategyDashboardResponse;
import com.challenge.aguiabranca.api.dashboard.repository.DashboardRepository;
import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import com.challenge.aguiabranca.api.project.domain.ProjectMetrics;
import com.challenge.aguiabranca.api.project.domain.ProjectMetricsCalculator;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import com.challenge.aguiabranca.api.strategy.domain.StrategyDocument;
import com.challenge.aguiabranca.api.strategy.repository.StrategyRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final DashboardRepository dashboard;
    private final StrategyRepository strategies;
    private final ProjectMetricsCalculator metrics;
    private final Clock clock;

    public DashboardService(DashboardRepository dashboard, StrategyRepository strategies,
                            ProjectMetricsCalculator metrics, Clock clock) {
        this.dashboard = dashboard;
        this.strategies = strategies;
        this.metrics = metrics;
        this.clock = clock;
    }

    public DashboardSummaryResponse summary(String strategyId, String projectId, Instant from, Instant to) {
        var values = dashboard.aggregate(strategyId, projectId, from, to, LocalDate.now(clock));
        ProjectMetrics financial = metrics.calculate(values.totalInvestment(), values.totalFinancialReturn());
        return new DashboardSummaryResponse(values.projectCount(), values.completedProjectCount(),
                values.activeProjectCount(), values.delayedProjectCount(), values.totalInvestment(),
                values.totalFinancialReturn(), financial.profit(), financial.roiPercent(),
                values.averageProgressPercent(), values.averageProductivityGainPercent(), values.averageDurationDays(),
                clock.instant(), filters(strategyId, projectId, from, to));
    }

    public List<StrategyDashboardResponse> byStrategy(String strategyId, String projectId, Instant from, Instant to) {
        List<ProjectDocument> projects = dashboard.find(strategyId, projectId, from, to);
        Map<String, StrategyDocument> strategyById = strategies.findAllById(
                        projects.stream().map(ProjectDocument::getStrategyId).distinct().toList()).stream()
                .collect(Collectors.toMap(StrategyDocument::getId, Function.identity()));
        Map<String, List<ProjectDocument>> grouped = projects.stream()
                .collect(Collectors.groupingBy(ProjectDocument::getStrategyId, LinkedHashMap::new, Collectors.toList()));
        return grouped.entrySet().stream().map(entry -> {
            Aggregates values = aggregate(entry.getValue());
            StrategyDocument strategy = strategyById.get(entry.getKey());
            return new StrategyDashboardResponse(entry.getKey(), strategy == null ? null : strategy.getCategory(),
                    strategy == null ? null : strategy.getCampaign(), entry.getValue().size(), values.investment,
                    values.financialReturn, values.profit, values.roi, values.averageProgress,
                    values.averageProductivity, values.averageDuration);
        }).toList();
    }

    public PageResponse<ProjectDashboardResponse> byProject(String strategyId, String projectId, Instant from,
                                                              Instant to, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        return PageResponse.from(dashboard.find(strategyId, projectId, from, to, pageable)
                .map(project -> ProjectDashboardResponse.from(project,
                        metrics.calculate(project.getInvestment(), project.getFinancialReturn()), today)));
    }

    public ChartResponse charts(ChartMetric metric, ChartGroup groupBy, String strategyId, String projectId,
                                Instant from, Instant to) {
        List<ProjectDocument> projects = dashboard.find(strategyId, projectId, from, to);
        List<ChartSeriesItem> series;
        if (groupBy == ChartGroup.STRATEGY) {
            series = byStrategy(strategyId, projectId, from, to).stream()
                    .map(item -> new ChartSeriesItem(item.strategyId(),
                            item.campaign() == null ? item.strategyId() : item.campaign(), strategyValue(item, metric)))
                    .toList();
        } else {
            series = projects.stream().map(project -> new ChartSeriesItem(project.getId(), project.getName(),
                    projectValue(project, metric))).toList();
        }
        return new ChartResponse(metric, groupBy, clock.instant(), series);
    }

    private BigDecimal strategyValue(StrategyDashboardResponse item, ChartMetric metric) {
        return switch (metric) {
            case ROI_PERCENT -> item.roiPercent();
            case PROFIT -> item.profit();
            case INVESTMENT -> item.totalInvestment();
            case FINANCIAL_RETURN -> item.totalFinancialReturn();
            case PRODUCTIVITY_GAIN_PERCENT -> item.averageProductivityGainPercent();
            case PROGRESS_PERCENT -> item.averageProgressPercent();
        };
    }

    private BigDecimal projectValue(ProjectDocument project, ChartMetric metric) {
        ProjectMetrics calculated = metrics.calculate(project.getInvestment(), project.getFinancialReturn());
        return switch (metric) {
            case ROI_PERCENT -> calculated.roiPercent();
            case PROFIT -> calculated.profit();
            case INVESTMENT -> money(project.getInvestment());
            case FINANCIAL_RETURN -> money(project.getFinancialReturn());
            case PRODUCTIVITY_GAIN_PERCENT -> project.getProductivityGainPercent();
            case PROGRESS_PERCENT -> BigDecimal.valueOf(project.getProgressPercent()).setScale(2);
        };
    }

    private Aggregates aggregate(List<ProjectDocument> projects) {
        BigDecimal investment = projects.stream().map(ProjectDocument::getInvestment).map(this::zeroIfNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal financialReturn = projects.stream().map(ProjectDocument::getFinancialReturn).map(this::zeroIfNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        ProjectMetrics financial = metrics.calculate(investment, financialReturn);
        long completed = projects.stream().filter(project -> project.getStatus() == ProjectStatus.CONCLUIDO).count();
        long active = projects.stream().filter(project -> project.getStatus() == ProjectStatus.EM_ANDAMENTO
                || project.getStatus() == ProjectStatus.PAUSADO).count();
        LocalDate today = LocalDate.now(clock);
        long delayed = projects.stream().filter(project -> project.getPlannedEndDate() != null
                && project.getPlannedEndDate().isBefore(today)
                && project.getStatus() != ProjectStatus.CONCLUIDO && project.getStatus() != ProjectStatus.CANCELADO).count();
        BigDecimal avgProgress = average(projects.stream()
                .map(project -> BigDecimal.valueOf(project.getProgressPercent())).toList());
        BigDecimal avgProductivity = average(projects.stream().map(ProjectDocument::getProductivityGainPercent)
                .filter(value -> value != null).toList());
        List<BigDecimal> durations = projects.stream()
                .filter(project -> project.getActualStartDate() != null && project.getActualEndDate() != null)
                .map(project -> BigDecimal.valueOf(ChronoUnit.DAYS.between(project.getActualStartDate(), project.getActualEndDate())))
                .toList();
        return new Aggregates(investment, financialReturn, financial.profit(), financial.roiPercent(),
                avgProgress, avgProductivity, average(durations), completed, active, delayed);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return null;
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal zeroIfNull(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private BigDecimal money(BigDecimal value) { return zeroIfNull(value).setScale(2, RoundingMode.HALF_UP); }

    private Map<String, Object> filters(String strategyId, String projectId, Instant from, Instant to) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (strategyId != null) result.put("strategyId", strategyId);
        if (projectId != null) result.put("projectId", projectId);
        if (from != null) result.put("from", from);
        if (to != null) result.put("to", to);
        return result;
    }

    private record Aggregates(
            BigDecimal investment, BigDecimal financialReturn, BigDecimal profit, BigDecimal roi,
            BigDecimal averageProgress, BigDecimal averageProductivity, BigDecimal averageDuration,
            long completed, long active, long delayed) {}
}
