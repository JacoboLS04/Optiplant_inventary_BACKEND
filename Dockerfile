# Etapa de compilación: genera el JAR desde el código fuente.
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Primero las dependencias (capa cacheable): Maven no descarga
# dependencias en cada rebuild si no cambian.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# Etapa de ejecución: imagen mínima con JRE 21.
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
