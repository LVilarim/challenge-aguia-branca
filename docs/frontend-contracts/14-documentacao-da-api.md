# 14. Documentação da API

## Endpoints

- OpenAPI JSON: `GET /v3/api-docs`;
- Swagger UI: `GET /swagger-ui.html`.

Ambos são públicos nos perfis em que estão habilitados. A UI Swagger fica desabilitada pelo perfil `prod`; o JSON não foi explicitamente desabilitado na configuração atual.

## Bearer JWT

O OpenAPI define o security scheme `bearerAuth`:

```text
type: http
scheme: bearer
bearerFormat: JWT
```

O login remove o requisito de segurança por anotação. Demais operações herdam bearer auth global.

## Tags

- `Auth`;
- `Strategies`;
- `Strategy History`;
- `Ideas`;
- `Projects`;
- `Dashboard`.

## Respostas documentadas

Um customizador acrescenta a todas as operações os status `400`, `401`, `403`, `404`, `409` e `500` com schema `ProblemDetail`.

Atenção: o schema global de `ProblemDetail` contém `type`, `title`, `status`, `detail`, `instance`, `code` e `traceId`, mas atualmente não declara o array opcional `errors` que aparece em validações. Para tratamento de formulário, seguir também [Tratamento de erros](11-tratamento-de-erros.md).

## Uso pelo frontend

- o JSON pode ser usado para gerar tipos/clientes, mas revise enums, nullability e operações geradas;
- campos `null` são omitidos em runtime, então os tipos de resposta correspondentes devem ser opcionais;
- não gerar novamente tipos silenciosamente em CI sem revisar diffs de contrato;
- manter o prefixo `/api/v1` configurável pela base URL;
- use estes documentos para regras de UX e o OpenAPI como fonte mecânica de rotas/schemas.

## Verificação manual

1. subir a API em `dev`;
2. abrir Swagger UI;
3. executar `/auth/login`;
4. usar `Authorize` com o JWT;
5. testar a rota conforme a role do usuário;
6. validar também erros e versões concorrentes, não apenas respostas `2xx`.
