# Histórico de estratégias

## Objetivo

Preservar uma trilha imutável das mudanças de estratégia, atendendo ao requisito de histórico e permitindo auditoria sem reconstruir o passado a partir do estado atual.

## Eventos

- `CREATED`;
- `UPDATED`;
- `ACTIVATED`;
- `CLOSED`;
- `ARCHIVED`.

Cada evento contém `id`, `strategyId`, `strategyVersion`, `eventType`, `occurredAt`, `actorUserId` e um snapshot. O snapshot contém no mínimo `id`, `date`/`occurredAt`, `category` e `campaign`, mais descrição, status e vigência.

## Endpoints

| Método e rota | Permissão | Observação |
|---|---|---|
| `GET /api/v1/strategies/{id}/history` | todos autenticados | paginação por data decrescente |
| `GET /api/v1/strategy-history/{historyId}` | todos autenticados | detalhe de uma versão |

Filtros opcionais: `eventType`, intervalo `from/to`, paginação. Não disponibilizar endpoints de criação, edição ou exclusão do histórico.

## DTO

`StrategyHistoryResponse(id, strategyId, version, eventType, occurredAt, actor, snapshot)`.

O ator pode ser um resumo (`id`, `name`) resolvido de modo eficiente; se o usuário for desativado, o histórico continua legível.

## Consistência

- gerar o snapshot no mesmo caso de uso que modifica a estratégia;
- não depender apenas de listener assíncrono na Sprint 2, pois uma falha poderia perder auditoria;
- índice único em `(strategyId, strategyVersion)` impede duplicação;
- histórico nunca deve ser alterado por um endpoint;
- correção excepcional de dados exige procedimento administrativo auditado, fora da API pública.

## Estrutura sugerida

- `StrategyHistoryDocument`, `StrategyEventType`;
- `StrategyHistoryRepository`;
- `StrategyHistoryService` chamado pelo serviço de estratégia;
- `StrategyHistoryController` somente leitura.

## Critérios de aceite

- cada mudança persistida possui exatamente um evento correspondente;
- snapshot antigo não muda após novas edições;
- resultado inclui id, data, categoria e campanha;
- ordenação padrão é do evento mais recente ao mais antigo;
- nenhum perfil consegue escrever diretamente no histórico.

## Testes esperados

- criação de snapshot para todos os eventos;
- imutabilidade após edição posterior;
- paginação e filtros;
- tentativa de duplicar versão falha;
- falha ao gravar histórico impede a confirmação de uma mudança que deva ser atômica.
