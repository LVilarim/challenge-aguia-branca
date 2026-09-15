# Arquitetura e organização do projeto

## Objetivo

Manter um monólito modular simples, testável e apropriado ao tamanho da Sprint 2. A separação é por funcionalidade, evitando um único pacote global de controllers, services e repositories.

## Estilo arquitetural

Fluxo recomendado:

`HTTP Controller → Application Service → Domain/Policy → Repository → MongoDB`

- **Controller:** traduz HTTP para DTO, aciona validação e devolve status/headers.
- **Application Service:** coordena caso de uso, transação lógica, autorização por propriedade e persistência.
- **Domain/Policy:** concentra transições e cálculos que não dependem de infraestrutura.
- **Repository:** abstrai consultas MongoDB.
- **Mapper:** converte DTO/documento sem expor persistência na API.

## Estrutura de pacotes proposta

```text
com.challenge.aguiabranca.api
├── ApiApplication.java
├── common
│   ├── config
│   ├── error
│   ├── pagination
│   └── validation
├── security
│   ├── config
│   ├── jwt
│   └── principal
├── auth
│   ├── web
│   ├── application
│   └── dto
├── user
│   ├── domain
│   ├── repository
│   ├── application
│   └── web
├── strategy
│   ├── domain
│   ├── repository
│   ├── application
│   └── web
├── idea
│   ├── domain
│   ├── repository
│   ├── application
│   └── web
├── project
│   ├── domain
│   ├── repository
│   ├── application
│   └── web
└── dashboard
    ├── application
    ├── repository
    └── web
```

Testes devem espelhar a estrutura em `src/test/java`.

## Classes transversais esperadas

- `SecurityConfig`, `JwtTokenService`, `JwtAuthenticationFilter` e `AuthenticatedUser`;
- `GlobalExceptionHandler`, `ApiProblem`, exceções `NotFound`, `Conflict`, `Forbidden` e `BusinessRule`;
- `ClockConfig` para injetar `Clock` e tornar datas testáveis;
- `MongoAuditingConfig` e uma base auditável, quando útil;
- configuração OpenAPI e CORS;
- utilitários de paginação e normalização de entrada.

## Regras de dependência

- `web` pode depender de `application` e DTOs;
- `application` pode depender de domínio e interfaces de repositório;
- domínio não deve depender de controller, servlet ou detalhes de JWT;
- um módulo só acessa outro por serviço de aplicação ou contrato explícito;
- controllers não chamam repositories diretamente;
- documentos MongoDB não são retornados diretamente.

## Convenções de código

- nomes em inglês no código e português apenas em mensagens voltadas ao usuário/documentação;
- construtor para injeção de dependência; sem field injection;
- `record` para DTOs imutáveis quando adequado;
- Lombok com moderação, evitando `@Data` em documentos com relações e dados sensíveis;
- métodos curtos com nomes que expressem o caso de uso;
- evitar interfaces para todo service sem necessidade real;
- não capturar `Exception` genérica para esconder falhas;
- usar `Clock` em vez de `Instant.now()` dentro das regras.

## Concorrência e consistência

MongoDB não fornece automaticamente as mesmas garantias de um banco relacional. Operações como ativar uma estratégia e encerrar outra devem usar uma destas abordagens:

1. transação MongoDB em replica set; ou
2. atualização condicional/idempotente mais índice e tratamento de conflito.

Para a Sprint 2, priorizar operações atômicas de documento e consultas condicionais. Se uma regra envolver múltiplos documentos críticos, habilitar replica set no ambiente e testar a transação.

## Critérios de aceite

- não há dependência circular entre módulos;
- regras de transição são testáveis sem contexto web;
- nenhum controller contém consulta MongoDB ou cálculo de dashboard;
- pacote raiz permanece sob `com.challenge.aguiabranca.api` para component scan correto;
- arquitetura e exceções seguem um padrão único em todas as funcionalidades.
