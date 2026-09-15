package com.challenge.aguiabranca.api.auth.web;

import com.challenge.aguiabranca.api.auth.application.AuthenticationService;
import com.challenge.aguiabranca.api.auth.dto.LoginRequest;
import com.challenge.aguiabranca.api.auth.dto.LoginResponse;
import com.challenge.aguiabranca.api.auth.dto.UserSummaryResponse;
import com.challenge.aguiabranca.api.security.principal.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
public class AuthController {
    private final AuthenticationService authentication;
    private final CurrentUserProvider currentUser;

    public AuthController(AuthenticationService authentication, CurrentUserProvider currentUser) {
        this.authentication = authentication;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica um usuário ativo e emite um access token")
    LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authentication.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário autenticado")
    UserSummaryResponse me() {
        return authentication.me(currentUser.id());
    }
}
