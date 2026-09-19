# 6. Projetos

## Tipos

Etapas: `DESCOBERTA`, `PLANEJAMENTO`, `EXECUCAO`, `VALIDACAO`, `ENCERRAMENTO`.

Status: `PLANEJADO`, `EM_ANDAMENTO`, `PAUSADO`, `CONCLUIDO`, `CANCELADO`.

Gestor cria e modifica. Gestor e líder consultam. Operador não acessa as rotas de projetos.

## Formato de resposta

```json
{
  "id": "project-id",
  "name": "Otimização de rotas",
  "description": "Projeto para reduzir distância e consumo.",
  "strategyId": "strategy-id",
  "sourceIdeaId": "idea-id",
  "managerUserId": "manager-id",
  "stage": "PLANEJAMENTO",
  "status": "PLANEJADO",
  "progressPercent": 0,
  "plannedStartDate": "2026-10-01",
  "plannedEndDate": "2026-12-20",
  "investment": 150000.00,
  "profit": -150000.00,
  "active": true,
  "progressHistory": [],
  "version": 0,
  "createdAt": "2026-09-19T14:00:00Z",
  "updatedAt": "2026-09-19T14:00:00Z"
}
```

Campos sem valor, incluindo `financialReturn` e `roiPercent`, são omitidos. Antes de resultados, `profit` considera retorno zero e pode ser negativo.

## Criar

`POST /api/v1/projects` — GESTOR; retorna `201` e `Location`.

```json
{
  "name": "Otimização de rotas",
  "description": "Projeto para reduzir distância e consumo.",
  "strategyId": "strategy-id",
  "sourceIdeaId": "idea-id",
  "stage": "PLANEJAMENTO",
  "plannedStartDate": "2026-10-01",
  "plannedEndDate": "2026-12-20",
  "investment": 150000.00
}
```

Regras:

- nome 3–150; descrição 10–5000;
- investimento obrigatório e não negativo;
- fim planejado não pode anteceder início;
- estratégia deve ser a vigente;
- `sourceIdeaId` é opcional; se informado, a ideia deve estar `APROVADA`, pertencer à mesma estratégia e não ter originado outro projeto;
- responsável é obtido do JWT.

## Listar

`GET /api/v1/projects` — GESTOR ou LIDER.

Filtros: `strategyId`, `sourceIdeaId`, `managerUserId`, `stage`, `status`, `from`, `to`, `delayed`, `page`, `size`, `sort`.

`from` e `to` são datas `YYYY-MM-DD`. Campos ordenáveis: `createdAt`, `updatedAt`, `name`, `status`, `stage`, `plannedEndDate`, `progressPercent`, `investment`.

A listagem sempre exclui projetos com `active=false`. `from` significa `plannedStartDate >= from`; `to` significa `plannedEndDate <= to`. O filtro de atraso só é aplicado quando `delayed=true`; enviar `false` equivale a não filtrar por atraso.

## Consultar

`GET /api/v1/projects/{id}` — GESTOR ou LIDER.

## Atualizar planejamento

`PUT /api/v1/projects/{id}` — GESTOR.

```json
{
  "name": "Otimização de rotas — fase 1",
  "description": "Descrição atualizada do projeto.",
  "stage": "EXECUCAO",
  "plannedStartDate": "2026-10-01",
  "plannedEndDate": "2027-01-15",
  "investment": 175000.00,
  "version": 1
}
```

Não altera estratégia, ideia de origem nem responsável. Projeto `CONCLUIDO` ou `CANCELADO` não aceita mudança de plano.

## Arquivar/cancelar

`DELETE /api/v1/projects/{id}?version=4` — GESTOR; retorna `204`.

- concluído: mantém `CONCLUIDO`, define `active=false` e registra histórico;
- demais estados: muda para `CANCELADO`, define `active=false` e registra histórico;
- repetição sobre cancelado/inativo é idempotente, desde que a versão enviada seja atual.

## Erros de negócio

- `409 STRATEGY_NOT_CURRENT`;
- `409 IDEA_NOT_APPROVED`;
- `409 STRATEGY_MISMATCH`;
- `409 IDEA_ALREADY_HAS_PROJECT`;
- `409 INVALID_DATE_RANGE`, `NEGATIVE_VALUE`, `INVALID_STATE_TRANSITION` ou `STALE_VERSION`;
- `404 RESOURCE_NOT_FOUND`.
