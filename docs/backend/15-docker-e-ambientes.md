# Docker Compose e configuração de ambientes

## Objetivo

Garantir que todos desenvolvam contra versões previsíveis e que segredos/configurações não sejam acoplados ao código.

## Situação atual

O `compose.yaml` existente sobe `mongo:latest`, define credenciais diretamente e publica uma porta aleatória do host por usar apenas `27017`. Durante a implementação, recomenda-se:

- fixar uma versão suportada do MongoDB em vez de `latest`;
- usar `${MONGO_USERNAME}`, `${MONGO_PASSWORD}` e `${MONGO_DATABASE}`;
- mapear `27017:27017` apenas no ambiente de desenvolvimento, se necessário;
- adicionar volume nomeado;
- adicionar healthcheck;
- não versionar `.env` com segredos.

## Serviços previstos

### Desenvolvimento

- MongoDB no Compose;
- backend executado pela IDE ou `mvnw`, aproveitando depuração e reload.

### Validação/deploy local

- MongoDB;
- backend construído com Dockerfile multi-stage;
- rede interna e healthchecks;
- volume persistente para o banco.

## Variáveis

| Variável | Finalidade | Obrigatória em produção |
|---|---|:---:|
| `SPRING_PROFILES_ACTIVE` | perfil ativo | ✓ |
| `MONGODB_URI` | conexão completa | ✓ |
| `JWT_SECRET` ou caminhos/chaves | assinatura JWT | ✓ |
| `JWT_ISSUER` | emissor | ✓ |
| `JWT_EXPIRATION_SECONDS` | duração do token | ✓ |
| `APP_CORS_ALLOWED_ORIGINS` | origens do frontend | ✓ |
| `SERVER_PORT` | porta HTTP | opcional |
| `LOG_LEVEL_APP` | nível da aplicação | opcional |
| credenciais de seed dev | usuários locais | somente dev |

Preferir `SPRING_DATA_MONGODB_URI` se seguir o binding nativo; escolher um único nome e documentá-lo no README principal.

## Perfis Spring

- `application.yml`: padrões seguros e comuns;
- `application-dev.yml`: conveniências locais sem segredo;
- `application-test.yml`: isolamento de teste;
- `application-prod.yml`: comportamento produtivo e fail-fast.

Evitar duplicação extensa entre perfis. Variáveis de ambiente sobrescrevem valores sensíveis.

## Dockerfile esperado

- estágio de build com JDK 21 e Maven Wrapper;
- estágio final com JRE 21 pequeno e suportado;
- usuário não-root;
- somente JAR e certificados necessários;
- `HEALTHCHECK` ou healthcheck no Compose consultando Actuator;
- `.dockerignore` excluindo Git, IDE, target e segredos.

## Critérios de aceite

- `docker compose up` inicia MongoDB saudável com versão fixada;
- backend local conecta usando configuração documentada;
- ambiente completo sobe sem editar arquivos versionados;
- dados persistem em reinicialização normal;
- nenhum segredo real está no Compose/Git;
- imagem do backend executa como usuário não-root.
