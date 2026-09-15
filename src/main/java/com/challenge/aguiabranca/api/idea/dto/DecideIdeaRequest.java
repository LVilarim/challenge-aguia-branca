package com.challenge.aguiabranca.api.idea.dto;

import com.challenge.aguiabranca.api.idea.domain.IdeaPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record DecideIdeaRequest(
        IdeaPriority priority,
        @NotBlank @Size(min = 10, max = 2000) String comment,
        @NotNull @PositiveOrZero Long version) {}

