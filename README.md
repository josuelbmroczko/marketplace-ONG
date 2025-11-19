# Como Rodar o Projeto Marketplace ONG

Existem duas formas de executar o projeto: usando Docker (recomendado) ou localmente para desenvolvimento.

## MÉTODO 1 — Rodar com Docker (Recomendado)

1. Clone o repositório:
   git clone https://github.com/josuelbmroczko/marketplace-ONG.git
   cd marketplace-ONG

2. Crie o arquivo .env na raiz do projeto com o seguinte conteúdo:

POSTGRES_DB=marketplace_db
POSTGRES_USER=marketplace_user
POSTGRES_PASSWORD=postgres

GEMINI_API_KEY=SUA_CHAVE_API_VAI_AQUI
GEMINI_API_URL=https://generativelanguage.googleapis.com/...

3. Suba os containers:
   docker-compose up --build

4. Acesse a aplicação:
   http://localhost:8080

------------------------------------------------------------

## MÉTODO 2 — Rodar Localmente (Modo Desenvolvimento)

### Backend (Spring Boot)

Edite o arquivo: src/main/resources/application.properties  
Use este conteúdo:

spring.datasource.url=jdbc:postgresql://localhost:5432/marketplace_db
spring.datasource.username=marketplace_user
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

gemini.api.key=SUA_CHAVE_API_VAI_AQUI
gemini.api.url=https://generativelanguage.googleapis.com/...

Inicie o backend:
mvn spring-boot:run

Backend rodando em:
http://localhost:8080

------------------------------------------------------------

### Frontend (React)

Acesse a pasta:
cd frontend

Instale dependências:
npm install

Inicie o servidor:
npm run dev

Frontend rodando em:
http://localhost:5173

(O vite.config.js já faz o proxy para o backend automaticamente)

------------------------------------------------------------

## Estrutura do Projeto

marketplace-ONG/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .env.example
├── frontend/
│   ├── vite.config.js
│   ├── package.json
│   └── src/
└── src/
├── main/
├── java/
└── resources/
├── application.properties
├── logback.xml
└── static/
