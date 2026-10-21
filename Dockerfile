# Etapa de build
FROM public.ecr.aws/docker/library/maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q clean package -DskipTests

# Etapa de execução: apenas JRE (sem JDK/compilador) e usuário sem privilégios.
# A imagem openjdk foi descontinuada e não recebe mais correções de segurança.
FROM public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
