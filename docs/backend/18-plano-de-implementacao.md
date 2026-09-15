# Plano de implementação da Sprint 2

## Objetivo

Sequenciar o trabalho de modo que cada etapa produza uma base testada para a seguinte, reduzindo retrabalho e integrações tardias.

## Fase 0 — decisões e preparação

- validar as premissas de vigência única, exclusão lógica e estados;
- confirmar compatibilidade do Spring Boot 4.1.1 com JWT, OpenAPI e Testcontainers escolhidos;
- criar configuração por perfis e exemplo de variáveis;
- ajustar MongoDB/Compose com versão fixa e healthcheck;
- configurar padrão de erros, auditoria e `Clock`.

**Saída:** aplicação sobe, MongoDB está saudável, erro-base e teste-base funcionam.

## Fase 1 — identidade e segurança

- modelar usuário/role e índice de e-mail;
- criar seed idempotente de desenvolvimento;
- implementar password encoder, login, JWT e principal;
- aplicar SecurityConfig, CORS, `401` e `403` padronizados;
- criar testes da matriz de acesso.

**Saída:** os três perfis autenticam e endpoints protegidos distinguem autenticação/autorização.

## Fase 2 — estratégias e histórico

- implementar documento, repository, DTOs e CRUD;
- implementar ativação atômica/vigência única;
- registrar snapshots históricos;
- criar consultas pública-autenticadas e filtros;
- cobrir transições e concorrência.

**Saída:** líder gerencia, demais consultam, histórico é imutável.

## Fase 3 — ideias e avaliação

- implementar CRUD do próprio operador;
- aplicar vínculo obrigatório à estratégia vigente;
- implementar submissão, fila do gestor, prioridade e decisão;
- proteger propriedade e versão;
- testar fluxo com dois operadores e um gestor.

**Saída:** ideia percorre todo o ciclo até aprovação/rejeição com auditoria.

## Fase 4 — projetos e resultados

- implementar projeto, vínculos e consultas;
- validar ideia aprovada e estratégia vigente;
- implementar progresso, transições, resultados e cálculos;
- liberar leitura ao líder;
- testar precisão financeira, prazos e concorrência.

**Saída:** gestor acompanha projetos e líder consulta dados completos.

## Fase 5 — dashboard

- implementar aggregation pipelines;
- consolidar calculador de métricas;
- criar resumo, agrupamento por estratégia/projeto e séries;
- validar filtros e conjuntos vazios;
- executar testes com dataset conhecido.

**Saída:** líder recebe dados consistentes para cards, tabelas e gráficos.

## Fase 6 — contrato e endurecimento

- completar OpenAPI e exemplos;
- executar fluxo de integração ponta a ponta;
- revisar CORS, logs, Actuator, limites e vazamento de dados;
- construir imagem e validar Compose completo;
- atualizar README raiz e checklist de deploy.

**Saída:** Definition of Done global atendida.

## Sugestão de divisão em issues

1. Base arquitetural, configuração e erros;
2. Usuários e seed de desenvolvimento;
3. Login/JWT/Security;
4. Estratégias CRUD;
5. Vigência e histórico;
6. Ideias do operador;
7. Avaliação do gestor;
8. Projetos e progresso;
9. Resultados e métricas;
10. Dashboard;
11. OpenAPI;
12. Testes ponta a ponta;
13. Docker, CI e documentação final.

Cada issue deve referenciar o documento correspondente, incluir testes positivos/negativos e não misturar refactor amplo com nova funcionalidade.

## Gate de revisão por entrega

- requisito e permissão estão claros;
- DTO não expõe documento MongoDB;
- regra está no serviço/policy, não no controller;
- erros seguem Problem Details;
- índices/consultas são adequados;
- testes cobrem happy path, validação, role e conflito;
- OpenAPI foi atualizado;
- não há segredo, token ou senha em código/log.

## Dependências entre módulos

```text
Usuários/Auth
    ↓
Estratégias → Histórico
    ↓
Ideias → Avaliação
    ↓
Projetos → Resultados
    ↓
Dashboard
```

## Fora deste plano

Integrações de IA permanecerão fora da Sprint em execução. Quando retomadas, devem receber uma especificação separada sobre provedor, privacidade, custo, timeout, fallback, avaliação de qualidade e autorização; não devem ser inseridas informalmente no fluxo crítico.
