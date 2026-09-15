package com.challenge.aguiabranca.api.idea.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VersionRequest(@NotNull @PositiveOrZero Long version) {}

