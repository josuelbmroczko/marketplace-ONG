# --- Estágio 1: Build (O Construtor) ---
# Usamos uma imagem que já tem Maven e JDK 17 (conforme o pom.xml que corrigimos)
FROM maven:3.9-eclipse-temurin-17 AS builder

# 1. Instala o Node.js (necessário para o 'npm run build' do React/Vite)
# Nós precisamos do Node 18+ para o Vite funcionar.
RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

WORKDIR /app

# 2. Copia SÓ os arquivos de projeto primeiro para cachear as dependências
COPY pom.xml .
COPY frontend/package.json frontend/package.json
# Copie o lock file se ele existir, para builds mais rápidos
COPY frontend/package-lock.json frontend/package-lock.json

# 3. Baixa as dependências (Maven)
RUN mvn dependency:go-offline

# 4. Instala as dependências (NPM)
# (Rodamos 'npm install' separado para também aproveitar o cache do Docker)
RUN cd frontend && npm install

# 5. Copia o resto do código-fonte
COPY src ./src
COPY frontend/ ./frontend

# 6. Roda o build!
# O 'mvn package' agora vai, automaticamente (graças ao pom.xml):
#  a) Rodar 'npm run build' na pasta 'frontend'
#  b) Copiar o 'dist' do React para 'target/classes/static'
#  c) Criar o .jar final
RUN mvn clean package -DskipTests


# --- Estágio 2: Runtime (O Executor) ---
# Use uma imagem JRE (Java Runtime) que é muito menor e mais segura
FROM eclipse-temurin:17-jre-jammy

# Cria um usuário e grupo não-root para segurança
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

WORKDIR /app

# Copia APENAS o .jar buildado do estágio anterior
COPY --from=builder /app/target/pageLogin-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando para executar a aplicação
# Adicionamos um profile 'docker' para qualquer configuração futura
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]