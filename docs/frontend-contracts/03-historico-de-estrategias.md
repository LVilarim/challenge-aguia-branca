# 3. Histórico de estratégias

## Visão geral

Cada criação, atualização, ativação, encerramento ou arquivamento gera um snapshot imutável. A gravação do histórico participa da transação usada nas mudanças de estratégia.

Eventos: `CREATED`, `UPDATED`, `ACTIVATED`, `CLOSED`, `ARCHIVED`.

## Listar histórico de uma estratégia

`GET /api/v1/strategies/{strategyId}/history` — qualquer usuário autenticado.

Query parameters:

- `eventType`: enum opcional;
- `from`, `to`: instantes ISO 8601 opcionais;
- `page`, `size`, `sort`;
- ordenáveis: `occurredAt`, `eventType`, `strategyVersion`;
- padrão: `occurredAt,desc`.

Resposta paginada com itens:

```json
{
  "id": "history-id",
  "strategyId": "strategy-id",
  "version": 2,
  "eventType": "ACTIVATED",
  "occurredAt": "2026-09-19T12:00:00Z",
  "actorUserId": "leader-id",
  "snapshot": {
    "id": "strategy-id",
    "occurredAt": "2026-09-19T12:00:00Z",
    "category": "Eficiência operacional",
    "campaign": "Operação 2026",
    "description": "Descrição da estratégia.",
    "status": "ATIVA",
    "validFrom": "2026-09-19T12:00:00Z"
  }
}
```

Observação: o campo externo `version` representa `strategyVersion`, não uma versão editável do evento histórico.

## Consultar evento específico

`GET /api/v1/strategy-history/{historyId}` — qualquer usuário autenticado.

Retorna o mesmo formato de item. Evento inexistente: `404 RESOURCE_NOT_FOUND`.

## Orientação para a interface

- ordenar como timeline usando `occurredAt`;
- mostrar `eventType` com rótulo localizado, sem alterar o valor enviado ao backend;
- apresentar o `snapshot`, e não o estado atual da estratégia, ao abrir uma versão histórica;
- não oferecer ações de edição/exclusão: o histórico é somente leitura;
- a listagem não valida previamente se o `strategyId` existe; uma estratégia sem eventos produz página vazia.
