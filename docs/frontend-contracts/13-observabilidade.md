# 13. Observabilidade

## Health check

`GET /actuator/health` — público.

Resposta saudável típica:

```json
{ "status": "UP" }
```

Detalhes não são expostos. O frontend comum não deve fazer polling agressivo deste endpoint; ele é destinado principalmente a infraestrutura e diagnóstico.

## Correlation ID

O cliente pode enviar:

```http
X-Correlation-Id: web-2c4f3d1a
```

Formato aceito: `[A-Za-z0-9._-]{1,64}`. Se ausente ou inválido, o backend gera UUID. O valor é devolvido em `X-Correlation-Id` e aparece como `traceId` em Problem Details.

Uso recomendado:

- gerar um ID por request ou por ação relevante do usuário;
- registrar o ID nos logs de desenvolvimento/telemetria do frontend;
- mostrá-lo em erros técnicos para suporte;
- não colocar e-mail, nome, token ou outro dado pessoal no ID.

## Logs HTTP do backend

Cada request gera log com método, caminho, status e duração em milissegundos. Body e token não são registrados pelo filtro. Não existe endpoint frontend para ler esses logs.

## Headers expostos ao navegador

CORS expõe `X-Correlation-Id` e `Location`, portanto podem ser lidos por `fetch`/Axios. Exemplo:

```ts
const traceId = response.headers.get("X-Correlation-Id");
```

## Falhas operacionais

- `5xx`: exibir estado recuperável e preservar `traceId`;
- timeout/rede: diferenciar de um Problem Details recebido;
- health `DOWN`: tratar como indisponibilidade do serviço, não como sessão expirada;
- não repetir automaticamente mutações sem estratégia de idempotência.
