# Segurança — ChamadoPro Backend

Este documento resume os controles de segurança e privacidade da API e
o que precisa ser feito na operação.

## Ações obrigatórias de operação

1. **Rotacionar segredos já expostos.** Versões anteriores deste repositório
   continham no `application.properties` a senha do banco e o `jwt.secret`.
   Eles continuam no histórico do Git: **troque a senha do PostgreSQL e gere
   um novo `JWT_SECRET`** em todos os ambientes (isso invalida tokens antigos).
2. **Trocar a senha do admin padrão.** Em instalações antigas o admin foi
   criado com `admin123`. Altere-a imediatamente.
3. Definir `DB_PASSWORD`, `JWT_SECRET` e `APP_ENCRYPTION_KEY` via variáveis de
   ambiente / AWS Secrets Manager / SSM Parameter Store (nunca em arquivo versionado).
4. Fazer backup do `APP_ENCRYPTION_KEY`: sem ele as API Keys do WhatsApp não podem ser lidas.

## Controles implementados

| Área | Controle |
|---|---|
| Segredos | Credenciais lidas de variáveis de ambiente; a aplicação não sobe com `JWT_SECRET` (< 32 bytes) ou `APP_ENCRYPTION_KEY` ausentes |
| Autenticação | JWT HS256 com expiração de 1h; BCrypt (custo 12); senha mínima de 8 caracteres |
| Força bruta | Bloqueio de 15 min após 5 tentativas de login falhas por e-mail (HTTP 429) |
| Enumeração de usuários | Login devolve sempre a mesma mensagem genérica |
| Autorização por perfil | `@PreAuthorize` em todos os endpoints administrativos (usuários, SLA, dashboard, exclusão de chamados) |
| Autorização por recurso (IDOR) | Cliente só acessa os próprios chamados, histórico e comentários; técnico só atribui chamados a si mesmo |
| Mass assignment | Cliente não altera status/prioridade; `clienteId` e autor de comentário vêm do usuário autenticado, não do corpo da requisição |
| Validação de entrada | Bean Validation em todos os DTOs (tamanhos, e-mail, telefone, avaliação 0–5, SLA) |
| Erros | Respostas padronizadas sem stack trace, nomes de classe ou mensagens internas |
| Cabeçalhos | `X-Frame-Options: DENY`, `X-Content-Type-Options`, HSTS, `Content-Security-Policy`, `Referrer-Policy`, `Permissions-Policy` |
| CORS | Desabilitado por padrão; liberado só para origens em `APP_CORS_ALLOWED_ORIGINS` |
| Superfície | Removida a liberação do `/h2-console` e o `frameOptions.disable()` |
| Dados em repouso | API Key do WhatsApp criptografada com AES-256-GCM |
| Logs | SQL não é logado por padrão; telefone mascarado; API Key e conteúdo de mensagens não vão para o log |
| Contêiner | Imagem JRE (sem JDK), usuário sem privilégios; troca da imagem `openjdk` descontinuada |

## Privacidade (LGPD)

| Direito / princípio | Implementação |
|---|---|
| Consentimento | Aceite versionado dos Termos de Uso e da Política de Privacidade (`app.legal.versao-termos`), com data/hora |
| Consentimento para terceiros | WhatsApp (CallMeBot) só com opt-in do próprio titular; revogável; zerado se o telefone mudar |
| Minimização | Mensagens ao CallMeBot sem título/descrição/comentários; `/usuarios/tecnicos` devolve só id, nome e nível; listagem completa de usuários restrita ao ADMIN |
| Acesso e portabilidade | `GET /usuarios/me/exportar` (JSON) |
| Eliminação | `DELETE /usuarios/me` anonimiza os dados pessoais e apaga notificações (o histórico dos chamados é preservado sem identificação) |

## Auditoria de serviços de terceiros

| Terceiro | Uso | Dados enviados | Observações |
|---|---|---|---|
| CallMeBot (api.callmebot.com) | Notificação por WhatsApp | Telefone, API Key do usuário e texto genérico com o nº do chamado | Serviço gratuito, sem contrato/DPA nem SLA; a API Key trafega na URL (limitação da API). Usar apenas com consentimento. Avaliar substituir pela WhatsApp Business Platform (Meta) em produção |
| AWS (EC2, RDS, ALB) | Hospedagem e banco | Todos os dados da aplicação | Operador de dados; manter RDS com criptografia em repouso e acesso só pela VPC |

Não há SDKs de analytics, publicidade ou rastreamento no backend.

## Recomendações pendentes (não implementadas)

- Rate limiting compartilhado (Redis) se houver mais de uma instância atrás do ALB.
- Migrações de banco versionadas (Flyway) em vez de `ddl-auto=update`.
- Revogação de tokens no logout / troca de senha (lista de bloqueio ou tokens de curta duração + refresh token).
- Rotina de retenção (ex.: apagar notificações com mais de 12 meses).
- Testes automatizados de autorização (não há suíte de testes no projeto).

## Como reportar uma vulnerabilidade

Envie os detalhes de forma privada para o contato de privacidade/segurança
informado na Política de Privacidade do sistema. Não abra issue pública.
