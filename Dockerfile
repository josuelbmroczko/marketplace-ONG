# Estágio 1: Build (Opcional, mas recomendado se o Maven não estiver local)
# Vamos usar uma abordagem mais simples que assume que você buildou o .jar localmente
# com `mvn package` primeiro.

# Estágio Final: Runtime
# Use uma imagem JRE (Java Runtime) que é muito menor que um JDK completo.
# A sua POM usa Java 25, então vamos usar o Temurin 25.
FROM eclipse-temurin:25-jre-jammy

# Cria um usuário e grupo não-root para segurança
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# Define o diretório de trabalho dentro do container
WORKDIR /app

# O nome do .jar vem do seu pom.xml (<artifactId>pageLogin</artifactId>-<version>0.0.1-SNAPSHOT</version>.jar)
# Copia o .jar que você buildou localmente para dentro do container
COPY target/pageLogin-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta que o Spring Boot usa
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]