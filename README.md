# Projeto Marketplace Multi-ONG

Esqueleto inicial do projeto Spring Boot para o desafio.

## 1. Requisitos

* Java 21 (ou 17)
* Maven 3.8+
* PostgreSQL 14+ (rodando localmente)

## 2. Configuração do Banco de Dados

Antes de rodar, você precisa ter um banco de dados PostgreSQL local chamado `marketplace_db` e um usuário `marketplace_user` com senha `postgres`.

Se não tiver, rode os seguintes comandos no terminal como usuário `postgres`):

docker run -d --name postgres-marketplace -e POSTGRES_USER=marketplace_user -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=marketplace_db -p 5432:5432 -v postgres-marketplace-data:/var/lib/postgresql/data postgres