# Validação e tratamento de erros

## Objetivo

Fazer toda falha previsível produzir resposta coerente, segura e útil ao frontend, sem espalhar `try/catch` pelos controllers.

## Camadas de validação

1. **Formato:** Bean Validation nos DTOs (`@NotBlank`, `@Size`, `@Email`, `@PositiveOrZero`).
2. **Consistência do DTO:** validadores de classe para intervalos de data e combinações de campos.
3. **Regra de negócio:** serviço valida estado, propriedade, vínculos e unicidade.
4. **Persistência:** índices e optimistic locking atuam como última barreira.

Usar `@Valid` nos corpos e `@Validated` para parâmetros. Normalizar strings e rejeitar valores formados só por espaços.

## Formato Problem Details

Adotar `application/problem+json`, aproveitando `ProblemDetail` do Spring:

```json
{
  "type": "https://api.exemplo.com/problems/validation-error",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/api/v1/ideas",
  "code": "VALIDATION_ERROR",
  "traceId": "abc123",
  "errors": [
    { "field": "title", "code": "Size", "message": "deve ter entre 5 e 150 caracteres" }
  ]
}
```

`traceId` ajuda suporte, mas não deve expor stack trace. `type` pode usar URI documental estável, sem exigir que ela esteja publicada na primeira versão.

## Mapeamento de status

| Situação | HTTP | Código sugerido |
|---|---:|---|
| JSON inválido / validação | 400 | `INVALID_REQUEST` / `VALIDATION_ERROR` |
| sem autenticação/token inválido | 401 | `UNAUTHORIZED` |
| autenticado sem role | 403 | `ACCESS_DENIED` |
| recurso inexistente/não visível ao dono | 404 | `RESOURCE_NOT_FOUND` |
| versão antiga, duplicidade, transição inválida | 409 | `CONFLICT`, `INVALID_STATE_TRANSITION` |
| mídia diferente de JSON | 415 | `UNSUPPORTED_MEDIA_TYPE` |
| rate limit, se aplicado | 429 | `RATE_LIMIT_EXCEEDED` |
| falha inesperada | 500 | `INTERNAL_ERROR` |

## Exceções sugeridas

- `ResourceNotFoundException`;
- `BusinessRuleException`;
- `InvalidStateTransitionException`;
- `ResourceConflictException`;
- `ResourceOwnershipException`;
- tratamento específico de `MethodArgumentNotValidException`, `ConstraintViolationException`, erro de parsing, duplicidade MongoDB e optimistic locking.

O `GlobalExceptionHandler` deve mapear exceções, não conter regra de domínio.

## Mensagens e internacionalização

Manter códigos estáveis para o frontend e mensagens legíveis em português. O cliente não deve tomar decisões com base no texto da mensagem. Evitar incluir valores sensíveis ou dados de outra pessoa.

## Critérios de aceite

- todos os erros possuem `status`, `code`, `title`, `detail`, `instance` e `traceId` quando disponível;
- erros de campo apontam o caminho correto, inclusive objetos/listas;
- nenhuma exceção de negócio vira `500`;
- stack trace não aparece fora do ambiente local;
- autenticação e autorização usam o mesmo formato dos demais erros.

## Testes esperados

- JSON malformado, enum inválido e campos ausentes;
- cada exceção de negócio;
- duplicidade e optimistic locking;
- erro inesperado sem vazamento de detalhes;
- `Content-Type` correto.
