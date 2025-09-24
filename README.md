# Chamado Pro - Backend

Este é o backend do sistema **Chamado Pro**, desenvolvido em **Java (Spring Boot)** com banco de dados **PostgreSQL**.

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

1. Acesse o PostgreSQL com seu usuário:
   ```bash
   psql -U postgres
   ```

2. Crie o banco de dados:
   ```sql
   CREATE DATABASE chamado_pro;
   ```

3. (Opcional) Crie um usuário específico:
   ```sql
   CREATE USER chamado_user WITH ENCRYPTED PASSWORD 'senha123';
   GRANT ALL PRIVILEGES ON DATABASE chamado_pro TO chamado_user;
   ```

## 🛠️ Configuração da aplicação

No arquivo `src/main/resources/application.properties` configure:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chamado-pro
spring.datasource.username=postgres
spring.datasource.password=alonso

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

⚠️ **Troque `chamado_user` e `senha123` pelos dados reais do seu banco.**

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

## 📌 Estrutura do Projeto

```
chamado-pro-backend/
│── src/main/java/com/chamado
│   ├── ChamadoApplication.java   # Classe principal
│   ├── controller/               # Controllers (endpoints REST)
│   ├── service/                  # Regras de negócio
│   ├── repository/               # Interfaces do JPA
│   ├── model/                    # Entidades
│
│── src/main/resources/
│   ├── application.properties    # Configurações
│
│── pom.xml                       # Dependências do Maven
```

---

## ✅ Passo a Passo Resumido

1. Instalar JDK, Maven e PostgreSQL.  
2. Clonar o projeto com submódulos.  
3. Criar banco de dados no PostgreSQL.  
4. Configurar `application.properties`.  
5. Rodar `mvn spring-boot:run`.  

---

📌 Agora seu backend está pronto para rodar 🚀
