package com.challenge.aguiabranca.api.strategy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateStrategyRequest(
        @NotBlank @Size(min = 2, max = 80) String category,
        @NotBlank @Size(min = 2, max = 120) String campaign,
        @NotBlank @Size(min = 10, max = 4000) String description,
        Instant validFrom,
        Instant validUntil,
        @NotNull @PositiveOrZero Long version) {}

