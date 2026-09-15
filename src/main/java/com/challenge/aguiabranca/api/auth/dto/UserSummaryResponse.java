package com.challenge.aguiabranca.api.auth.dto;

import com.challenge.aguiabranca.api.user.domain.Role;
import com.challenge.aguiabranca.api.user.domain.UserDocument;

public record UserSummaryResponse(String id, String name, String email, Role role) {
    public static UserSummaryResponse from(UserDocument user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

