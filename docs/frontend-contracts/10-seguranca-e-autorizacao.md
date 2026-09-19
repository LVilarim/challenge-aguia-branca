# 10. Segurança e autorização

## Autenticação stateless

O backend não cria sessão HTTP. Cada requisição protegida deve enviar:

```http
Authorization: Bearer <accessToken>
```

O JWT usa HS256 e contém `iss`, `sub` (ID do usuário), `iat`, `exp`, `jti` e `role`. A autorização efetiva não confia apenas na role do token: a cada requisição o backend busca o usuário pelo `sub`, confirma que está ativo e usa a role atual do banco.

## Rotas públicas

- `POST /api/v1/auth/login`;
- `/v3/api-docs/**`;
- `/swagger-ui.html` e `/swagger-ui/**`;
- `/actuator/health`.

Todas as outras rotas exigem token válido.

## Matriz de autorização

| Operação | OPERADOR | GESTOR | LIDER |
|---|:---:|:---:|:---:|
| `/auth/me` | ✓ | ✓ | ✓ |
| Consultar estratégias/histórico | ✓ | ✓ | ✓ |
| Criar/alterar/ativar/arquivar estratégia | — | — | ✓ |
| Criar/listar próprias/alterar/submeter/arquivar ideia | ✓ | — | — |
| Listar todas/avaliar/priorizar/decidir ideia | — | ✓ | — |
| Consultar projeto | — | ✓ | ✓ |
| Criar/alterar/progresso/resultados/arquivar projeto | — | ✓ | — |
| Dashboard | — | — | ✓ |

Não existe herança: `LIDER` não pode executar automaticamente operações de `GESTOR` ou `OPERADOR`.

## Comportamento de 401 e 403

- `401 UNAUTHORIZED`: token ausente, inválido, expirado, emissor incorreto, usuário inexistente/inativo durante validação ou autenticação necessária;
- `403 ACCESS_DENIED`: token válido, mas role sem permissão; também é usado para conta inativa detectada no login.

Ambas seguem `application/problem+json`. No frontend:

- em `401`, limpar credenciais e redirecionar ao login;
- em `403`, preservar sessão e exibir acesso negado, exceto se a política do produto decidir encerrar sessão após conta inativa;
- ocultar ações por role para experiência do usuário, sem tratar isso como segurança suficiente.

## CORS

- origens vêm de `APP_CORS_ALLOWED_ORIGINS`;
- métodos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`;
- request headers: `Authorization`, `Content-Type`, `X-Correlation-Id`;
- response headers expostos: `Location`, `X-Correlation-Id`;
- `allowCredentials=false`: não enviar cookies/`credentials: include`.

Se o navegador bloquear a requisição antes de existir resposta HTTP, revisar a origem configurada no backend; isso não é um erro de contrato da rota.
