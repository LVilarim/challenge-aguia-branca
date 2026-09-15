package com.challenge.aguiabranca.api.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserDocumentTest {
    @Test
    void normalizesEmailAndStoresOnlyTheHash() {
        var encoder = new BCryptPasswordEncoder(4);
        String hash = encoder.encode("Senha123!");
        UserDocument user = new UserDocument("Pessoa", "  PESSOA@EXEMPLO.COM ", hash, Role.OPERADOR);
        assertThat(user.getEmail()).isEqualTo("pessoa@exemplo.com");
        assertThat(user.getPasswordHash()).isNotEqualTo("Senha123!");
        assertThat(encoder.matches("Senha123!", user.getPasswordHash())).isTrue();
    }
}
