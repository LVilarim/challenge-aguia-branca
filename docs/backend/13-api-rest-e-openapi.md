# Convenções REST e documentação OpenAPI

## Objetivo

Oferecer ao frontend um contrato previsível, navegável e verificável, mantendo a documentação junto do código.

## Convenções

- base: `/api/v1`;
- recursos no plural e em inglês: `/strategies`, `/ideas`, `/projects`;
- JSON em `camelCase`;
- datas ISO 8601;
- `201 Created` e header `Location` em criações;
- `204 No Content` em exclusões sem corpo;
- paginação baseada em `page`, `size`, `sort`;
- filtros por query string;
- ações de domínio explícitas como `/submit`, `/approval` e `/results` quando não equivalem a CRUD simples.

## Paginação

Contrato recomendado:

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

Não expor diretamente `PageImpl`, pois seu JSON pode mudar entre versões do Spring. Criar `PageResponse<T>` estável.

## OpenAPI

Adicionar biblioteca compatível com Spring Boot 4/Spring MVC após conferir a matriz de compatibilidade vigente. Configurar:

- título, versão, descrição e contatos do projeto;
- servidor local e variáveis de ambiente sem fixar host de produção;
- esquema `bearerAuth` JWT;
- tags: Auth, Strategies, Strategy History, Ideas, Projects, Dashboard;
- exemplos de requests/responses;
- respostas `400`, `401`, `403`, `404`, `409` e `500` reutilizáveis;
- DTO Problem Details e paginação;
- enums, limites e formatos.

Disponibilizar JSON em rota padrão do componente escolhido e UI Swagger apenas conforme política do ambiente. Em produção, pode-se manter o JSON e restringir/desabilitar a UI.

## Versionamento e compatibilidade

- mudanças aditivas e campos opcionais não exigem nova versão;
- renomear/remover campo ou mudar semântica exige `/api/v2` ou janela de depreciação;
- não reutilizar enum com significado diferente;
- registrar decisões relevantes em ADR simples dentro da documentação, se surgirem.

## Idempotência

- `PUT` deve ser idempotente;
- ações de decisão devem impedir efeitos duplicados via estado/versão;
- criação pode futuramente aceitar `Idempotency-Key`, mas não é obrigatória na Sprint;
- `DELETE` lógico repetido pode responder `204` enquanto o recurso já estiver arquivado, se essa política for adotada globalmente.

## Critérios de aceite

- todos os endpoints entregues aparecem no OpenAPI;
- exemplos e schemas correspondem ao comportamento real;
- endpoint protegido exibe bearer auth;
- respostas de erro estão documentadas;
- o frontend consegue gerar/usar cliente sem depender de classes internas;
- documentação é validada em teste ou build para evitar especificação quebrada.
