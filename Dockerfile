# syntax=docker/dockerfile:1

# ---- build: compila e roda os testes do build padrão (não precisam de Docker) ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY contract contract
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B package

# ---- runtime: só o JRE e o jar, usuário sem privilégios ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ENV PORT=8081 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
COPY --from=build /app/target/ms-fila-sql.jar app.jar
USER 10001:10001
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fsS "http://localhost:${PORT}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
