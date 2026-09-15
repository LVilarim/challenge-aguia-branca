package com.challenge.aguiabranca.api.dashboard.dto;

import java.math.BigDecimal;

public record ChartSeriesItem(String key, String label, BigDecimal value) {}

