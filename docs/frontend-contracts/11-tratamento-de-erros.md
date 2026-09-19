# 11. Tratamento de erros

## Formato padrão

Content-Type: `application/problem+json`.

```json
{
  "type": "https://api.aguia-branca.local/problems/validation-error",
  "title": "Bad Request",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/api/v1/ideas",
  "code": "VALIDATION_ERROR",
  "traceId": "web-req-123",
  "errors": [
    {
      "field": "title",
      "code": "Size",
      "message": "deve ter tamanho entre 5 e 150"
    }
  ]
}
```

`errors` só aparece quando há violações por campo/parâmetro. O frontend deve decidir o comportamento por `status` e `code`, nunca pelo texto de `detail`.

## Status e códigos comuns

| HTTP | `code` | Uso |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | Bean Validation em campos/parâmetros |
| 400 | `INVALID_REQUEST` | JSON, enum, tipo ou parâmetro inválido/ausente |
| 401 | `UNAUTHORIZED` | autenticação ausente ou inválida |
| 403 | `ACCESS_DENIED` | role sem permissão ou conta inativa no login |
| 404 | `RESOURCE_NOT_FOUND` | recurso inexistente ou ideia alheia |
| 409 | `DUPLICATE_RESOURCE` | índice único violado |
| 409 | `STALE_VERSION` | concorrência otimista |
| 409 | código da regra | estado, vínculo, datas ou valores incompatíveis |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | `Content-Type` não suportado |
| 500 | `INTERNAL_ERROR` | falha inesperada |

## Códigos de negócio observados

- estratégias: `ACTIVE_STRATEGY_EXISTS`, `INVALID_DATE_RANGE`, `INVALID_STATE_TRANSITION`;
- ideias: `STRATEGY_NOT_CURRENT`, `PRIORITY_REQUIRED`, `INVALID_STATE_TRANSITION`;
- projetos: `STRATEGY_NOT_CURRENT`, `IDEA_NOT_APPROVED`, `STRATEGY_MISMATCH`, `IDEA_ALREADY_HAS_PROJECT`, `RESULTS_REQUIRED`, `INVALID_PROGRESS`, `INVALID_DATE_RANGE`, `INVALID_PERCENTAGE`, `NEGATIVE_VALUE`, `INVALID_STATE_TRANSITION`.

Todos esses códigos de regra são `409` na implementação atual.

## Estratégia de tratamento no frontend

1. tentar interpretar o corpo como Problem Details;
2. correlacionar `errors[].field` ao formulário;
3. para `STALE_VERSION`, avisar que o registro mudou e recarregá-lo;
4. para `INVALID_STATE_TRANSITION`, atualizar o recurso e recalcular ações disponíveis;
5. mostrar mensagem geral baseada em `detail`, com fallback próprio;
6. incluir `traceId` em telas de suporte/erro técnico;
7. não exibir stack trace: o backend não o envia.

O header `X-Correlation-Id` normalmente tem o mesmo valor de `traceId`. Um erro de rede/CORS pode não conter Problem Details e precisa de fallback separado.
