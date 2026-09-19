# 5. Avaliação de ideias

## Acesso e fluxo

Todas as rotas deste documento exigem `GESTOR`.

```text
SUBMETIDA → EM_AVALIACAO → APROVADA
                         └→ REJEITADA
```

Somente ideias `SUBMETIDA` podem iniciar avaliação. Priorização e decisão exigem `EM_AVALIACAO`. `APROVADA` e `REJEITADA` são finais na versão atual.

## Listar ideias para gestão

`GET /api/v1/ideas`

Filtros opcionais:

- `status`: `RASCUNHO`, `SUBMETIDA`, `EM_AVALIACAO`, `APROVADA`, `REJEITADA`, `ARQUIVADA`;
- `priority`: `BAIXA`, `MEDIA`, `ALTA`, `CRITICA`;
- `strategyId`, `authorUserId`;
- `from`, `to`: instantes ISO 8601 aplicados a `createdAt`;
- `text`: busca textual implementada para os campos suportados pelo repositório;
- paginação e ordenação padrão.

Exemplo:

```http
GET /api/v1/ideas?status=SUBMETIDA&strategyId=abc&page=0&size=20&sort=submittedAt,asc
```

Sem `status`, a listagem exclui ideias `RASCUNHO`, mas pode incluir submetidas, em avaliação, aprovadas, rejeitadas e arquivadas. `text` procura um trecho, sem diferenciar maiúsculas/minúsculas, em `title`, `description` e `problem`. `from` e `to` limitam `createdAt` de forma inclusiva.

## Iniciar avaliação

`POST /api/v1/ideas/{id}/evaluation`

```json
{ "version": 1 }
```

Retorna `200` com a ideia em `EM_AVALIACAO` e nova `version`.

## Definir prioridade

`PATCH /api/v1/ideas/{id}/priority`

```json
{
  "priority": "ALTA",
  "comment": "Potencial relevante para redução de custos.",
  "version": 2
}
```

`priority` é obrigatória; `comment` é opcional e limitado a 2000 caracteres. O comentário informado substitui `managerComment` quando não vazio.

## Aprovar

`POST /api/v1/ideas/{id}/approval`

```json
{
  "priority": "CRITICA",
  "comment": "Aprovada para planejamento e implantação.",
  "version": 3
}
```

`comment`: obrigatório, 10–2000 caracteres. `priority` é opcional no payload apenas se a ideia já tiver prioridade; caso contrário retorna `409 PRIORITY_REQUIRED`.

## Rejeitar

`POST /api/v1/ideas/{id}/rejection`

```json
{
  "comment": "Rejeitada por não apresentar benefício mensurável.",
  "version": 3
}
```

O DTO também aceita `priority`, mas ela não é aplicada pelo fluxo de rejeição. A ideia já precisa ter prioridade definida; caso contrário retorna `409 PRIORITY_REQUIRED`.

## Resposta após decisão

Além dos campos básicos:

```json
{
  "status": "APROVADA",
  "priority": "CRITICA",
  "managerComment": "Aprovada para planejamento e implantação.",
  "decidedAt": "2026-09-19T15:00:00Z",
  "decidedBy": "manager-id",
  "version": 4
}
```

## Erros relevantes

- `404 RESOURCE_NOT_FOUND`;
- `409 STALE_VERSION`;
- `409 INVALID_STATE_TRANSITION`;
- `409 PRIORITY_REQUIRED`;
- `400 VALIDATION_ERROR` para comentário curto/ausente e enum inválido (`INVALID_REQUEST` quando o enum não pode ser desserializado).
