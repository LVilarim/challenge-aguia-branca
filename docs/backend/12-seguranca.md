# Segurança da aplicação

## Objetivo

Aplicar defesa em profundidade para autenticação, autorização, dados, configuração e operação, além do mecanismo básico de JWT.

## Configuração HTTP

- sessão stateless;
- CSRF desabilitado somente porque a autenticação usa bearer token fora de cookie;
- `/api/v1/auth/login`, documentação OpenAPI e health restrito podem ser públicos conforme ambiente;
- todo o restante exige autenticação por padrão;
- regras por rota mais `@PreAuthorize` nos casos de uso sensíveis;
- `401` e `403` em Problem Details;
- headers de segurança adequados e HTTPS obrigatório em produção.

## Autorização

Há dois níveis:

1. **role:** restringe a categoria da operação;
2. **escopo do recurso:** operador só acessa suas ideias.

Nunca aceitar `userId`, `authorId`, `createdBy` ou `managerId` do cliente quando o valor deve vir do principal autenticado. Ao buscar ideia de operador, preferir consulta `findByIdAndAuthorUserId` para evitar falhas de checagem posterior.

## CORS

Configurar origens por variável `APP_CORS_ALLOWED_ORIGINS`. Permitir apenas métodos e headers necessários. Não combinar origem `*` com credenciais. Ambientes dev e prod devem ter listas distintas.

## Segredos

- JWT secret/chave, senha MongoDB e credenciais de seed ficam em variáveis/secret manager;
- fornecer `.env.example` sem valores reais;
- falhar ao iniciar produção quando segredo obrigatório está ausente ou fraco;
- não imprimir environment completo;
- prever rotação de chave JWT por identificador `kid` se usar chave assimétrica.

## Proteções adicionais

- limite de tamanho de request e limites de campos;
- paginação máxima;
- rate limiting de login recomendado;
- comparação de senha por encoder seguro;
- prevenção de mass assignment por DTOs específicos;
- queries construídas por parâmetros tipados/allowlists;
- Actuator expondo apenas `health`/`info` conforme ambiente;
- logs sanitizados;
- dependências verificadas e atualizadas conscientemente.

## Threat checklist

- token roubado: expiração curta, HTTPS e armazenamento seguro no frontend;
- enumeração de usuários: mensagem uniforme no login;
- IDOR: consulta por ID + proprietário;
- elevação de privilégio: role nunca mutável por endpoint público;
- injection NoSQL: não aceitar operadores MongoDB ou JSON livre como filtro;
- brute force: rate limit/atraso progressivo e monitoramento;
- dados sensíveis: resposta mínima e ausência de senha/token em logs.

## Critérios de aceite

- matriz de acesso coberta por testes negativos;
- nenhuma rota nova fica pública por omissão;
- segredos não estão no Git;
- CORS aceita somente origens configuradas;
- endpoints Actuator sensíveis não estão expostos;
- análise de dependências não apresenta vulnerabilidade crítica conhecida sem decisão registrada.
