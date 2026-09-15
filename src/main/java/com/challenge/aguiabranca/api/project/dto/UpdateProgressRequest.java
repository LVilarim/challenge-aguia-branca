package com.challenge.aguiabranca.api.project.dto;

import com.challenge.aguiabranca.api.project.domain.ProjectStage;
import com.challenge.aguiabranca.api.project.domain.ProjectStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateProgressRequest(
        @NotNull ProjectStage stage,
        @NotNull ProjectStatus status,
        @Min(0) @Max(100) int progressPercent,
        @Size(max = 2000) String note,
        @NotNull @PositiveOrZero Long version) {}

