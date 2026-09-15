# Execução, build e deploy

## Objetivo

Definir um caminho reproduzível desde a máquina do desenvolvedor até a execução de uma imagem pronta.

## Pré-requisitos locais

- JDK 21;
- Docker Engine/Desktop com Compose;
- portas configuradas disponíveis;
- variáveis descritas em `.env.example` ou na sessão da IDE.

Usar sempre o Maven Wrapper do repositório, reduzindo diferenças de versão.

## Fluxo local esperado

1. copiar `.env.example` para arquivo local ignorado e preencher apenas credenciais de desenvolvimento;
2. iniciar MongoDB pelo Compose;
3. aguardar healthcheck;
4. executar a aplicação com perfil `dev`;
5. confirmar `/actuator/health`;
6. abrir OpenAPI/Swagger e autenticar com usuário seed local;
7. executar testes antes de enviar mudanças.

Os comandos concretos devem ser acrescentados ao README raiz durante a implementação, depois que nomes de variáveis, portas e plugin OpenAPI estiverem definidos.

## Pipeline de CI recomendado

Em pull request:

1. checkout;
2. configurar JDK 21 com cache Maven;
3. executar testes e verificação de estilo/qualidade;
4. validar OpenAPI;
5. construir JAR;
6. construir imagem Docker;
7. executar scan de dependências/imagem;
8. publicar artefato somente em branch/tag autorizada.

## Deploy

- imagem imutável identificada por tag/commit;
- segredos injetados pela plataforma;
- MongoDB persistente, autenticado e com backup;
- TLS terminado no proxy/plataforma;
- pelo menos endpoint de readiness/health;
- política de rollback para a imagem anterior;
- migrations de dados compatíveis com rollback ou executadas de forma planejada.

## Checklist pré-release

- suíte automatizada verde;
- JWT e CORS configurados para o ambiente;
- Swagger UI restrita conforme decisão;
- seed dev desabilitado;
- logs sem `DEBUG` indiscriminado;
- índices MongoDB presentes;
- backup/restore do banco entendido;
- smoke test de login, estratégia, ideia, projeto e dashboard;
- documentação e exemplos atualizados.

## Critérios de aceite

- pessoa nova executa o projeto seguindo apenas a documentação;
- build e testes não dependem da IDE;
- imagem inicia com configuração externa;
- falha de variável crítica encerra o processo com mensagem clara e sem expor segredo;
- deploy possui healthcheck e caminho de rollback.
