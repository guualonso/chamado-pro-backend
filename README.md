# Chamado Pro - Backend

Este é o backend do sistema **Chamado Pro**, desenvolvido em **Java (Spring Boot)** com banco de dados **PostgreSQL** e autenticação **JWT**.

## ✨ Funcionalidades

- Autenticação JWT, controle de acesso por perfil (ADMIN/TECNICO/CLIENTE) e por dono do recurso
- Gestão de chamados (CRUD completo)
- Comentários em chamados, com notificação automática para a outra parte
- **Escalonamento de chamados em níveis (N1, N2, N3)**
  - Manual, via endpoint dedicado
  - Automático, disparado por estouro de SLA
- **Notificações via WhatsApp** (opt-in), usando a API [CallMeBot](https://www.callmebot.com/)
- **Gestão de SLA**
  - Prioridade sugerida automaticamente por categoria do chamado (e editável pelo usuário)
  - Prazos configuráveis por prioridade
  - Criação e edição das configurações de SLA
  - Verificação periódica de estouro de prazo (scheduler)
- **Dashboard administrativo** com métricas agregadas de chamados
- **Histórico (auditoria)** de eventos de cada chamado

## 🚀 Requisitos

Antes de rodar o projeto, instale os seguintes softwares:

- [Java JDK 17+](https://adoptium.net/)
- [Maven](https://maven.apache.org/)
- [PostgreSQL 15+](https://www.postgresql.org/download/)
- [Git](https://git-scm.com/)

## 📥 Clonando o repositório

```bash
git clone --recurse-submodules https://github.com/guualonso/chamado-pro.git
cd chamado-pro/CHP/3.Implementacao/chamado-pro-backend
```

Se você já clonou sem os submódulos, rode:

```bash
git submodule update --init --recursive
```

## ⚙️ Configuração do Banco de Dados

> ⚠️ **Importante:** o Hibernate (`ddl-auto=update`) cria as **tabelas** automaticamente, mas **não cria o banco de dados**. Esse passo precisa ser feito manualmente antes da primeira execução.

1. Acesse o PostgreSQL com seu usuário:
   ```bash
   psql -U postgres
   ```

2. Crie o banco de dados (o nome usa hífen, por isso precisa estar entre aspas):
   ```sql
   CREATE DATABASE "chamado-pro";
   ```

3. (Recomendado) Crie um usuário específico, com uma senha forte:
   ```sql
   CREATE USER chamado_user WITH ENCRYPTED PASSWORD 'troque-por-uma-senha-forte';
   GRANT ALL PRIVILEGES ON DATABASE "chamado-pro" TO chamado_user;
   ```

## 🛠️ Configuração da aplicação

Nenhuma credencial fica no código. Tudo é lido de **variáveis de ambiente** (veja `.env.example`):

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL` | não | JDBC do PostgreSQL (padrão `jdbc:postgresql://localhost:5432/chamado-pro`) |
| `DB_USERNAME` | não | Usuário do banco (padrão `postgres`) |
| `DB_PASSWORD` | **sim** | Senha do banco |
| `JWT_SECRET` | **sim** | Segredo de assinatura do JWT, mínimo 32 bytes (`openssl rand -base64 48`) |
| `APP_ENCRYPTION_KEY` | **sim** | Chave AES-256 em Base64 para dados sensíveis em repouso (`openssl rand -base64 32`) |
| `ADMIN_EMAIL` / `ADMIN_DEFAULT_PASSWORD` | não | Admin inicial. Sem senha definida, uma senha aleatória é gerada e exibida **uma vez** no log |
| `APP_CORS_ALLOWED_ORIGINS` | não | Origens liberadas para CORS (vazio quando o front é servido pelo mesmo domínio) |
| `WHATSAPP_ENABLED` | não | Liga/desliga o envio via CallMeBot (padrão `true`) |

A aplicação **não sobe** sem `JWT_SECRET` e `APP_ENCRYPTION_KEY` válidos.

Exemplo para desenvolvimento local:

```bash
export DB_PASSWORD=sua_senha
export JWT_SECRET=$(openssl rand -base64 48)
export APP_ENCRYPTION_KEY=$(openssl rand -base64 32)
export ADMIN_DEFAULT_PASSWORD=uma_senha_forte
mvn spring-boot:run
```

> Guarde o `APP_ENCRYPTION_KEY` usado: se ele mudar, as API Keys do WhatsApp já gravadas não poderão ser lidas.

## 📱 Notificações por WhatsApp (CallMeBot)

O envio por WhatsApp usa a API gratuita do [CallMeBot](https://www.callmebot.com/), um **serviço de terceiro**. Por isso:

- só acontece para usuários que deram **consentimento explícito** (opt-in) na tela de consentimento do sistema;
- a mensagem enviada ao terceiro é **genérica** ("há uma atualização no chamado #N"), sem título, descrição ou comentários;
- a API Key é guardada **criptografada** no banco.

Para cada usuário que quiser receber mensagens:

1. Adicionar o número do CallMeBot (+34 644 84 71 64) aos contatos do WhatsApp.
2. Enviar a mensagem `I allow callmebot to send me messages`.
3. Informar o telefone (só dígitos, com DDI) e a API Key recebida no cadastro do usuário.
4. O próprio usuário ativar a opção de WhatsApp na tela de consentimento.

## ▶️ Rodando o backend

### Usando Maven
```bash
mvn spring-boot:run
```

### Usando Java diretamente
```bash
mvn clean package -DskipTests
java -jar target/chamado-pro-backend-0.0.1-SNAPSHOT.jar
```

O backend estará rodando em:

```
http://localhost:8080
```

Na primeira execução é criado um usuário **ADMIN** com o e-mail de `ADMIN_EMAIL` (padrão `admin@chamado.com`) e a senha de `ADMIN_DEFAULT_PASSWORD`. Se ela não for informada, uma senha aleatória aparece no log de inicialização. Troque-a após o primeiro acesso.

## 📌 Estrutura do Projeto

```
chamado-pro-backend/
│── src/main/java/com/chamado
│   ├── ChamadoMain.java           # Classe principal
│   ├── config/                    # Segurança, JWT, scheduling, dados iniciais
│   ├── controller/                # Controllers (endpoints REST)
│   ├── service/                   # Regras de negócio (chamados, SLA, escalonamento,
│   │                               #   notificações, WhatsApp, dashboard...)
│   ├── repository/                # Interfaces do JPA
│   ├── model/                     # Entidades (Chamado, Usuario, Historico,
│   │                               #   Notificacao, SlaConfig...)
│   ├── model/enums/                # Enums (StatusChamado, Prioridade, NivelTecnico...)
│   ├── dto/                       # Objetos de transferência de dados
│   ├── exception/                 # Exceções e tratamento global de erros
│
│── src/main/resources/
│   ├── application.properties     # Configurações
│
│── pom.xml                        # Dependências do Maven
```

## 🔌 Principais Endpoints

Todos exigem `Authorization: Bearer <token>`, exceto `/auth/login`.

| Método | Endpoint | Quem pode | Descrição |
|---|---|---|---|
| POST | `/auth/login` | público | Autenticação e geração de token JWT (bloqueio após 5 falhas) |
| GET | `/chamados` | ADMIN, TECNICO | Fila completa de chamados |
| GET | `/chamados/{id}` | ADMIN, TECNICO, cliente dono | Detalhe do chamado |
| POST | `/chamados` | ADMIN, CLIENTE | Abre chamado (o cliente é sempre o usuário logado) |
| PUT | `/chamados/{id}` | ADMIN, TECNICO, cliente dono | Cliente: título/descrição/avaliação. Equipe: status/categoria/prioridade |
| DELETE | `/chamados/{id}` | ADMIN | Exclui chamado |
| GET | `/chamados/cliente/{id}` | ADMIN ou o próprio cliente | Chamados de um cliente |
| GET | `/chamados/tecnico/{id}` | ADMIN ou o próprio técnico | Chamados de um técnico |
| PUT | `/chamados/{id}/atribuir-tecnico/{tecnicoId}` | ADMIN, TECNICO (só a si mesmo) | Atribui um técnico ao chamado |
| PUT | `/chamados/{id}/escalonar` | ADMIN, TECNICO | Escalona manualmente o chamado para outro nível |
| GET | `/chamados/{id}/historico` | quem pode ver o chamado | Histórico (auditoria) de eventos do chamado |
| GET/POST | `/comentarios/chamado/{id}` | quem pode ver o chamado | Comentários (o autor é sempre o usuário logado) |
| DELETE | `/comentarios/{id}` | autor ou ADMIN | Exclui comentário |
| GET/POST/PUT/DELETE | `/usuarios` | ADMIN | CRUD de usuários |
| GET | `/usuarios/tecnicos` | ADMIN, TECNICO | Lista técnicos (id, nome e nível) |
| GET | `/usuarios/me` | autenticado | Dados do próprio usuário e situação dos consentimentos |
| PUT | `/usuarios/me/consentimentos` | autenticado | Aceite dos termos e opt-in/opt-out de WhatsApp |
| GET | `/usuarios/me/exportar` | autenticado | Exporta os próprios dados (LGPD) |
| DELETE | `/usuarios/me` | autenticado | Anonimiza a própria conta (LGPD) |
| GET | `/notificacoes` | autenticado | Notificações do usuário autenticado |
| GET | `/notificacoes/nao-lidas` | autenticado | Contagem de notificações não lidas |
| PUT | `/notificacoes/{id}/lida` | destinatário | Marca notificação como lida |
| GET/PUT | `/sla-config` | ADMIN | Configuração de prazos de SLA por prioridade |
| GET | `/dashboard/admin` | ADMIN | Métricas agregadas para o painel administrativo |

Veja também [SECURITY.md](SECURITY.md) e [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## ✅ Passo a Passo Resumido

1. Instalar JDK, Maven e PostgreSQL.
2. Clonar o projeto com submódulos.
3. Criar o banco de dados `chamado-pro` no PostgreSQL.
4. Definir as variáveis de ambiente (`DB_PASSWORD`, `JWT_SECRET`, `APP_ENCRYPTION_KEY`).
5. Rodar `mvn spring-boot:run`.

---

📌 Agora seu backend está pronto para rodar 🚀
