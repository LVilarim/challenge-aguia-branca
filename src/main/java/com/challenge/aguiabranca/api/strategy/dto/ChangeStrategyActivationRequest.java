package com.challenge.aguiabranca.api.strategy.dto;

import com.challenge.aguiabranca.api.strategy.domain.ActivationAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record ChangeStrategyActivationRequest(
        @NotNull ActivationAction action,
        @NotNull Instant effectiveAt,
        @NotNull @PositiveOrZero Long version) {}

