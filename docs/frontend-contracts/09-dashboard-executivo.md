# 9. Dashboard executivo

## Acesso e filtros comuns

Todos os endpoints exigem `LIDER`.

Filtros opcionais comuns:

- `strategyId`;
- `projectId`;
- `from`, `to`: instantes ISO 8601 aplicados pelo repositório ao período de criação.

Todas as consultas do dashboard excluem projetos `active=false` e `CANCELADO`. `from` e `to` limitam `createdAt` de forma inclusiva; portanto não representam datas planejadas ou reais do projeto.

Campos nulos são omitidos. Médias sem dados e ROI com investimento zero podem não aparecer.

## Resumo geral

`GET /api/v1/dashboard/summary`

```json
{
  "projectCount": 8,
  "completedProjectCount": 3,
  "activeProjectCount": 4,
  "delayedProjectCount": 1,
  "totalInvestment": 500000.00,
  "totalFinancialReturn": 640000.00,
  "totalProfit": 140000.00,
  "aggregateRoiPercent": 28.00,
  "averageProgressPercent": 61.25,
  "averageProductivityGainPercent": 10.75,
  "averageDurationDays": 84.50,
  "generatedAt": "2026-09-19T16:00:00Z",
  "appliedFilters": {
    "strategyId": "strategy-id"
  }
}
```

Ativos são projetos `EM_ANDAMENTO` ou `PAUSADO`. Atrasados têm `plannedEndDate` anterior à data corrente e não estão concluídos/cancelados. ROI agregado usa totais, não a média dos ROIs individuais.

## Indicadores por estratégia

`GET /api/v1/dashboard/by-strategy`

Retorna array, não página:

```json
[
  {
    "strategyId": "strategy-id",
    "category": "Eficiência",
    "campaign": "Operação 2026",
    "projectCount": 3,
    "totalInvestment": 250000.00,
    "totalFinancialReturn": 320000.00,
    "profit": 70000.00,
    "roiPercent": 28.00,
    "averageProgressPercent": 75.00,
    "averageProductivityGainPercent": 12.50,
    "averageDurationDays": 90.00
  }
]
```

## Indicadores por projeto

`GET /api/v1/dashboard/by-project` — paginado.

Aceita filtros comuns mais `page`, `size`, `sort`. Ordenáveis: `createdAt`, `name`, `status`, `progressPercent`, `investment`, `financialReturn`, `plannedEndDate`.

Item:

```json
{
  "projectId": "project-id",
  "name": "Otimização de rotas",
  "strategyId": "strategy-id",
  "status": "CONCLUIDO",
  "progressPercent": 100,
  "plannedEndDate": "2026-12-20",
  "actualEndDate": "2026-12-18",
  "investment": 150000.00,
  "financialReturn": 210000.00,
  "profit": 60000.00,
  "roiPercent": 40.00,
  "productivityGainPercent": 12.50,
  "delayed": false
}
```

## Dados para gráficos

`GET /api/v1/dashboard/charts?metric=ROI_PERCENT&groupBy=STRATEGY`

`metric`: `ROI_PERCENT`, `PROFIT`, `INVESTMENT`, `FINANCIAL_RETURN`, `PRODUCTIVITY_GAIN_PERCENT`, `PROGRESS_PERCENT`.

`groupBy`: `STRATEGY`, `PROJECT`.

```json
{
  "metric": "ROI_PERCENT",
  "groupBy": "STRATEGY",
  "generatedAt": "2026-09-19T16:00:00Z",
  "series": [
    { "key": "strategy-id", "label": "Operação 2026", "value": 28.00 }
  ]
}
```

`metric` e `groupBy` são obrigatórios. Valor sem dados pode ser omitido no item da série. A escolha do tipo de gráfico é responsabilidade do frontend.
