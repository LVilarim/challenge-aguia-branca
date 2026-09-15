# Ideias de inovação e problemas dos operadores

## Objetivo

Permitir que cada operador registre e acompanhe suas próprias ideias/problemas, sempre associados à estratégia vigente, sem visualizar ou alterar registros de outros operadores.

## Modelo

Campos: `id`, `title`, `description`, `problem`, `expectedBenefit`, `strategyId`, `authorUserId`, `status`, `priority`, `managerComment`, `submittedAt`, `decidedAt`, `decidedBy`, `version`, auditoria.

## Regras de negócio

- somente `OPERADOR` cria ideia;
- o autor é obtido do JWT, nunca do payload;
- na criação, `strategyId` deve apontar para a estratégia atualmente `ATIVA`;
- se o cliente omitir `strategyId`, o backend pode associar a vigente; recomenda-se exigir/retornar explicitamente para o usuário saber o vínculo;
- operador lista e consulta apenas as próprias ideias;
- operador edita/exclui somente em `RASCUNHO` ou `SUBMETIDA`;
- “exclusão” após submissão deve arquivar; rascunho sem dependências pode ser removido fisicamente se a equipe optar por isso;
- prioridade e decisão são campos exclusivos do gestor;
- o vínculo estratégico não muda após criação, mesmo quando outra estratégia passa a vigorar.

## Endpoints do operador

| Método e rota | Resultado |
|---|---|
| `POST /api/v1/ideas` | cria ideia; `201` + `Location` |
| `GET /api/v1/ideas/mine` | lista as próprias ideias, paginada |
| `GET /api/v1/ideas/{id}` | retorna a própria ideia |
| `PUT /api/v1/ideas/{id}` | atualiza campos autorais e versão |
| `POST /api/v1/ideas/{id}/submit` | muda `RASCUNHO → SUBMETIDA` |
| `DELETE /api/v1/ideas/{id}` | remove/arquiva conforme estado; `204` |

Para evitar exposição, `GET/PUT/DELETE` de ideia alheia deve retornar `404`, seguindo uma política consistente.

## Endpoints de consulta do gestor

- `GET /api/v1/ideas`: lista todas, com filtros por `status`, `priority`, `strategyId`, autor, período e texto;
- `GET /api/v1/ideas/{id}`: detalhe para avaliação.

Resolver conflito de rota/permissão no mesmo controller ou separar controllers com o mesmo contrato público. Não duplicar regras de busca.

## DTOs e validações

`CreateIdeaRequest`: `title` (5–150), `description` (20–5000), `problem` (10–3000), `expectedBenefit` (opcional, máximo 2000), `strategyId` obrigatório.

`UpdateIdeaRequest`: mesmos campos editáveis e `version` obrigatório.

`IdeaResponse`: inclui estratégia resumida, autor resumido quando gestor, status, prioridade, comentários de decisão e auditoria. Para operador, não expor dados desnecessários de outros usuários.

## Estrutura sugerida

- `IdeaDocument`, `IdeaStatus`, `IdeaPriority`;
- `IdeaRepository` e repositório customizado para filtros;
- `IdeaService`, `IdeaOwnershipPolicy`, `IdeaTransitionPolicy`;
- `IdeaController`, `IdeaManagementController` se a separação melhorar a clareza;
- DTOs e `IdeaMapper`.

## Critérios de aceite

- operador cria ideia somente para estratégia vigente;
- autor é derivado do token;
- operador não descobre nem altera ideia alheia;
- campos de gestão não são aceitos em criação/edição do operador;
- ideia mantém referência à estratégia original;
- listagens possuem paginação e ordenação.

## Testes esperados

- criação com estratégia ativa, inativa e inexistente;
- tentativa de forjar autor/status/prioridade;
- propriedade em leitura, update e delete;
- limites dos campos;
- edição em cada status;
- filtros e paginação para operador e gestor.
