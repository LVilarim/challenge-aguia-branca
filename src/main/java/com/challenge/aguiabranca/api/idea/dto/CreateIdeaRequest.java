package com.challenge.aguiabranca.api.idea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIdeaRequest(
        @NotBlank @Size(min = 5, max = 150) String title,
        @NotBlank @Size(min = 20, max = 5000) String description,
        @NotBlank @Size(min = 10, max = 3000) String problem,
        @Size(max = 2000) String expectedBenefit,
        @NotBlank String strategyId) {}

