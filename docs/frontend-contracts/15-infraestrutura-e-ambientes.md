# 15. Infraestrutura e ambientes

## Endereços para integração

O backend escuta por padrão em `http://localhost:8080`. O frontend deve receber a base URL por variável de ambiente própria, por exemplo:

```text
VITE_API_BASE_URL=http://localhost:8080
```

Não concatenar `/api/v1` duas vezes. Uma abordagem é configurar base `http://localhost:8080/api/v1` para o cliente de negócio e usar URL separada para `/actuator/health`.

## Execução local do backend

Opção comum:

```powershell
Copy-Item .env.example .env
docker compose up -d
$env:SPRING_PROFILES_ACTIVE = 'dev'
.\mvnw.cmd spring-boot:run
```

Stack completa em contêineres:

```powershell
docker compose --profile full up --build -d
```

O MongoDB roda autenticado como replica set de um nó. Isso é necessário para transações de troca da estratégia vigente e histórico.

## Perfis

### `dev`

- seed habilitado;
- Swagger UI habilitado;
- origens locais padrão: `http://localhost:3000` e `http://localhost:5173`.

### `test`

- configuração isolada usada por testes;
- integração usa MongoDB real via Testcontainers quando Docker está disponível.

### `prod`

- seed desabilitado;
- Swagger UI desabilitado;
- exige URI MongoDB, segredo/emissor/expiração JWT e origens CORS;
- segredos devem ser fornecidos pela plataforma.

## Variáveis relevantes ao frontend

| Backend | Impacto no frontend |
|---|---|
| `SERVER_PORT` | compõe a base URL local |
| `APP_CORS_ALLOWED_ORIGINS` | deve conter exatamente a origem do frontend |
| `JWT_EXPIRATION_SECONDS` | aparece como `expiresIn` no login |
| `SPRING_PROFILES_ACTIVE` | afeta seed e disponibilidade do Swagger UI |

As variáveis `SEED_*`, MongoDB e `JWT_SECRET` nunca devem ser copiadas para bundle/env público do frontend.

## Docker e CI

- Dockerfile multi-stage com Java 21 e usuário não-root;
- Compose inclui `mongodb`, inicialização de keyfile e `api` no profile `full`;
- CI executa testes, empacotamento, validação do Compose e build da imagem em pushes da `main` e pull requests;
- health check da API usa `/actuator/health`.

## Checklist de integração

- base URL correta para o ambiente;
- origem do frontend liberada no CORS;
- `Authorization` enviado apenas ao domínio da API;
- nenhum segredo de backend no frontend;
- telas e ações condicionadas à role de `/auth/me`;
- tratamento de campos opcionais, `409` e expiração do token;
- health, OpenAPI e Swagger não usados como dependência funcional da interface.
