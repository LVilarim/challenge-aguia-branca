# 1. Autenticação e usuários

## Visão geral

O backend usa JWT stateless assinado com HS256. O login é a única operação de usuário exposta sem autenticação. Não existem endpoints de cadastro, listagem ou alteração de usuários.

Papéis possíveis: `OPERADOR`, `GESTOR`, `LIDER`.

## Login

`POST /api/v1/auth/login` — público.

Request:

```json
{
  "email": "lider@aguia.local",
  "password": "Lider123!"
}
```

Validações:

- `email`: obrigatório, formato de e-mail, máximo 254 caracteres;
- `password`: obrigatória, máximo 256 caracteres.

Resposta `200`:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "id": "66f...",
    "name": "Líder Demo",
    "email": "lider@aguia.local",
    "role": "LIDER"
  }
}
```

Erros relevantes:

- `400 VALIDATION_ERROR`: payload inválido;
- `401 UNAUTHORIZED`: e-mail inexistente ou senha incorreta, sempre com mensagem genérica;
- `403 ACCESS_DENIED`: conta inativa.

## Usuário autenticado

`GET /api/v1/auth/me` — qualquer usuário autenticado.

Resposta `200`:

```json
{
  "id": "66f...",
  "name": "Operador Demo",
  "email": "operador@aguia.local",
  "role": "OPERADOR"
}
```

Use este endpoint ao restaurar a aplicação para confirmar que o token ainda corresponde a um usuário ativo. A cada requisição protegida, o backend procura o usuário pelo `sub` do JWT e rejeita usuários inexistentes ou inativos.

## Integração recomendada

1. autenticar e guardar `accessToken`, `expiresIn` e `user`;
2. enviar `Authorization: Bearer <accessToken>`;
3. ao iniciar/recarregar a aplicação, chamar `/auth/me`;
4. em qualquer `401`, limpar a sessão e encaminhar ao login;
5. em `403`, manter a sessão e informar falta de permissão/conta inativa conforme o contexto;
6. não decodificar o JWT como fonte autoritativa de dados do usuário.

O backend não possui refresh token. Ao expirar, o usuário deve autenticar novamente. A expiração padrão é 900 segundos, mas o frontend deve respeitar `expiresIn` em vez de fixar esse valor.

## Usuários de desenvolvimento

No perfil `dev`, o seed pode criar um usuário de cada role usando as variáveis `SEED_*`. Os valores padrão estão em `.env.example`; não os assumir em ambientes compartilhados ou de produção.

## Segurança das senhas

Senhas são comparadas com BCrypt, custo 12, e o hash nunca aparece nas respostas. Não há endpoint de troca ou recuperação de senha na versão atual.
