# Estratégia de testes

## Objetivo

Detectar regressões de regra, segurança, persistência e contrato HTTP sem tornar a suíte lenta ou frágil.

## Pirâmide

### Testes unitários

Rápidos, sem Spring, para:

- políticas de transição;
- cálculo de lucro, ROI, prazo e produtividade;
- mappers e normalizadores relevantes;
- serviços com dependências mockadas quando o teste representa um caso de uso isolado;
- emissão/validação de token quando possível sem contexto web.

### Slice tests

- MVC/controller: validação, serialização, status, Problem Details e segurança;
- MongoDB: queries, índices, filtros, optimistic locking e aggregation pipelines.

### Integração

Subir aplicação e MongoDB real/efêmero, preferencialmente com Testcontainers. Cobrir fluxos completos:

1. login;
2. líder cria e ativa estratégia;
3. operador cria/submete ideia;
4. gestor prioriza/aprova;
5. gestor cria projeto, atualiza e conclui;
6. líder consulta dashboard.

## Organização

```text
src/test/java/com/challenge/aguiabranca/api
├── unit/ ou pacotes espelhados
├── integration
├── support
│   ├── TestDataFactory.java
│   └── MongoIntegrationTest.java
└── security
```

Preferir builders/factories de teste a fixtures JSON duplicadas. Cada teste cria seus dados e limpa/usa banco isolado.

## Matriz mínima

Para cada endpoint:

- sucesso;
- request inválido;
- não autenticado;
- cada role não autorizada;
- recurso inexistente;
- conflito de estado/versão quando aplicável.

Para propriedade de ideias, testar explicitamente dois operadores. Para dashboard, usar dataset de valores conhecidos e comparar resultado decimal exato.

## Ferramentas

- JUnit 5;
- AssertJ;
- Mockito apenas em unidade;
- Spring Security Test;
- MockMvc ou cliente de teste compatível com a versão do Spring;
- Testcontainers MongoDB para persistência real.

Verificar compatibilidade das dependências com Spring Boot 4.1.1 antes de fixar versões.

## Cobertura e qualidade

Cobertura numérica não substitui cenários. Meta inicial recomendada: 80% de linhas no domínio/aplicação e 100% das regras críticas/transições. O build deve falhar por teste quebrado; flakiness não é aceitável.

## Critérios de aceite

- suíte roda por Maven Wrapper em máquina limpa;
- testes não dependem de ordem ou banco compartilhado;
- nenhum segredo real é necessário;
- autorização negativa é coberta;
- pipelines MongoDB são testados contra MongoDB;
- tempo/data usam `Clock` fixo;
- fluxo ponta a ponta da Sprint passa.
