# Chamado Pro - Backend

Este é o backend do sistema **Chamado Pro**, desenvolvido em **Java (Spring Boot)** com banco de dados **PostgreSQL** e autenticação **JWT**.

## ✨ Funcionalidades

- Autenticação JWT (login, controle de acesso por papel de usuário)
- Gestão de chamados (CRUD completo)
- Comentários em chamados, com notificação automática para a outra parte
- **Escalonamento de chamados em níveis (N1, N2, N3)**
  - Manual, via endpoint dedicado
  - Automático, disparado por estouro de SLA
- **Notificações reais via WhatsApp**, usando a API [CallMeBot](https://www.callmebot.com/)
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

3. (Opcional) Crie um usuário específico:
   ```sql
   CREATE USER chamado_user WITH ENCRYPTED PASSWORD 'senha123';
   GRANT ALL PRIVILEGES ON DATABASE "chamado-pro" TO chamado_user;
   ```

## 🛠️ Configuração da aplicação

No arquivo `src/main/resources/application.properties` configure:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chamado-pro
spring.datasource.username=postgres
spring.datasource.password=sua_senha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

jwt.secret=sua_chave_secreta_jwt
jwt.expiration-ms=3600000
```

⚠️ **Troque `sua_senha`, `sua_chave_secreta_jwt` e (se for o caso) `chamado_user`/`senha123` pelos dados reais do seu ambiente.**

## 📱 Configuração do WhatsApp (CallMeBot)

As notificações de chamados são enviadas via WhatsApp usando a API gratuita do [CallMeBot](https://www.callmebot.com/).

1. Adicione o número oficial do CallMeBot aos seus contatos no WhatsApp.
2. Envie a mensagem de ativação solicitada pelo bot.
3. Você receberá uma **API Key** pessoal por mensagem.
4. Configure no `application.properties`:
   ```properties
   callmebot.api.key=SUA_API_KEY
   callmebot.phone.number=SEU_NUMERO_COM_DDI
   ```

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

Um usuário **ADMIN** padrão é criado automaticamente na primeira execução:

```
email: admin@chamado.com
senha: admin123
```

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
│
│── src/main/resources/
│   ├── application.properties     # Configurações
│
│── pom.xml                        # Dependências do Maven
```

## 🔌 Principais Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/login` | Autenticação e geração de token JWT |
| GET/POST/PUT/DELETE | `/chamados` | CRUD de chamados |
| PUT | `/chamados/{id}/atribuir-tecnico/{tecnicoId}` | Atribui um técnico ao chamado |
| PUT | `/chamados/{id}/escalonar` | Escalona manualmente o chamado para outro nível |
| GET | `/chamados/{id}/historico` | Histórico (auditoria) de eventos do chamado |
| GET/POST/DELETE | `/comentarios` | Comentários em chamados |
| GET/POST/PUT/DELETE | `/usuarios` | CRUD de usuários |
| GET | `/usuarios/tecnicos` | Lista técnicos (com nível N1/N2/N3) |
| GET | `/notificacoes` | Notificações do usuário autenticado |
| GET | `/notificacoes/nao-lidas` | Contagem de notificações não lidas |
| PUT | `/notificacoes/{id}/lida` | Marca notificação como lida |
| GET/POST/PUT | `/sla-config` | Configuração de prazos de SLA por prioridade |
| GET | `/dashboard` | Métricas agregadas para o painel administrativo |

## ✅ Passo a Passo Resumido

1. Instalar JDK, Maven e PostgreSQL.
2. Clonar o projeto com submódulos.
3. Criar o banco de dados `chamado-pro` no PostgreSQL.
4. Configurar `application.properties` (banco, JWT e CallMeBot).
5. Rodar `mvn spring-boot:run`.

---

📌 Agora seu backend está pronto para rodar 🚀
