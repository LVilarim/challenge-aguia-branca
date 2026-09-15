package com.challenge.aguiabranca.api.security.principal;

import com.challenge.aguiabranca.api.common.error.UnauthorizedException;
import com.challenge.aguiabranca.api.user.domain.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
    public String id() {
        Authentication authentication = authentication();
        return authentication.getName();
    }

    public Role role() {
        String authority = authentication().getAuthorities().stream()
                .map(item -> item.getAuthority())
                .filter(item -> item.startsWith("ROLE_"))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Autenticação inválida."));
        return Role.valueOf(authority.substring(5));
    }

    private Authentication authentication() {
        Authentication value = SecurityContextHolder.getContext().getAuthentication();
        if (value == null || !value.isAuthenticated()) throw new UnauthorizedException("Autenticação necessária.");
        return value;
    }
}

