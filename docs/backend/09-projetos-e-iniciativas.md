# Projetos e iniciativas

## Objetivo

Permitir que gestores planejem e acompanhem projetos vinculados à estratégia vigente, registrando progresso, investimento e resultados que alimentam o dashboard. Líderes possuem acesso de consulta.

## Modelo

Campos principais:

- identidade: `id`, `name`, `description`;
- vínculos: `strategyId`, `sourceIdeaId` opcional, `managerUserId`;
- execução: `stage`, `status`, `progressPercent`;
- prazo: datas planejadas e reais;
- financeiro: `investment`, `financialReturn`;
- resultado: `productivityGainPercent`, `resultsSummary`;
- controle: `version`, `active`, auditoria.

Valores derivados não precisam ser persistidos: `profit = financialReturn - investment`; `roiPercent = profit / investment × 100`. Para investimento zero, ROI deve ser `null`/não aplicável, nunca infinito.

## Regras

- somente `GESTOR` cria e altera;
- gestor autenticado torna-se responsável por padrão; se a equipe permitir atribuição a outro gestor, validar o usuário e role;
- na criação, estratégia deve estar ativa;
- se houver `sourceIdeaId`, a ideia deve estar `APROVADA`, pertencer à mesma estratégia e ainda não possuir projeto;
- `progressPercent` fica entre 0 e 100;
- projeto `CONCLUIDO` exige data real de fim e dados de resultado mínimos;
- data final não antecede inicial;
- investimento/retorno não podem ser negativos;
- projeto referenciado em relatórios é arquivado/cancelado, não apagado fisicamente.

## Endpoints

| Método e rota | GESTOR | LIDER |
|---|:---:|:---:|
| `POST /api/v1/projects` | criar | — |
| `GET /api/v1/projects` | consultar | consultar |
| `GET /api/v1/projects/{id}` | consultar | consultar |
| `PUT /api/v1/projects/{id}` | atualizar | — |
| `PATCH /api/v1/projects/{id}/progress` | atualizar | — |
| `POST /api/v1/projects/{id}/results` | registrar/atualizar resultado | — |
| `DELETE /api/v1/projects/{id}` | arquivar/cancelar | — |

Filtros: `strategyId`, `sourceIdeaId`, `managerUserId`, `stage`, `status`, período, atrasados, paginação e ordenação.

## DTOs

- `CreateProjectRequest(name, description, strategyId, sourceIdeaId, stage, plannedStartDate, plannedEndDate, investment)`;
- `UpdateProjectRequest(..., version)`;
- `UpdateProgressRequest(stage, status, progressPercent, note, version)`;
- `RegisterProjectResultRequest(actualEndDate, financialReturn, productivityGainPercent, resultsSummary, version)`;
- `ProjectResponse` inclui lucro/ROI calculados e resumos de estratégia/ideia.

## Transições sugeridas

- `PLANEJADO → EM_ANDAMENTO | CANCELADO`;
- `EM_ANDAMENTO → PAUSADO | CONCLUIDO | CANCELADO`;
- `PAUSADO → EM_ANDAMENTO | CANCELADO`;
- estados finais: `CONCLUIDO`, `CANCELADO`.

Separar `stage` de `status`: etapa indica onde o trabalho está; status indica a condição operacional.

## Estrutura sugerida

- `ProjectDocument`, `ProjectStatus`, `ProjectStage`;
- `ProjectRepository` e consultas customizadas;
- `ProjectService`, `ProjectTransitionPolicy`, `ProjectMetricsCalculator`;
- `ProjectController`, DTOs, mapper;
- subdocumento/evento `ProgressEntry` se for necessário mostrar a evolução, contendo data, nota, etapa, status, percentual e ator.

## Critérios de aceite

- projeto nasce vinculado a estratégia vigente;
- somente ideia aprovada compatível pode originar projeto;
- líder não cria nem altera;
- progresso e resultado ficam auditáveis;
- lucro e ROI são calculados de forma consistente;
- conclusão sem resultado mínimo é rejeitada;
- filtros suportam a consulta executiva exigida.

## Testes esperados

- autorização por endpoint;
- vínculos inexistentes, inativos ou incompatíveis;
- transições de status e limites de progresso;
- cálculos com investimento positivo, zero e casas decimais;
- prazo válido/inválido e detecção de atraso;
- concorrência por versão;
- vínculo único entre ideia e projeto.
