Marketplace Multi-ONG
Este é um projeto full-stack que serve como um marketplace para Organizações Não Governamentais (ONGs). O backend é construído com Spring Boot e o frontend com React (Vite), integrando-se também com a API do Gemini para funcionalidades de IA.

O projeto é containerizado com Docker e orquestrado com Docker Compose para facilitar a execução.

Tecnologias Utilizadas
Backend
Java 17

Spring Boot 3.3

Spring Data JPA (Hibernate)

Spring Security

PostgreSQL (Banco de Dados)

Lombok

Logstash (Logback): Para logs em formato JSON.

Frontend
React.js

Vite (Build tool)

Axios: Para requisições HTTP.

React Router DOM: Para gerenciamento de rotas.

Styled Components: Para estilização CSS-in-JS.

Infra & DevOps
Docker & Docker Compose

Maven: Gerenciador de dependências e build do Java (que também dispara o build do frontend).

Pré-requisitos
Para rodar o projeto, você precisará ter instalado:

Java 17 (ou superior)

Apache Maven 3.9 (ou superior)

Node.js 18 (ou superior) e NPM

Docker

Docker Compose

Como Rodar o Projeto
Existem duas formas principais de rodar este projeto:

Método 1: Docker Compose (Recomendado)
Este método sobe a aplicação completa (Backend + Banco de Dados) e é a forma mais simples de ver tudo funcionando.

Clone o repositório:

Bash

git clone https://github.com/josuelbmroczko/marketplace-ONG.git
cd marketplace-ONG
Crie o arquivo de ambiente: Crie um arquivo chamado .env na raiz do projeto e adicione suas chaves de API. Você pode usar o docker-compose.yml como base:

GEMINI_API_KEY=
GEMINI_API_URL=

Snippet de código

# Configuração do Banco de Dados Docker
POSTGRES_DB=marketplace_db
POSTGRES_USER=marketplace_user
POSTGRES_PASSWORD=postgres

# Chaves da API do Gemini
GEMINI_API_KEY=SUA_CHAVE_API_VAI_AQUI
GEMINI_API_URL=https://generativelanguage.googleapis.com/...
Suba os containers: Este comando irá construir a imagem do Spring Boot (que por sua vez constrói o React) e subir o container do banco de dados.

Bash

docker-compose up --build
Acesse a aplicação: A aplicação estará disponível em http://localhost:8080.

Método 2: Localmente (Modo de Desenvolvimento)
Este método é ideal para desenvolver, pois permite o hot-reload do frontend e do backend separadamente. Você precisará de dois terminais.

1. Backend (Spring Boot)
   Configure o ambiente: Edite o arquivo src/main/resources/application.properties para apontar para o seu banco de dados local.

Properties

# Conexão com o banco local
spring.datasource.url=jdbc:postgresql://localhost:5432/marketplace_db
spring.datasource.username=marketplace_user
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update

# Chaves da API
gemini.api.key=SUA_CHAVE_API_VAI_AQUI
gemini.api.url=https://generativelanguage.googleapis.com/...
Rode a aplicação Spring: Na raiz do projeto (marketplace-ONG/), execute o Maven:

Bash

mvn spring-boot:run
O backend estará rodando em http://localhost:8080.

2. Frontend (React)
   Navegue até a pasta do frontend: Em um novo terminal, vá para a pasta frontend.

Bash

cd frontend
Instale as dependências:

Bash

npm install
Rode o servidor de desenvolvimento do Vite:

Bash

npm run dev
O frontend estará disponível em http://localhost:5173 (ou outra porta indicada pelo Vite).

Nota: O arquivo vite.config.js já está configurado com um proxy. Qualquer chamada à API (/api/...) feita pelo React será automaticamente redirecionada para o backend no http://localhost:8080.

📁 Estrutura do Projeto
marketplace-ONG/
├── Dockerfile           # Define a imagem Docker da aplicação
├── docker-compose.yml   # Orquestra os serviços (app + db)
├── pom.xml              # Dependências e build do Backend (Maven)
├── mvnw                 # Maven Wrapper (Linux/Mac)
├── mvnw.cmd             # Maven Wrapper (Windows)
├── .env.example         # Exemplo de variáveis de ambiente
├── frontend/            # Pasta do projeto React/Vite
│   ├── vite.config.js   # Configuração do Vite (incluindo o proxy)
│   ├── package.json     # Dependências do Frontend (NPM)
│   └── src/             # Código-fonte do React
└── src/                 # Código-fonte do Backend (Spring Boot)
├── main/
│   ├── java/        # Código Java/Spring
│   └── resources/   # Recursos do Spring
│       ├── application.properties
│       ├── logback.xml
│       └── static/  # O build do React é cop