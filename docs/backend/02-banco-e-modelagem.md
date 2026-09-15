# Banco de dados e modelagem MongoDB

## Objetivo

Definir documentos, referências, índices e regras de persistência necessárias aos casos de uso, mantendo histórico e permitindo agregações do dashboard.

## Coleções

### `users`

| Campo | Tipo | Regra |
|---|---|---|
| `_id` | ObjectId | gerado pelo MongoDB |
| `name` | string | obrigatório, 2–120 caracteres |
| `email` | string | obrigatório, normalizado em minúsculas, único |
| `passwordHash` | string | obrigatório, nunca exposto |
| `role` | enum | `OPERADOR`, `GESTOR`, `LIDER` |
| `active` | boolean | padrão `true` |
| `createdAt`, `updatedAt` | instant | auditoria |

Índices: único em `email`; índice em `role, active`.

### `strategies`

Campos mínimos: `_id`, `category`, `campaign`, `description`, `status`, `validFrom`, `validUntil`, `version`, auditoria e usuário responsável. Status: `RASCUNHO`, `ATIVA`, `ENCERRADA`, `ARQUIVADA`.

Índices: `status`; `category`; `validFrom`; índice parcial/controle de aplicação para vigência única. Não depender apenas de consulta seguida de insert, pois há condição de corrida.

### `strategy_history`

Snapshot imutável contendo `strategyId`, `strategyVersion`, `eventType`, `occurredAt`, `actorUserId` e `snapshot`. O snapshot preserva pelo menos id, data, categoria e campanha, além dos demais campos relevantes.

Índices: `strategyId, strategyVersion` único; `occurredAt`; `eventType`.

### `ideas`

Campos: `_id`, `title`, `description`, `problem`, `expectedBenefit`, `strategyId`, `authorUserId`, `status`, `priority`, `managerComment`, datas de submissão/decisão, auditoria e `version` para controle otimista.

Status: `RASCUNHO`, `SUBMETIDA`, `EM_AVALIACAO`, `APROVADA`, `REJEITADA`, `ARQUIVADA`. Prioridade: `BAIXA`, `MEDIA`, `ALTA`, `CRITICA` ou ausente antes da avaliação.

Índices: `authorUserId, createdAt`; `strategyId`; `status, priority`; busca por título pode começar com regex controlada/índice adequado, sem adicionar Elasticsearch.

### `projects`

Campos: `_id`, `name`, `description`, `strategyId`, `sourceIdeaId` opcional, `managerUserId`, `stage`, `status`, `plannedStartDate`, `plannedEndDate`, datas reais, `investment`, `financialReturn`, `productivityGainPercent`, `progressPercent`, `resultsSummary`, auditoria e `version`.

Status sugeridos: `PLANEJADO`, `EM_ANDAMENTO`, `PAUSADO`, `CONCLUIDO`, `CANCELADO`. Etapas podem ser enum estável (`DESCOBERTA`, `PLANEJAMENTO`, `EXECUCAO`, `VALIDACAO`, `ENCERRAMENTO`) ou catálogo acordado com o frontend.

Índices: `strategyId, status`; `managerUserId`; `sourceIdeaId`; `plannedEndDate`.

## Referências versus documentos embutidos

- guardar IDs para relações entre agregados (`strategyId`, `authorUserId`, `sourceIdeaId`);
- não usar `@DBRef`, evitando carregamentos implícitos e comportamento difícil de controlar;
- incorporar snapshots somente quando imutabilidade histórica for intencional;
- validar existência e estado das referências na camada de aplicação.

## Dinheiro, percentuais e datas

- Java: `BigDecimal`; MongoDB: `Decimal128` por conversão explícita;
- definir escala monetária de 2 casas e arredondamento `HALF_UP` nas bordas;
- percentuais com escala acordada, recomendação `0..100`;
- instantes de auditoria em UTC (`Instant`);
- datas planejadas sem horário como `LocalDate`, serializadas `YYYY-MM-DD`.

## Concorrência

Usar `@Version` em ideias, estratégias e projetos. Uma atualização com versão antiga deve resultar em `409 Conflict`, impedindo que dois usuários sobrescrevam alterações silenciosamente.

## Migração e dados iniciais

MongoDB é flexível, mas mudanças de schema precisam ser versionadas. Recomenda-se Mongock ou scripts versionados quando surgirem dados existentes. Para a primeira execução, um initializer idempotente pode criar usuários de demonstração somente no perfil `dev`, com senhas vindas de variáveis de ambiente.

## Critérios de aceite

- índices únicos e de consulta são criados/validados;
- não há senha em coleções fora de `users`;
- uma exclusão não quebra referências históricas;
- cálculos financeiros preservam precisão decimal;
- update concorrente é detectado;
- fixtures e seeds não executam em produção por padrão.
