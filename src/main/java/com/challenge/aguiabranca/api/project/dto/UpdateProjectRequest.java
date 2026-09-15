package com.challenge.aguiabranca.api.project.dto;

import com.challenge.aguiabranca.api.project.domain.ProjectStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProjectRequest(
        @NotBlank @Size(min = 3, max = 150) String name,
        @NotBlank @Size(min = 10, max = 5000) String description,
        @NotNull ProjectStage stage,
        @NotNull LocalDate plannedStartDate,
        @NotNull LocalDate plannedEndDate,
        @NotNull @PositiveOrZero BigDecimal investment,
        @NotNull @PositiveOrZero Long version) {}

