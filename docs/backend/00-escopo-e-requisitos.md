# Escopo e requisitos da Sprint 2

## Objetivo

Entregar uma API segura que permita registrar e acompanhar o ciclo entre orientação estratégica, ideia de inovação, aprovação, execução de projeto e mensuração de resultado.

## Atores

| Perfil | Responsabilidade principal |
|---|---|
| `OPERADOR` | Consultar estratégias vigentes e gerenciar as próprias ideias. |
| `GESTOR` | Consultar estratégias, avaliar ideias e gerenciar projetos/iniciativas. |
| `LIDER` | Gerenciar estratégias, consultar projetos e consumir dashboards executivos. |

## Requisitos funcionais

### RF-01 — Autenticação

- autenticar os três perfis por credenciais;
- emitir e validar JWT;
- armazenar senha somente como hash forte;
- impedir operações incompatíveis com o perfil.

### RF-02 — Estratégias

- líder cria, consulta, altera e desativa estratégias;
- gestor e operador apenas consultam;
- manter registro histórico com, no mínimo, identificador, data, categoria e campanha;
- permitir identificar inequivocamente a estratégia vigente.

### RF-03 — Ideias

- operador cria, consulta, edita e exclui suas ideias, respeitando o estado do fluxo;
- gestor consulta ideias, define prioridade e aprova ou rejeita;
- toda ideia deve estar vinculada à estratégia vigente no momento da criação.

### RF-04 — Projetos e iniciativas

- gestor cria, consulta, atualiza e desativa projetos;
- gestor registra progresso e resultados;
- líder consulta etapa, status, investimento, prazo e retorno financeiro;
- todo projeto deve estar vinculado a uma estratégia vigente na criação.

### RF-05 — Dashboard

- líder consulta resumo geral;
- líder filtra resultados por estratégia ou projeto;
- disponibilizar dados agregados para ROI, lucro, investimento, prazo e aumento de produtividade;
- retornar dados próprios para gráficos, sem gerar imagens no backend.

## Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF-01 | Java 21 e Spring Boot conforme o `pom.xml` do repositório. |
| RNF-02 | MongoDB como persistência. |
| RNF-03 | API REST/JSON documentada em OpenAPI. |
| RNF-04 | Execução reproduzível com Docker Compose e variáveis de ambiente. |
| RNF-05 | Validação, erros padronizados e códigos HTTP coerentes. |
| RNF-06 | Testes unitários, de integração, segurança e contrato HTTP. |
| RNF-07 | Logs sem dados sensíveis e endpoints de saúde controlados. |
| RNF-08 | CORS restrito às origens configuradas. |

## Premissas adotadas onde o enunciado é aberto

- Haverá no máximo uma estratégia `ATIVA` por vez. Ativar uma nova estratégia encerra a anterior de forma atômica.
- “Excluir” entidades históricas significa desativar/arquivar, preservando referências e relatórios.
- Operador só altera ou exclui ideia em `RASCUNHO` ou `SUBMETIDA`; após iniciar avaliação, apenas gestor muda seu estado.
- Aprovar ideia não cria projeto automaticamente. O gestor cria o projeto informando a ideia aprovada; isso mantém o fluxo explícito e auditável.
- Métricas de dashboard são calculadas a partir de projetos e resultados persistidos, não de valores enviados diretamente ao endpoint de relatório.

Essas premissas devem ser validadas com a equipe antes da codificação. Se mudarem, atualizar primeiro estes documentos e os contratos de API.

## Matriz de rastreabilidade

| Requisito | Documento | Evidência de aceite |
|---|---|---|
| RF-01 | `03`, `04`, `12` | login válido/inválido e testes de autorização |
| RF-02 | `05`, `06` | CRUD restrito, vigência única e histórico imutável |
| RF-03 | `07`, `08` | propriedade da ideia e transições válidas |
| RF-04 | `09` | CRUD, progresso, resultado e vínculo estratégico |
| RF-05 | `10` | agregações reproduzíveis e filtros |
| RNF-03/05 | `11`, `13` | OpenAPI e Problem Details |
| RNF-04 | `15`, `17` | subida limpa em ambiente novo |
| RNF-06 | `14` | suíte automatizada verde |

## Critérios de aceite globais

- usuário não autenticado recebe `401`; autenticado sem permissão recebe `403`;
- nenhuma senha, segredo ou token aparece em logs ou respostas;
- referências inexistentes geram `404`; conflitos de estado ou unicidade geram `409`;
- payload inválido gera `400` com erros por campo;
- endpoints de coleção suportam paginação e ordenação;
- OpenAPI representa DTOs, status, filtros e segurança;
- os requisitos de IA não são implementados nesta fase.
