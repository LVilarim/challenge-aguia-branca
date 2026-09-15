package com.challenge.aguiabranca.api.auth.application;

import com.challenge.aguiabranca.api.auth.dto.LoginRequest;
import com.challenge.aguiabranca.api.auth.dto.LoginResponse;
import com.challenge.aguiabranca.api.auth.dto.UserSummaryResponse;
import com.challenge.aguiabranca.api.common.config.AppProperties;
import com.challenge.aguiabranca.api.common.error.ForbiddenException;
import com.challenge.aguiabranca.api.common.error.ResourceNotFoundException;
import com.challenge.aguiabranca.api.common.error.UnauthorizedException;
import com.challenge.aguiabranca.api.security.jwt.JwtTokenService;
import com.challenge.aguiabranca.api.user.domain.UserDocument;
import com.challenge.aguiabranca.api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtTokenService tokens;
    private final AppProperties properties;
    private final String dummyHash;

    public AuthenticationService(UserRepository users, PasswordEncoder passwords, JwtTokenService tokens, AppProperties properties) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
        this.properties = properties;
        this.dummyHash = passwords.encode("constant-time-dummy-password");
    }

    public LoginResponse login(LoginRequest request) {
        UserDocument user = users.findByEmail(UserDocument.normalizeEmail(request.email())).orElse(null);
        boolean matches = passwords.matches(request.password(), user == null ? dummyHash : user.getPasswordHash());
        if (user == null || !matches) throw new UnauthorizedException("Credenciais inválidas.");
        if (!user.isActive()) throw new ForbiddenException("Conta inativa.");
        return new LoginResponse(tokens.issue(user), "Bearer", properties.jwt().expirationSeconds(), UserSummaryResponse.from(user));
    }

    public UserSummaryResponse me(String userId) {
        return users.findById(userId).map(UserSummaryResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário"));
    }
}

