package com.challenge.aguiabranca.api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI apiOpenApi() {
        var bearer = "bearerAuth";
        Schema<?> problem = new ObjectSchema()
                .addProperty("type", new Schema<>().type("string").format("uri"))
                .addProperty("title", new Schema<>().type("string"))
                .addProperty("status", new Schema<>().type("integer"))
                .addProperty("detail", new Schema<>().type("string"))
                .addProperty("instance", new Schema<>().type("string").format("uri"))
                .addProperty("code", new Schema<>().type("string"))
                .addProperty("traceId", new Schema<>().type("string"));
        return new OpenAPI()
                .info(new Info()
                        .title("Challenge Águia Branca API")
                        .version("v1")
                        .description("API da Sprint 2 para estratégias, ideias, projetos e indicadores.")
                        .contact(new Contact().name("Challenge Águia Branca")))
                .components(new Components()
                        .addSecuritySchemes(bearer,
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))
                        .addSchemas("ProblemDetail", problem))
                .addSecurityItem(new SecurityRequirement().addList(bearer));
    }

    @Bean
    OperationCustomizer standardProblemResponses() {
        return (operation, handlerMethod) -> {
            addProblem(operation, "400", "Requisição inválida");
            addProblem(operation, "401", "Autenticação ausente ou inválida");
            addProblem(operation, "403", "Acesso negado");
            addProblem(operation, "404", "Recurso não encontrado");
            addProblem(operation, "409", "Conflito de estado, versão ou unicidade");
            addProblem(operation, "500", "Falha interna");
            return operation;
        };
    }

    private static void addProblem(io.swagger.v3.oas.models.Operation operation, String status, String description) {
        if (operation.getResponses().containsKey(status)) return;
        var schema = new Schema<>().$ref("#/components/schemas/ProblemDetail");
        var mediaType = new io.swagger.v3.oas.models.media.MediaType().schema(schema);
        operation.getResponses().addApiResponse(status, new ApiResponse().description(description)
                .content(new Content().addMediaType("application/problem+json", mediaType)));
    }
}
