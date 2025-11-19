
FROM maven:3.9-eclipse-temurin-17 AS builder

RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

COPY pom.xml .
COPY frontend/package.json frontend/package.json
COPY frontend/package-lock.json frontend/package-lock.json

RUN mvn dependency:go-offline

RUN cd frontend && npm install

COPY src ./src
COPY frontend/ ./frontend

RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-jammy

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

WORKDIR /app

COPY --from=builder /app/target/pageLogin-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]