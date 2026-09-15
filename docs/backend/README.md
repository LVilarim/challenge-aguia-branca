# Backend — guia de implementação da Sprint 2

Este diretório é a especificação modular para implementar o backend do **Challenge Águia Branca**. Ele transforma os requisitos funcionais da Sprint 2 em contratos, regras de negócio, estrutura de código, critérios de aceite e estratégia de testes.

> Escopo atual: autenticação, usuários e perfis, estratégias e histórico, ideias e aprovação, projetos/iniciativas, dashboard e requisitos transversais. **Integrações com IA estão deliberadamente fora desta entrega.**

## Estado inicial confirmado no repositório

- Java 21;
- Spring Boot 4.1.1;
- Maven Wrapper;
- pacote raiz `com.challenge.aguiabranca.api`;
- Spring MVC, Security, Validation, Data MongoDB e Actuator;
- MongoDB iniciado por `compose.yaml`;
- ainda não há implementação de domínio;
- bibliotecas de JWT e OpenAPI ainda deverão ser escolhidas e adicionadas durante a implementação, validando compatibilidade com Spring Boot 4.

## Ordem recomendada de leitura e implementação

1. [Escopo e requisitos](00-escopo-e-requisitos.md)
2. [Arquitetura e organização](01-arquitetura-e-organizacao.md)
3. [Banco de dados e modelagem](02-banco-e-modelagem.md)
4. [Autenticação e JWT](03-autenticacao-e-jwt.md)
5. [Usuários, perfis e permissões](04-usuarios-roles-e-permissoes.md)
6. [Estratégias](05-estrategias.md)
7. [Histórico de estratégias](06-historico-de-estrategias.md)
8. [Ideias](07-ideias.md)
9. [Priorização e aprovação](08-priorizacao-e-aprovacao.md)
10. [Projetos e iniciativas](09-projetos-e-iniciativas.md)
11. [Dashboard e relatórios](10-dashboard-e-relatorios.md)
12. [Validação e tratamento de erros](11-validacao-e-erros.md)
13. [Segurança](12-seguranca.md)
14. [Convenções REST e OpenAPI](13-api-rest-e-openapi.md)
15. [Testes](14-testes.md)
16. [Docker e ambientes](15-docker-e-ambientes.md)
17. [Observabilidade](16-observabilidade.md)
18. [Execução e deploy](17-execucao-e-deploy.md)
19. [Plano de implementação](18-plano-de-implementacao.md)

## Decisões globais

- API versionada sob `/api/v1`.
- Autenticação stateless com access token JWT.
- Sem cadastro público de usuários na Sprint 2.
- Identificadores MongoDB expostos como `String`.
- Datas e horas em UTC no backend e em ISO 8601 na API.
- Valores monetários em `BigDecimal`, nunca `double`.
- Listagens paginadas e ordenadas.
- DTOs separados dos documentos MongoDB.
- Controllers finos; regras de negócio na camada de aplicação/serviço.
- Autorização tanto na configuração HTTP quanto no serviço para regras de propriedade.
- Exclusão lógica quando o registro já participa de histórico ou relatórios.
- Erros no padrão Problem Details (`application/problem+json`).

## Definição de pronto da Sprint 2

Uma funcionalidade só é considerada concluída quando possui regra de autorização aplicada, validação de entrada, contrato OpenAPI, testes automatizados pertinentes, erros padronizados e instruções de execução atualizadas. Todos os endpoints devem operar sobre MongoDB real em ambiente de desenvolvimento/teste de integração, sem mocks no fluxo entregue ao frontend.

## Fora do escopo

- IA generativa, scoring por IA ou insights automáticos;
- refresh token, SSO, OAuth2 social e recuperação de senha;
- gerenciamento completo de usuários por interface administrativa;
- gráficos renderizados pelo backend — o backend entrega séries e agregados; o frontend renderiza os gráficos;
- mensageria, Elasticsearch e arquitetura de microsserviços.
