package com.challenge.aguiabranca.api.idea.dto;

import com.challenge.aguiabranca.api.idea.domain.IdeaPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record PrioritizeIdeaRequest(
        @NotNull IdeaPriority priority,
        @Size(max = 2000) String comment,
        @NotNull @PositiveOrZero Long version) {}

