package com.challenge.aguiabranca.api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.aguiabranca.api.idea.repository.IdeaRepository;
import com.challenge.aguiabranca.api.project.repository.ProjectRepository;
import com.challenge.aguiabranca.api.strategy.repository.StrategyHistoryRepository;
import com.challenge.aguiabranca.api.strategy.repository.StrategyRepository;
import com.challenge.aguiabranca.api.user.domain.Role;
import com.challenge.aguiabranca.api.user.domain.UserDocument;
import com.challenge.aguiabranca.api.user.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(properties = "spring.data.mongodb.auto-index-creation=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class SprintFlowIntegrationTest {
    @Container
    @ServiceConnection
    static final MongoDBContainer MONGODB = new MongoDBContainer("mongo:8.0.14").withReplicaSet();

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired UserRepository users;
    @Autowired StrategyRepository strategies;
    @Autowired StrategyHistoryRepository history;
    @Autowired IdeaRepository ideas;
    @Autowired ProjectRepository projects;
    @Autowired PasswordEncoder passwords;

    @BeforeEach
    void cleanAndSeedUsers() {
        projects.deleteAll();
        ideas.deleteAll();
        history.deleteAll();
        strategies.deleteAll();
        users.deleteAll();
        users.save(new UserDocument("Operador", "operador@test.local", passwords.encode("Operador123!"), Role.OPERADOR));
        users.save(new UserDocument("Gestor", "gestor@test.local", passwords.encode("Gestor123!"), Role.GESTOR));
        users.save(new UserDocument("Líder", "lider@test.local", passwords.encode("Lider123!"), Role.LIDER));
    }

    @Test
    void executesTheWholeSprintFlowWithRealMongo() throws Exception {
        JsonNode openApi = perform(get("/v3/api-docs"), null, null, 200);
        assertThat(openApi.at("/paths/~1api~1v1~1dashboard~1summary").isMissingNode()).isFalse();
        assertThat(openApi.at("/components/securitySchemes/bearerAuth").isMissingNode()).isFalse();

        mvc.perform(get("/api/v1/strategies/current")).andExpect(status().isUnauthorized());

        String leader = login("lider@test.local", "Lider123!");
        JsonNode strategy = perform(post("/api/v1/strategies"), leader, """
                {"category":"Eficiência","campaign":"Operação 2030",
                 "description":"Orientação estratégica para elevar eficiência operacional."}
                """, 201);
        String strategyId = strategy.get("id").asText();
        strategy = perform(patch("/api/v1/strategies/" + strategyId + "/activation"), leader, """
                {"action":"ACTIVATE","effectiveAt":"2026-09-15T12:00:00Z","version":%d}
                """.formatted(strategy.get("version").asLong()), 200);

        String operator = login("operador@test.local", "Operador123!");
        JsonNode idea = perform(post("/api/v1/ideas"), operator, """
                {"title":"Otimizar roteirização","description":"Otimizar rotas usando dados históricos da operação.",
                 "problem":"Deslocamentos acima do necessário.","expectedBenefit":"Reduzir distância e combustível.",
                 "strategyId":"%s"}
                """.formatted(strategyId), 201);
        String ideaId = idea.get("id").asText();
        idea = perform(post("/api/v1/ideas/" + ideaId + "/submit"), operator,
                "{" + "\"version\":" + idea.get("version").asLong() + "}", 200);

        String manager = login("gestor@test.local", "Gestor123!");
        idea = perform(post("/api/v1/ideas/" + ideaId + "/evaluation"), manager,
                "{" + "\"version\":" + idea.get("version").asLong() + "}", 200);
        idea = perform(patch("/api/v1/ideas/" + ideaId + "/priority"), manager, """
                {"priority":"ALTA","comment":"Alinhada à estratégia vigente.","version":%d}
                """.formatted(idea.get("version").asLong()), 200);
        idea = perform(post("/api/v1/ideas/" + ideaId + "/approval"), manager, """
                {"comment":"Viabilidade e benefício confirmados.","version":%d}
                """.formatted(idea.get("version").asLong()), 200);
        assertThat(idea.get("status").asText()).isEqualTo("APROVADA");

        JsonNode project = perform(post("/api/v1/projects"), manager, """
                {"name":"Roteirização eficiente","description":"Projeto para reduzir os deslocamentos operacionais.",
                 "strategyId":"%s","sourceIdeaId":"%s","stage":"PLANEJAMENTO",
                 "plannedStartDate":"2026-09-15","plannedEndDate":"2026-10-15","investment":1000.00}
                """.formatted(strategyId, ideaId), 201);
        String projectId = project.get("id").asText();
        project = perform(patch("/api/v1/projects/" + projectId + "/progress"), manager, """
                {"stage":"EXECUCAO","status":"EM_ANDAMENTO","progressPercent":50,
                 "note":"Piloto iniciado.","version":%d}
                """.formatted(project.get("version").asLong()), 200);
        project = perform(post("/api/v1/projects/" + projectId + "/results"), manager, """
                {"actualEndDate":"2026-10-10","financialReturn":1250.00,"productivityGainPercent":12.50,
                 "resultsSummary":"Resultados validados pela equipe operacional.","version":%d}
                """.formatted(project.get("version").asLong()), 200);
        assertThat(project.get("roiPercent").decimalValue()).isEqualByComparingTo("25.00");

        JsonNode summary = perform(get("/api/v1/dashboard/summary"), leader, null, 200);
        assertThat(summary.get("projectCount").asLong()).isEqualTo(1);
        assertThat(summary.get("totalProfit").decimalValue()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(summary.get("aggregateRoiPercent").decimalValue()).isEqualByComparingTo(new BigDecimal("25.00"));

        perform(post("/api/v1/projects"), leader, """
                {"name":"Projeto não autorizado","description":"Payload válido para verificar a autorização por papel.",
                 "strategyId":"%s","stage":"PLANEJAMENTO","plannedStartDate":"2026-09-15",
                 "plannedEndDate":"2026-10-15","investment":100.00}
                """.formatted(strategyId), 403);
    }

    private String login(String email, String password) throws Exception {
        JsonNode body = perform(post("/api/v1/auth/login"), null,
                "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}", 200);
        return body.get("accessToken").asText();
    }

    private JsonNode perform(MockHttpServletRequestBuilder request, String token, String body, int expectedStatus)
            throws Exception {
        request.contentType(MediaType.APPLICATION_JSON);
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.content(body);
        String response = mvc.perform(request).andExpect(status().is(expectedStatus))
                .andReturn().getResponse().getContentAsString();
        return response.isBlank() ? json.createObjectNode() : json.readTree(response);
    }
}
