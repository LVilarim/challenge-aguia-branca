package com.challenge.aguiabranca.api.dashboard.dto;

import java.time.Instant;
import java.util.List;

public record ChartResponse(
        ChartMetric metric, ChartGroup groupBy, Instant generatedAt, List<ChartSeriesItem> series) {}

