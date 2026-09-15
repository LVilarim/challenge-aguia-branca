# Autenticação e JWT

## Objetivo

Autenticar usuários ativos por e-mail e senha e emitir um JWT curto, assinado e verificável, usado nas requisições subsequentes.

## Endpoint

### `POST /api/v1/auth/login`

Público. Recebe:

```json
{
  "email": "operador@exemplo.com",
  "password": "senha-do-usuario"
}
```

Resposta `200 OK`:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "68c...",
    "name": "Nome",
    "email": "operador@exemplo.com",
    "role": "OPERADOR"
  }
}
```

Erros: `400` para payload malformado; `401` para credenciais inválidas; `403` para conta inativa. A mensagem de `401` não deve revelar se o e-mail existe.

### `GET /api/v1/auth/me`

Autenticado. Retorna os dados públicos do principal atual. É útil para o frontend restaurar a sessão sem confiar nos dados locais.

## DTOs

- `LoginRequest(email, password)`;
- `LoginResponse(accessToken, tokenType, expiresIn, UserSummaryResponse user)`;
- `UserSummaryResponse(id, name, email, role)`.

Validações: `email` obrigatório e válido, máximo 254; `password` obrigatório e com limite máximo defensivo. Não aplicar regra de complexidade no login — ela pertence à criação/troca de senha.

## Conteúdo do token

Claims mínimos:

- `sub`: ID estável do usuário;
- `role`: perfil atual;
- `iat`, `exp`;
- `iss`: emissor configurado;
- `jti`: identificador do token.

Evitar nome, e-mail, senha ou informações de negócio no token. O token é assinado, não criptografado; seu conteúdo pode ser lido pelo cliente.

## Fluxo

1. normalizar e-mail;
2. localizar usuário;
3. executar comparação de hash mesmo sem vazar o motivo da falha;
4. rejeitar conta inativa;
5. emitir token com expiração curta, recomendação inicial de 15 minutos;
6. responder sem cookie e sem persistir token no banco;
7. em cada requisição, extrair `Authorization: Bearer <token>`, validar assinatura, emissor e expiração, carregar o principal e popular o `SecurityContext`.

## Componentes sugeridos

- `AuthController`;
- `AuthenticationService`;
- `JwtTokenService`;
- `JwtAuthenticationFilter` (`OncePerRequestFilter`);
- `AuthenticatedUser`/`UserPrincipal`;
- `AuthenticationEntryPoint` para `401`;
- `AccessDeniedHandler` para `403`.

## Dependência JWT

Escolher uma biblioteca mantida que suporte o algoritmo definido e seja compatível com Java 21/Spring Boot 4. Fixar versão no `pom.xml`; não implementar criptografia JWT manualmente. Preferir chave assimétrica (`RS256`/`ES256`) em produção. Para a Sprint, `HS256` é aceitável somente com segredo aleatório forte, externo ao repositório e com comprimento compatível.

## Critérios de aceite

- credenciais válidas dos três perfis produzem token;
- senha incorreta, usuário inexistente e token inválido não revelam detalhes;
- token expirado, adulterado, com emissor inválido ou sem prefixo correto gera `401`;
- endpoint protegido sem token gera `401`, não `403`;
- `me` reflete o usuário autenticado;
- aplicação permanece stateless (`SessionCreationPolicy.STATELESS`);
- logs nunca incluem senha ou JWT completo.

## Testes esperados

- unitário do emissor/validador para claims, expiração e assinatura;
- integração do login para sucesso, falhas e usuário inativo;
- filtro com header ausente, malformado, token expirado e válido;
- teste para cada role confirmando conversão em `GrantedAuthority` padronizada (`ROLE_OPERADOR`, etc.).
