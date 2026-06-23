# ============================================================
# Backend PNAE Lite - build multi-stage
# Mantem o H2 em memoria (nenhum banco externo necessario).
# ============================================================

# ---- Estagio 1: build (Maven + JDK 17) ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache de dependencias: copia so o pom primeiro
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Codigo-fonte e empacotamento (testes ja validados no Passo 3)
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Estagio 2: runtime (apenas JRE) ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
