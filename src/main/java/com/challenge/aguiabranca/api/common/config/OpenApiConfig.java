package com.challenge.aguiabranca.api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI apiOpenApi() {
        var bearer = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Challenge Águia Branca API")
                        .version("v1")
                        .description("API da Sprint 2 para estratégias, ideias, projetos e indicadores.")
                        .contact(new Contact().name("Challenge Águia Branca")))
                .components(new Components().addSecuritySchemes(bearer,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(bearer));
    }
}

