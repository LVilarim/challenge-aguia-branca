package com.challenge.aguiabranca.api.idea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateIdeaRequest(
        @NotBlank @Size(min = 5, max = 150) String title,
        @NotBlank @Size(min = 20, max = 5000) String description,
        @NotBlank @Size(min = 10, max = 3000) String problem,
        @Size(max = 2000) String expectedBenefit,
        @NotNull @PositiveOrZero Long version) {}

