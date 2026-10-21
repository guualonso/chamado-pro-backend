# Avisos de terceiros — ChamadoPro Backend

Este projeto usa as bibliotecas de código aberto abaixo. Todas permitem uso
comercial; as licenças exigem preservar os avisos de copyright e o texto da
licença na distribuição (o JAR gerado pelo Spring Boot mantém os arquivos
`META-INF/LICENSE*` de cada dependência).

| Componente | Licença |
|---|---|
| Spring Boot / Spring Framework / Spring Security / Spring Data JPA | Apache License 2.0 |
| Hibernate ORM 6.6 | GNU LGPL 2.1 (uso como biblioteca dinâmica, sem modificações, é permitido em software proprietário) |
| Hibernate Validator | Apache License 2.0 |
| Jackson | Apache License 2.0 |
| Apache Tomcat (embutido) | Apache License 2.0 |
| JJWT (io.jsonwebtoken) | Apache License 2.0 |
| PostgreSQL JDBC Driver | BSD 2-Clause |
| Lombok (apenas em tempo de compilação) | MIT |
| SLF4J / Logback | MIT / EPL 1.0 + LGPL 2.1 |
| Imagem Eclipse Temurin (JRE) | GPLv2 com Classpath Exception |

Para gerar a lista completa e atualizada (incluindo dependências transitivas):

```bash
mvn org.codehaus.mojo:license-maven-plugin:2.4.0:aggregate-third-party-report
```
