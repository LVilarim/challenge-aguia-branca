# Challenge Águia Branca — API

Backend da Sprint 2 para acompanhar o ciclo entre estratégia, ideia de inovação, aprovação, projeto e resultado. A API usa Java 21, Spring Boot 4.1, MongoDB, autenticação JWT stateless e OpenAPI.

## Pré-requisitos

- JDK 21;
- Docker Desktop/Engine com Compose;
- PowerShell no Windows ou um shell compatível em Linux/macOS.

## Execução local

O MongoDB roda como replica set de um nó porque a troca de estratégia vigente e a gravação de seu histórico são transacionais.

```powershell
Copy-Item .env.example .env
docker compose up -d
$env:SPRING_PROFILES_ACTIVE = 'dev'
.\mvnw.cmd spring-boot:run
```

Com os valores locais do exemplo, a API fica em `http://localhost:8080`. Confira:

- health: `http://localhost:8080/actuator/health`;
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`;
- Swagger UI: `http://localhost:8080/swagger-ui.html`.

O perfil `dev` cria, de forma idempotente, um usuário de cada papel. As credenciais locais estão em `.env.example` e devem ser alteradas para qualquer ambiente compartilhado. O seed nunca redefine um usuário existente e não executa em produção.

Para subir MongoDB e API juntos em contêineres:

```powershell
docker compose --profile full up --build -d
```

## Autenticação

Envie `POST /api/v1/auth/login`:

```json
{
  "email": "lider@aguia.local",
  "password": "Lider123!"
}
```

Use o token retornado nas demais rotas:

```text
Authorization: Bearer <accessToken>
```

Os papéis não possuem herança implícita:

- `OPERADOR`: consulta estratégias e gerencia apenas as próprias ideias;
- `GESTOR`: avalia ideias e gerencia projetos/resultados;
- `LIDER`: gerencia estratégias, consulta projetos e acessa o dashboard.

## Recursos HTTP

Todas as rotas de negócio usam o prefixo `/api/v1`.

| Recurso | Rotas principais |
|---|---|
| Autenticação | `POST /auth/login`, `GET /auth/me` |
| Estratégias | CRUD lógico, `/strategies/current`, `/{id}/activation` |
| Histórico | `/strategies/{id}/history`, `/strategy-history/{historyId}` |
| Ideias | `/ideas/mine`, `/submit`, `/evaluation`, `/priority`, `/approval`, `/rejection` |
| Projetos | CRUD lógico, `/progress`, `/results` |
| Dashboard | `/summary`, `/by-strategy`, `/by-project`, `/charts` |

Listagens aceitam `page`, `size` (máximo 100) e `sort=campo,direção`. Filtros e enums completos aparecem no OpenAPI. Mutações de estratégias, ideias e projetos exigem `version`; uma versão antiga retorna `409`.

Erros seguem `application/problem+json`, com `code`, `instance`, `traceId` e erros por campo quando aplicável. O header `X-Correlation-Id` pode ser enviado pelo cliente (até 64 caracteres seguros) e é devolvido na resposta.

## Testes e build

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

A suíte inclui testes de domínio e um fluxo HTTP ponta a ponta em MongoDB real via Testcontainers. Quando o Docker não está disponível, somente esse teste de integração é ignorado; com Docker ativo, toda a jornada de autenticação a dashboard é executada.

Validações adicionais:

```powershell
docker compose config
docker build -t aguia-branca-api:local .
```

## Configuração de produção

O perfil `prod` exige explicitamente:

- `SPRING_MONGODB_URI` para um replica set autenticado;
- `JWT_SECRET` aleatório com ao menos 32 bytes;
- `JWT_ISSUER`;
- `JWT_EXPIRATION_SECONDS`;
- `APP_CORS_ALLOWED_ORIGINS`.

Swagger UI e seed ficam desativados em produção. Injete segredos pela plataforma, termine TLS no proxy/plataforma e não versione o arquivo `.env`.

## Decisões técnicas

- DTOs não expõem documentos MongoDB nem hashes de senha;
- dinheiro usa `BigDecimal`/Decimal128 e arredondamento `HALF_UP`;
- estratégia vigente e snapshot histórico são confirmados na mesma transação MongoDB;
- IDs de usuário vêm do JWT, nunca do payload;
- exclusões históricas são lógicas;
- ROI agregado usa totais ponderados, não média de percentuais individuais;
- somente `health` e `info` são expostos pelo Actuator.

A especificação detalhada e os critérios de aceite estão em [docs/backend/README.md](docs/backend/README.md).
