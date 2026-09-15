package com.challenge.aguiabranca.api.project.domain;

import java.time.Instant;

public record ProgressEntry(
        Instant occurredAt, String actorUserId, ProjectStage stage, ProjectStatus status,
        int progressPercent, String note) {}

