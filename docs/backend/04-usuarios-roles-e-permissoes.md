# Usuários, roles e permissões

## Objetivo

Representar as três identidades exigidas e centralizar a matriz de acesso. A Sprint 2 não exige cadastro público nem CRUD administrativo completo de usuários.

## Modelo

`User`: `id`, `name`, `email`, `passwordHash`, `role`, `active`, `createdAt`, `updatedAt`.

`Role`:

```text
OPERADOR
GESTOR
LIDER
```

Não presumir hierarquia automática entre roles. Um líder não recebe implicitamente todas as operações de gestor ou operador; cada endpoint deve declarar os perfis autorizados segundo o requisito.

## Provisionamento inicial

Opções aceitas:

1. initializer idempotente apenas em `dev`, lendo e-mails e senhas do ambiente;
2. script administrativo versionado e executado conscientemente;
3. dados de teste criados por fixtures durante testes.

Nunca manter senhas fixas reais no Git. O initializer deve verificar o e-mail antes de inserir e jamais redefinir senha automaticamente numa reinicialização.

## Endpoints da Sprint

### `GET /api/v1/users/me`

Pode ser um alias de `/auth/me`; escolher apenas um contrato para evitar duplicidade. Retorna dados públicos do usuário atual.

Não expor listagem geral, alteração de role, ativação ou criação pública sem um requisito de administração. Essas ações ampliam a superfície de segurança e ficam fora do escopo.

## Matriz de autorização

| Operação | OPERADOR | GESTOR | LIDER |
|---|:---:|:---:|:---:|
| Login/consultar a si | ✓ | ✓ | ✓ |
| Consultar estratégias | ✓ | ✓ | ✓ |
| Gerenciar estratégias | — | — | ✓ |
| Gerenciar próprias ideias | ✓ | — | — |
| Consultar/priorizar/aprovar ideias | — | ✓ | — |
| Gerenciar projetos e resultados | — | ✓ | — |
| Consultar andamento de projetos | — | ✓ | ✓ |
| Consultar dashboard executivo | — | — | ✓ |

“Gerenciar próprias ideias” exige role e propriedade. Ter o ID de outra ideia não concede acesso.

## Estrutura sugerida

- `UserDocument`, `Role`;
- `UserRepository` com `findByEmailIgnoreCase` e `existsByEmailIgnoreCase`;
- `UserDetailsService` ou adaptador equivalente;
- `CurrentUserProvider` para fornecer o ID do principal à aplicação;
- `UserSummaryResponse`, sem `passwordHash`.

## Hash de senha

Usar `PasswordEncoder` resistente a força bruta. Recomendação: Argon2id quando a dependência/ambiente estiverem adequados; BCrypt com custo calibrado é alternativa simples. O algoritmo e parâmetros precisam permitir atualização futura. Nunca criptografar senha de forma reversível.

## Critérios de aceite

- e-mail é único independentemente de caixa;
- toda conta possui exatamente uma role válida;
- usuário inativo não autentica;
- nenhuma API serializa `passwordHash`;
- autorização respeita a matriz, inclusive casos de propriedade;
- seed é idempotente e limitado a ambiente não produtivo.

## Testes esperados

- unicidade/normalização de e-mail;
- carregamento de usuário ativo e inativo;
- hash não coincide com senha em texto;
- matriz de acesso parametrizada por role;
- tentativa de operador acessar ideia alheia retorna `404` ou `403` conforme política única adotada. Recomenda-se `404` para não revelar existência.
