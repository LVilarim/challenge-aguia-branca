package com.challenge.aguiabranca.api.project.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterProjectResultRequest(
        @NotNull LocalDate actualEndDate,
        @NotNull @PositiveOrZero BigDecimal financialReturn,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal productivityGainPercent,
        @NotBlank @Size(min = 10, max = 5000) String resultsSummary,
        @NotNull @PositiveOrZero Long version) {}

