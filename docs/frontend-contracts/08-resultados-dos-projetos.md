# 8. Resultados dos projetos

## Endpoint

`POST /api/v1/projects/{id}/results` — somente GESTOR.

```json
{
  "actualEndDate": "2026-12-18",
  "financialReturn": 210000.00,
  "productivityGainPercent": 12.50,
  "resultsSummary": "O projeto reduziu o tempo médio e o consumo nas rotas piloto.",
  "version": 5
}
```

Validações:

- data de conclusão obrigatória;
- retorno financeiro obrigatório e não negativo;
- produtividade opcional, entre 0 e 100;
- resumo obrigatório, 10–5000 caracteres;
- versão obrigatória.

Somente projetos `EM_ANDAMENTO` ou já `CONCLUIDO` aceitam resultados. Uma nova chamada para projeto concluído atualiza os resultados e gera nova entrada no histórico.

## Efeitos da conclusão

- `actualEndDate` recebe o valor informado;
- `status` passa a `CONCLUIDO`;
- `stage` passa a `ENCERRAMENTO`;
- `progressPercent` passa a `100`;
- histórico recebe nota `Resultados registrados`;
- `profit` e `roiPercent` são recalculados na resposta.

## Métricas

```text
profit = financialReturn - investment
roiPercent = (profit × 100) / investment
```

Valores são arredondados para duas casas com `HALF_UP`. Se o investimento for zero, `roiPercent` é `null` e, devido à configuração global, pode não aparecer no JSON.

Exemplo parcial de resposta:

```json
{
  "status": "CONCLUIDO",
  "stage": "ENCERRAMENTO",
  "progressPercent": 100,
  "actualEndDate": "2026-12-18",
  "investment": 150000.00,
  "financialReturn": 210000.00,
  "profit": 60000.00,
  "roiPercent": 40.00,
  "productivityGainPercent": 12.50,
  "resultsSummary": "O projeto reduziu o tempo médio e o consumo nas rotas piloto.",
  "version": 6
}
```

## Erros

- `409 INVALID_STATE_TRANSITION`;
- `409 INVALID_DATE_RANGE` se o fim anteceder `actualStartDate`;
- `409 NEGATIVE_VALUE`;
- `409 INVALID_PERCENTAGE`;
- `409 STALE_VERSION`;
- `400 VALIDATION_ERROR` para restrições do DTO.
