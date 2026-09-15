package com.challenge.aguiabranca.api.user.application;

import com.challenge.aguiabranca.api.common.config.AppProperties;
import com.challenge.aguiabranca.api.user.domain.Role;
import com.challenge.aguiabranca.api.user.domain.UserDocument;
import com.challenge.aguiabranca.api.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevUserInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final AppProperties properties;

    public DevUserInitializer(UserRepository users, PasswordEncoder passwords, AppProperties properties) {
        this.users = users;
        this.passwords = passwords;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Seed seed = properties.seed();
        if (seed == null || !seed.enabled()) return;
        createIfMissing("Operador Demo", seed.operadorEmail(), seed.operadorPassword(), Role.OPERADOR);
        createIfMissing("Gestor Demo", seed.gestorEmail(), seed.gestorPassword(), Role.GESTOR);
        createIfMissing("Líder Demo", seed.liderEmail(), seed.liderPassword(), Role.LIDER);
    }

    private void createIfMissing(String name, String email, String password, Role role) {
        String normalized = UserDocument.normalizeEmail(email);
        if (normalized != null && password != null && !users.existsByEmail(normalized)) {
            users.save(new UserDocument(name, normalized, passwords.encode(password), role));
        }
    }
}

