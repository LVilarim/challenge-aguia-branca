# Estratégias e orientações da empresa

## Objetivo

Permitir que a liderança gerencie orientações estratégicas e que todos os perfis consultem a orientação vigente e o catálogo histórico.

## Modelo e regras

Campos: `id`, `category`, `campaign`, `description`, `status`, `validFrom`, `validUntil`, `version`, `createdBy`, `updatedBy`, `createdAt`, `updatedAt`.

- categoria: 2–80 caracteres;
- campanha: 2–120 caracteres;
- descrição: obrigatória, 10–4000 caracteres;
- `validUntil`, se presente, não pode anteceder `validFrom`;
- somente líder cria, altera, ativa, encerra ou arquiva;
- no máximo uma estratégia está `ATIVA`;
- uma estratégia referenciada não é removida fisicamente;
- alterações geram evento histórico.

## Endpoints sugeridos

| Método e rota | Permissão | Resultado |
|---|---|---|
| `POST /api/v1/strategies` | LIDER | cria rascunho; `201` + `Location` |
| `GET /api/v1/strategies/current` | todos | estratégia ativa; `200` ou `404` |
| `GET /api/v1/strategies` | todos | lista paginada e filtrável |
| `GET /api/v1/strategies/{id}` | todos | detalhe |
| `PUT /api/v1/strategies/{id}` | LIDER | substitui campos editáveis |
| `PATCH /api/v1/strategies/{id}/activation` | LIDER | ativa/encerra |
| `DELETE /api/v1/strategies/{id}` | LIDER | arquivamento lógico; `204` |

Filtros: `status`, `category`, `campaign`, `validFrom`, `validUntil`, `page`, `size`, `sort`. Limitar `size`, recomendação máxima 100.

## DTOs

- `CreateStrategyRequest(category, campaign, description, validFrom, validUntil)`;
- `UpdateStrategyRequest(..., version)`;
- `ChangeStrategyActivationRequest(action, effectiveAt, version)`;
- `StrategyResponse` com auditoria, status e versão;
- `StrategySummaryResponse` para listas.

Não aceitar `createdBy`, `status` arbitrário ou datas de auditoria do cliente.

## Fluxo de ativação

1. validar role e versão;
2. validar estratégia alvo e intervalo de vigência;
3. encerrar a estratégia ativa anterior com `validUntil = effectiveAt`;
4. ativar a nova com `validFrom = effectiveAt`;
5. registrar histórico de ambas;
6. concluir atomicamente ou não alterar nenhuma.

## Estrutura sugerida

- `StrategyDocument`, `StrategyStatus`;
- `StrategyRepository` e consultas customizadas para vigente/filtros;
- `StrategyService`, `StrategyActivationService`;
- `StrategyController`, DTOs e `StrategyMapper`;
- `StrategyPolicy` para transições.

## Critérios de aceite

- operador e gestor não conseguem mutar estratégias;
- todos os perfis autenticados consultam a vigente;
- duas estratégias não ficam ativas simultaneamente;
- ativação/edição gera histórico consistente;
- estratégia referenciada é arquivada, não apagada;
- update com versão obsoleta retorna `409`.

## Testes esperados

- validações de campo e intervalo;
- cada transição permitida/proibida;
- autorização por role em cada verbo HTTP;
- concorrência de duas ativações;
- filtros/paginação;
- criação do histórico junto à alteração.
