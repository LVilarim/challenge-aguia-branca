# 2. Estratégias

## Tipos

`StrategyStatus`: `RASCUNHO`, `ATIVA`, `ENCERRADA`, `ARQUIVADA`.

`ActivationAction`: `ACTIVATE`, `CLOSE`.

Resposta de estratégia:

```json
{
  "id": "strategy-id",
  "category": "Eficiência operacional",
  "campaign": "Operação 2026",
  "description": "Descrição da orientação estratégica.",
  "status": "ATIVA",
  "validFrom": "2026-09-19T12:00:00Z",
  "version": 1,
  "createdBy": "user-id",
  "updatedBy": "user-id",
  "createdAt": "2026-09-19T11:00:00Z",
  "updatedAt": "2026-09-19T12:00:00Z"
}
```

Campos nulos, como `validUntil`, podem não aparecer.

## Endpoints

| Método | Rota | Acesso | Resposta |
|---|---|---|---|
| `POST` | `/api/v1/strategies` | LIDER | `201`, corpo e `Location` |
| `GET` | `/api/v1/strategies` | autenticado | página de estratégias |
| `GET` | `/api/v1/strategies/current` | autenticado | estratégia `ATIVA` |
| `GET` | `/api/v1/strategies/{id}` | autenticado | estratégia |
| `PUT` | `/api/v1/strategies/{id}` | LIDER | estratégia atualizada |
| `PATCH` | `/api/v1/strategies/{id}/activation` | LIDER | estratégia atualizada |
| `DELETE` | `/api/v1/strategies/{id}` | LIDER | `204` |

## Criar

```json
{
  "category": "Eficiência operacional",
  "campaign": "Operação 2026",
  "description": "Descrição com no mínimo dez caracteres.",
  "validFrom": "2026-09-19T12:00:00Z",
  "validUntil": "2026-12-31T23:59:59Z"
}
```

Limites: categoria 2–80, campanha 2–120, descrição 10–4000. Datas são opcionais, mas `validUntil` não pode anteceder `validFrom`. A estratégia nasce `RASCUNHO`.

## Listar

`GET /api/v1/strategies?status=ATIVA&category=Eficiência&campaign=2026&validFrom=2026-01-01T00:00:00Z&validUntil=2026-12-31T23:59:59Z&page=0&size=20&sort=createdAt,desc`

Campos de ordenação: `createdAt`, `updatedAt`, `category`, `campaign`, `validFrom`, `status`.

`category` e `campaign` fazem busca parcial sem diferenciar maiúsculas/minúsculas. `validFrom` seleciona registros com início maior ou igual ao valor; `validUntil`, registros com fim menor ou igual.

## Atualizar

`PUT` exige todos os campos editáveis e `version`:

```json
{
  "category": "Eficiência operacional",
  "campaign": "Operação 2026 revisada",
  "description": "Nova descrição da estratégia.",
  "validFrom": "2026-09-19T12:00:00Z",
  "validUntil": null,
  "version": 1
}
```

Estratégia arquivada não pode ser editada.

## Ativar ou encerrar

```json
{
  "action": "ACTIVATE",
  "effectiveAt": "2026-09-19T12:00:00Z",
  "version": 1
}
```

Ao ativar uma estratégia, a vigente anterior é encerrada na mesma operação. `CLOSE` só é válido para estratégia ativa. Ativação aceita `RASCUNHO` ou `ENCERRADA`.

## Arquivar

`DELETE /api/v1/strategies/{id}` não recebe `version` atualmente. Uma estratégia ativa deve ser encerrada antes. Repetir o arquivamento é idempotente e retorna `204`.

## Erros de negócio

- `404 RESOURCE_NOT_FOUND`: estratégia/id ou vigente não encontrada;
- `409 STALE_VERSION`: versão desatualizada;
- `409 ACTIVE_STRATEGY_EXISTS`: conflito ao manter vigência única;
- `409 INVALID_DATE_RANGE`: datas incompatíveis;
- `409 INVALID_STATE_TRANSITION`: ação incompatível com o status.
