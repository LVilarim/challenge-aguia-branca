# 4. Ideias de inovação

## Tipos e acesso

Status: `RASCUNHO`, `SUBMETIDA`, `EM_AVALIACAO`, `APROVADA`, `REJEITADA`, `ARQUIVADA`.

Prioridade: `BAIXA`, `MEDIA`, `ALTA`, `CRITICA`.

Operadores criam e gerenciam apenas as próprias ideias. Gestores podem consultar todas e avaliá-las. Líder não possui acesso às rotas de ideias na implementação atual.

## Formato de resposta

```json
{
  "id": "idea-id",
  "title": "Reduzir consumo de combustível",
  "description": "Descrição detalhada da proposta de inovação.",
  "problem": "Consumo acima do esperado nas rotas urbanas.",
  "expectedBenefit": "Redução de custos e emissões.",
  "strategyId": "strategy-id",
  "authorUserId": "operator-id",
  "status": "RASCUNHO",
  "version": 0,
  "createdAt": "2026-09-19T12:00:00Z",
  "updatedAt": "2026-09-19T12:00:00Z"
}
```

Campos de avaliação ausentes ainda não aparecem no JSON.

## Criar ideia

`POST /api/v1/ideas` — OPERADOR. Retorna `201` e `Location`.

```json
{
  "title": "Reduzir consumo de combustível",
  "description": "Descrição detalhada com pelo menos vinte caracteres.",
  "problem": "Problema com pelo menos dez caracteres.",
  "expectedBenefit": "Benefício esperado opcional.",
  "strategyId": "strategy-id"
}
```

Limites: título 5–150, descrição 20–5000, problema 10–3000, benefício até 2000. `strategyId` deve ser exatamente a estratégia vigente. O autor vem do JWT.

## Listar próprias ideias

`GET /api/v1/ideas/mine?page=0&size=20&sort=createdAt,desc` — OPERADOR.

Não há filtros de status/texto nesta rota. Campos de ordenação: `createdAt`, `updatedAt`, `title`, `status`, `priority`, `submittedAt`, `decidedAt`.

## Consultar ideia

`GET /api/v1/ideas/{id}` — OPERADOR ou GESTOR.

Para operador, ideia alheia retorna `404`, evitando revelar sua existência. Gestor consulta qualquer ideia.

## Atualizar ideia

`PUT /api/v1/ideas/{id}` — somente o OPERADOR proprietário.

```json
{
  "title": "Título revisado",
  "description": "Descrição revisada e suficientemente detalhada.",
  "problem": "Problema revisado da operação.",
  "expectedBenefit": "Novo benefício esperado.",
  "version": 0
}
```

Permitido somente em `RASCUNHO` ou `SUBMETIDA`.

## Enviar para avaliação

`POST /api/v1/ideas/{id}/submit` — OPERADOR proprietário.

```json
{ "version": 1 }
```

Transição válida: `RASCUNHO → SUBMETIDA`.

## Arquivar

`DELETE /api/v1/ideas/{id}?version=2` — OPERADOR proprietário; resposta `204`.

Permitido em `RASCUNHO` ou `SUBMETIDA`; muda o status para `ARQUIVADA`.

## Erros relevantes

- `404 RESOURCE_NOT_FOUND`: ideia inexistente ou não pertencente ao operador;
- `409 STALE_VERSION`: versão desatualizada;
- `409 STRATEGY_NOT_CURRENT`: criação vinculada a estratégia não vigente;
- `409 INVALID_STATE_TRANSITION`: ação proibida no status atual.
