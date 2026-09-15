# Dashboard e endpoints de relatório

## Objetivo

Entregar a líderes agregações consistentes para resumo geral e análises por estratégia ou projeto. O frontend é responsável pela representação visual; a API retorna números, categorias e séries.

## Permissão

Todos os endpoints deste módulo exigem `LIDER`. Gestores podem consultar projetos individuais, mas não recebem automaticamente o dashboard executivo.

## Endpoints

### `GET /api/v1/dashboard/summary`

Filtros opcionais: `strategyId`, `projectId`, `from`, `to`. Retorna totais gerais.

### `GET /api/v1/dashboard/by-strategy`

Retorna uma linha por estratégia com quantidade de projetos, investimento, retorno, lucro, ROI agregado, prazo e produtividade.

### `GET /api/v1/dashboard/by-project`

Retorna métricas detalhadas por projeto, paginadas e ordenáveis.

### `GET /api/v1/dashboard/charts`

Recebe `metric`, `groupBy` e período controlados por enum/allowlist. Retorna séries, por exemplo:

```json
{
  "metric": "ROI_PERCENT",
  "groupBy": "STRATEGY",
  "generatedAt": "2026-09-15T13:00:00Z",
  "series": [
    { "key": "strategy-id", "label": "Campanha X", "value": 24.50 }
  ]
}
```

Não aceitar nome de campo MongoDB livre no parâmetro de ordenação/agregação.

## DTO de resumo sugerido

```text
DashboardSummaryResponse
- projectCount
- completedProjectCount
- activeProjectCount
- delayedProjectCount
- totalInvestment
- totalFinancialReturn
- totalProfit
- aggregateRoiPercent
- averageProgressPercent
- averageProductivityGainPercent
- averageDurationDays
- generatedAt
- appliedFilters
```

## Fórmulas

- `lucro = Σ retorno financeiro − Σ investimento`;
- `ROI agregado (%) = (Σ retorno − Σ investimento) / Σ investimento × 100`;
- aumento médio de produtividade: média apenas de projetos que informaram a métrica;
- prazo médio: média entre início e término reais de projetos concluídos;
- atrasado: projeto não finalizado cuja data planejada final é anterior à data atual.

Não calcular ROI agregado como média simples dos ROIs individuais, pois isso distorce projetos de tamanhos diferentes. Quando investimento total for zero, retornar `null` e um indicador de métrica não aplicável.

## Fonte e consistência

- resultados vêm da coleção de projetos;
- filtros devem ser aplicados antes das agregações;
- excluir arquivados/cancelados por padrão e permitir inclusão explícita se houver necessidade;
- definir se projetos em andamento entram em retorno/ROI; recomendação: incluir valores registrados e informar contagens por status;
- `generatedAt` indica o instante do cálculo;
- para o volume esperado, usar aggregation pipeline sob demanda; cache só depois de medir necessidade.

## Estrutura sugerida

- `DashboardController`;
- `DashboardService`;
- `DashboardAggregationRepository` com `MongoTemplate`;
- DTOs de resumo, agrupamento e série;
- `ProjectMetricsCalculator` compartilhado com projetos para evitar fórmulas divergentes.

## Critérios de aceite

- apenas líder acessa;
- resumo geral bate com os projetos persistidos;
- filtros por estratégia, projeto e período são combináveis;
- valores monetários mantêm precisão;
- retorno é adequado a cards, tabelas e gráficos;
- coleção vazia retorna zeros/listas vazias e `null` onde a divisão é inaplicável, sem erro 500.

## Testes esperados

- dataset conhecido com cálculo manual de cada métrica;
- investimento total zero;
- métricas ausentes;
- projetos cancelados, atrasados e concluídos;
- filtros e intervalos inclusivos/exclusivos documentados;
- autorização;
- teste de integração do pipeline MongoDB, não apenas mock de repository.
