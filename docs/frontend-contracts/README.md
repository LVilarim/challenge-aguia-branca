# Contratos do backend para o frontend

Esta pasta descreve o contrato HTTP **efetivamente implementado na branch `main`**, analisada no commit `f1744d1`. Os documentos são voltados à integração do frontend e complementam o OpenAPI servido pela aplicação.

## Convenções globais

- Base local padrão: `http://localhost:8080`.
- Prefixo das rotas de negócio: `/api/v1`.
- Corpo e respostas: JSON; erros: `application/problem+json`.
- Autenticação: `Authorization: Bearer <accessToken>` em todas as rotas, exceto login, OpenAPI, Swagger UI e health check.
- Datas com horário: ISO 8601/UTC, por exemplo `2026-09-19T14:30:00Z`.
- Datas sem horário: `YYYY-MM-DD`.
- IDs: strings geradas pelo MongoDB.
- Campos `null` são omitidos do JSON pela configuração global do Jackson.
- Valores monetários e percentuais chegam como números JSON; no frontend, evitar cálculos com ponto flutuante quando precisão financeira importar.
- Enums devem ser enviados exatamente como documentados, em maiúsculas e sem acentos.

## Documentos por módulo

1. [Autenticação e usuários](01-autenticacao-e-usuarios.md)
2. [Estratégias](02-estrategias.md)
3. [Histórico de estratégias](03-historico-de-estrategias.md)
4. [Ideias de inovação](04-ideias-de-inovacao.md)
5. [Avaliação de ideias](05-avaliacao-de-ideias.md)
6. [Projetos](06-projetos.md)
7. [Progresso de projetos](07-progresso-de-projetos.md)
8. [Resultados dos projetos](08-resultados-dos-projetos.md)
9. [Dashboard executivo](09-dashboard-executivo.md)
10. [Segurança e autorização](10-seguranca-e-autorizacao.md)
11. [Tratamento de erros](11-tratamento-de-erros.md)
12. [Paginação, filtros e ordenação](12-paginacao-filtros-e-ordenacao.md)
13. [Observabilidade](13-observabilidade.md)
14. [Documentação da API](14-documentacao-da-api.md)
15. [Infraestrutura e ambientes](15-infraestrutura-e-ambientes.md)

## Matriz resumida de acesso

| Recurso | OPERADOR | GESTOR | LIDER |
|---|---|---|---|
| Login e `/auth/me` | sim | sim | sim |
| Consultar estratégias/histórico | sim | sim | sim |
| Gerenciar estratégias | não | não | sim |
| Criar e gerenciar próprias ideias | sim | não | não |
| Listar e avaliar ideias | não | sim | não |
| Consultar projetos | não | sim | sim |
| Gerenciar projetos, progresso e resultados | não | sim | não |
| Dashboard | não | não | sim |

Os papéis não têm herança implícita. Uma tela deve ser habilitada pela role exata, mas o backend continua sendo a autoridade final.

## Concorrência otimista

Estratégias, ideias e projetos retornam `version`. O frontend deve guardar o valor recebido e devolvê-lo na próxima mutação. Depois de qualquer mutação bem-sucedida, substitua o objeto local pela resposta, que contém a nova versão. `409 STALE_VERSION` exige recarregar o recurso antes de tentar novamente.

Exceções atuais:

- `DELETE /strategies/{id}` não recebe `version` na implementação atual;
- `DELETE /ideas/{id}` e `DELETE /projects/{id}` recebem `version` como query parameter.

## Atenções encontradas na análise da `main`

- Regras de negócio usam `409 Conflict`, inclusive datas e transições inválidas.
- `DELETE /strategies/{id}` arquiva sem controle de versão explícito no contrato HTTP.
- Em projetos, `delayed=false` não filtra projetos não atrasados; somente `delayed=true` adiciona filtro.
- A rejeição reutiliza um DTO que aceita `priority`, mas o fluxo ignora esse campo e exige prioridade previamente registrada.
- A consulta de histórico por `strategyId` não confirma a existência da estratégia; pode retornar página vazia.
- Em produção, a Swagger UI está desabilitada, mas o OpenAPI JSON continua habilitado pela configuração atual.

Esses pontos documentam o comportamento existente; não são instruções para o frontend contorná-los silenciosamente. Caso o backend seja corrigido, estes contratos e o OpenAPI devem ser atualizados juntos.

## Headers úteis

```http
Authorization: Bearer <token>
Content-Type: application/json
X-Correlation-Id: identificador-opcional
```

O backend devolve `X-Correlation-Id` e o mesmo valor em `traceId` nas respostas de erro. O header enviado deve usar apenas letras, números, `.`, `_` ou `-`, com no máximo 64 caracteres.
