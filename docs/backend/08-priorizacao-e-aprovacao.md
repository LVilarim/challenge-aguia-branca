# Priorização e aprovação de ideias

## Objetivo

Formalizar o fluxo de avaliação executado pelo gestor, com transições válidas, justificativas e auditoria.

## Máquina de estados

```text
RASCUNHO → SUBMETIDA → EM_AVALIACAO → APROVADA
                         └──────────→ REJEITADA
RASCUNHO/SUBMETIDA → ARQUIVADA (pelo autor, conforme regra)
```

Não permitir saltar de `RASCUNHO` diretamente para aprovação. `APROVADA` e `REJEITADA` são estados finais nesta Sprint. Uma reabertura futura exigirá caso de uso e evento próprios.

## Regras

- somente `GESTOR` inicia avaliação, prioriza e decide;
- apenas ideia `SUBMETIDA` pode entrar em avaliação;
- prioridade é obrigatória antes da decisão;
- aprovação/rejeição exige comentário do gestor, recomendação mínima de 10 caracteres;
- decisão registra `decidedAt` e `decidedBy` a partir do servidor;
- uma ideia aprovada pode originar no máximo um projeto, salvo decisão futura em contrário;
- todas as mutações usam `version` para impedir decisão concorrente.

## Endpoints

| Método e rota | Corpo | Resultado |
|---|---|---|
| `POST /api/v1/ideas/{id}/evaluation` | `{ "version": 1 }` | inicia avaliação |
| `PATCH /api/v1/ideas/{id}/priority` | prioridade, comentário opcional, versão | atualiza prioridade |
| `POST /api/v1/ideas/{id}/approval` | prioridade, comentário, versão | aprova |
| `POST /api/v1/ideas/{id}/rejection` | comentário, versão | rejeita |

Sucesso pode retornar `200` com o recurso atualizado. Estado incompatível ou versão obsoleta retorna `409`; ideia inexistente retorna `404`.

## DTOs

- `StartEvaluationRequest(version)`;
- `PrioritizeIdeaRequest(priority, comment, version)`;
- `DecideIdeaRequest(priority?, comment, version)`;
- `IdeaDecisionResponse` ou o `IdeaResponse` completo.

## Auditoria

O documento da ideia mantém o estado atual, mas recomenda-se uma lista de eventos embutida pequena ou coleção `idea_history` se a equipe quiser rastreabilidade completa. O mínimo obrigatório nesta Sprint é registrar quem decidiu, quando, prioridade e justificativa. Nunca sobrescrever esses dados silenciosamente.

## Critérios de aceite

- somente gestor acessa os endpoints de avaliação;
- transições inválidas retornam `409` com código de erro estável;
- duas decisões concorrentes não são aceitas;
- aprovação contém prioridade, comentário, gestor e data;
- ideia em rascunho não aparece por padrão na fila de avaliação;
- gestor pode filtrar fila por status, prioridade e estratégia.

## Testes esperados

- tabela de todas as transições permitidas e proibidas;
- ausência de prioridade/comentário;
- autorização dos três perfis;
- optimistic locking;
- decisão idempotente: repetir a mesma requisição não deve criar múltiplos efeitos; pode retornar estado atual ou `409`, conforme contrato documentado.
