package com.challenge.aguiabranca.api.common.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppProperties(Jwt jwt, Cors cors, Seed seed) {
    public record Jwt(String issuer, String secret, long expirationSeconds) {
        public Duration expiration() {
            return Duration.ofSeconds(expirationSeconds);
        }
    }

    public record Cors(List<String> allowedOrigins) {}

    public record Seed(
            boolean enabled,
            String operadorEmail,
            String operadorPassword,
            String gestorEmail,
            String gestorPassword,
            String liderEmail,
            String liderPassword) {}
}

