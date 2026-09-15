# Observabilidade e operação

## Objetivo

Permitir diagnóstico de falhas e acompanhamento básico da API sem expor dados de negócio ou credenciais.

## Logs

Usar logging estruturado ou padrão consistente contendo:

- timestamp UTC;
- nível;
- aplicação/ambiente;
- `traceId`/`correlationId`;
- método, rota e status sem corpo sensível;
- duração;
- evento de negócio relevante com IDs técnicos, não payload completo.

Nunca registrar senha, hash, JWT, header `Authorization`, URI MongoDB com credenciais ou descrições potencialmente sensíveis em nível normal.

## Correlation ID

Aceitar `X-Correlation-Id` somente se válido e com tamanho limitado; caso contrário gerar UUID. Propagar no MDC, na resposta e no Problem Details. Isso facilita correlacionar um erro do frontend ao log.

## Actuator

Expor inicialmente:

- `/actuator/health`;
- `/actuator/info` se houver conteúdo útil e não sensível.

Detalhes do health devem ser limitados em produção. Endpoints como `env`, `beans`, `mappings`, heap dump e loggers não devem ser públicos.

## Métricas recomendadas

- quantidade e latência de requests por rota/status;
- falhas de autenticação sem identificar senha/token;
- erros 5xx;
- tempo das agregações de dashboard;
- disponibilidade do MongoDB;
- contagem de decisões de ideia e conclusão de projetos como métricas de negócio opcionais.

## Auditoria funcional

Logs não substituem histórico persistido. Alterações de estratégia, decisões de ideia e progresso/resultado de projeto precisam guardar ator e instante no domínio. Logs servem ao diagnóstico operacional.

## Critérios de aceite

- health reflete indisponibilidade do MongoDB;
- erro retornado contém identificador correlacionável;
- dados sensíveis não aparecem em logs de testes;
- endpoints Actuator não autorizados não são acessíveis;
- configuração de nível de log varia por ambiente sem recompilar.
