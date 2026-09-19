# 12. Paginação, filtros e ordenação

## Contrato de página

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "sort": ["createdAt,desc"]
}
```

- `page` é baseado em zero;
- padrão: `page=0`, `size=20`;
- tamanho efetivo fica entre 1 e 100;
- página negativa é normalizada para 0;
- tamanho menor que 1 vira 1 e maior que 100 vira 100;
- `sort=campo,asc|desc`; direção diferente de `asc` resulta em `desc`;
- campo de ordenação não permitido cai silenciosamente no campo padrão do endpoint.

Como o backend normaliza valores em vez de falhar, use a paginação devolvida na resposta como fonte do estado real da UI.

## Rotas paginadas

| Rota | Filtros | Ordenação permitida |
|---|---|---|
| `/strategies` | `status`, `category`, `campaign`, `validFrom`, `validUntil` | `createdAt`, `updatedAt`, `category`, `campaign`, `validFrom`, `status` |
| `/strategies/{id}/history` | `eventType`, `from`, `to` | `occurredAt`, `eventType`, `strategyVersion` |
| `/ideas/mine` | nenhum filtro de domínio | `createdAt`, `updatedAt`, `title`, `status`, `priority`, `submittedAt`, `decidedAt` |
| `/ideas` | `status`, `priority`, `strategyId`, `authorUserId`, `from`, `to`, `text` | os mesmos campos de ideias |
| `/projects` | `strategyId`, `sourceIdeaId`, `managerUserId`, `stage`, `status`, `from`, `to`, `delayed` | `createdAt`, `updatedAt`, `name`, `status`, `stage`, `plannedEndDate`, `progressPercent`, `investment` |
| `/dashboard/by-project` | `strategyId`, `projectId`, `from`, `to` | `createdAt`, `name`, `status`, `progressPercent`, `investment`, `financialReturn`, `plannedEndDate` |

## Formatos de filtro

- estratégias, ideias e dashboard usam `Instant` nos campos `from`/`to`: `2026-09-01T00:00:00Z`;
- projetos usam `LocalDate`: `2026-09-01`;
- enums são case-sensitive;
- booleano `delayed`: `true` ou `false`;
- texto e IDs devem ser URL-encoded com `URLSearchParams`.

## Recomendações para componentes

- zerar `page` ao alterar filtros ou ordenação;
- não requisitar `size > 100`;
- preservar os filtros na URL do frontend quando a tela precisar ser compartilhável;
- cancelar requisições anteriores durante busca digitada;
- considerar `totalPages=0` em coleções vazias;
- não inferir “última página” apenas pelo tamanho de `content`; usar `totalPages`/`totalElements`.
