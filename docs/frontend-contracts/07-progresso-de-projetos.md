# 7. Progresso de projetos

## Endpoint

`PATCH /api/v1/projects/{id}/progress` — somente GESTOR.

```json
{
  "stage": "EXECUCAO",
  "status": "EM_ANDAMENTO",
  "progressPercent": 35,
  "note": "Primeiro piloto concluído.",
  "version": 1
}
```

Validações:

- `stage` e `status` obrigatórios;
- `progressPercent`: inteiro de 0 a 100;
- `note`: opcional, máximo 2000 caracteres;
- `version`: obrigatória, zero ou positiva.

## Transições de status

```text
PLANEJADO    → EM_ANDAMENTO | CANCELADO
EM_ANDAMENTO → PAUSADO | CANCELADO
PAUSADO      → EM_ANDAMENTO | CANCELADO
CONCLUIDO    → nenhuma
CANCELADO    → nenhuma
```

Enviar o mesmo status atual é aceito, permitindo atualizar etapa, percentual e observação. `CONCLUIDO` nunca é aceito nesta rota: a conclusão deve usar `/results`.

Ao fazer `PLANEJADO → EM_ANDAMENTO`, o backend define `actualStartDate` com a data corrente se ainda estiver vazia.

## Histórico de progresso

Cada atualização acrescenta um item em `progressHistory`, retornado dentro de `ProjectResponse`:

```json
{
  "occurredAt": "2026-09-19T15:20:00Z",
  "actorUserId": "manager-id",
  "stage": "EXECUCAO",
  "status": "EM_ANDAMENTO",
  "progressPercent": 35,
  "note": "Primeiro piloto concluído."
}
```

Não há endpoint separado nem paginação para o histórico. O frontend o obtém em `GET /projects/{id}` ou nas respostas de mutação.

## Integração recomendada

- sempre enviar a `version` do último `ProjectResponse`;
- substituir o estado local pela resposta completa;
- usar `progressHistory` como timeline em ordem de inserção;
- não oferecer `CONCLUIDO` no seletor desta tela;
- ao receber `409 STALE_VERSION`, recarregar o projeto.

## Erros

- `409 INVALID_STATE_TRANSITION`;
- `409 RESULTS_REQUIRED` quando `CONCLUIDO` é enviado;
- `409 INVALID_PROGRESS` para valor fora da faixa que ultrapasse a validação HTTP;
- `409 STALE_VERSION`;
- `404 RESOURCE_NOT_FOUND`.
