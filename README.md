#  Academia API - Spring Boot

API REST desenvolvida para gerenciamento de uma academia, com autenticação, controle de usuários, mensalidades e registro de acessos.

O projeto foi desenvolvido utilizando **Java + Spring Boot**, aplicando boas práticas como DTOs, Services, Mappers, validações, segurança com JWT e containerização com Docker.

---

##  Tecnologias utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Security
* JWT (JSON Web Token)
* Spring Data JPA / Hibernate
* Bean Validation
* Lombok
* Maven

### Banco de dados

* MySQL / MariaDB

### Infraestrutura

* Docker
* Docker Compose

### Ferramentas

* IntelliJ IDEA
* Postman
* Git e GitHub

---

#  Funcionalidades

##  Autenticação e Segurança

* Login utilizando JWT
* Controle de acesso por Roles
* Proteção de endpoints com Spring Security
* Criptografia de senha utilizando BCrypt

Perfis disponíveis:

```
ROLE_ADMIN
ROLE_USER
ROLE_FUNCIONARIO
```

---

#  Usuários

Funcionalidades:

* Cadastro de usuários
* Atualização de dados
* Consulta de usuários
* Controle de permissões
* Associação com mensalidades e acessos

---

#  Mensalidades

O sistema possui gerenciamento de mensalidades:

* Criação de mensalidade
* Atualização de plano
* Pagamento de mensalidade
* Geração automática de próxima mensalidade
* Controle de status

Status possíveis:

```
PENDENTE
PAGA
ATRASADA
```

Regras implementadas:

* Usuário não pode possuir múltiplas mensalidades ativas
* Apenas mensalidades pendentes ou atrasadas podem ser pagas
* Após pagamento, uma nova mensalidade pode ser gerada automaticamente

---

#  Controle de acesso à academia

Sistema responsável por registrar entradas dos alunos.

Funcionalidades:

* Validação de usuário
* Verificação de senha
* Conferência de mensalidade ativa
* Controle de dias de acesso semanal

Exemplo:

Um aluno que possui plano de 3 dias por semana terá seu acesso limitado conforme sua mensalidade.

---

#  Arquitetura do projeto

Estrutura utilizada:

```
src/main/java/com/academia/auth

├── Config
│   ├── SecurityConfig
│   └── CorsConfig
│
├── Controllers
│
├── DTOS
│
├── Entities
│
├── Exceptions
│
├── Mappers
│
├── Repositories
│
├── Services
│
└── Schedulers
```

---

#  Executando com Docker

## Pré-requisitos

Instale:

* Docker
* Docker Compose

---

## Configuração

Crie um arquivo:

```
.env
```

baseado no exemplo:

```
.env.docker.example
```

Configure as variáveis:

```
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
JWT_SECRET=
```

---

## Subir os containers

Execute:

```bash
docker compose up -d
```

A aplicação será iniciada juntamente com o banco de dados.

---

#  Endpoints principais

## Autenticação

```
POST /auth/login
POST /auth/register
```

---

## Usuários

```
GET /usuarios
GET /usuarios/{id}
PUT /usuarios/{id}
DELETE /usuarios/{id}
```

---

## Mensalidades

```
POST /mensalidades
GET /mensalidades
PUT /mensalidades/{id}/pagar
```

---

## Acesso

```
POST /acesso
```

---

#  Documentação da API

A API pode ser testada utilizando:

* Postman
* Insomnia

---

#  Variáveis de ambiente

O projeto utiliza variáveis de ambiente para proteger informações sensíveis.

Exemplo:

```
JWT_SECRET=
DATABASE_PASSWORD=
DATABASE_USERNAME=
```

Nunca envie o arquivo `.env` para o GitHub.

---

#  Docker

Arquivos relacionados:

```
Dockerfile
docker-compose.yml
.dockerignore
.env.docker.example
```

O Docker é utilizado para facilitar a execução do backend e banco de dados em diferentes ambientes.

---

#  Regras de negócio implementadas

* Controle de permissões por cargo
* Controle de pagamento de mensalidades
* Restrição de acesso sem mensalidade válida
* Controle de dias de treino
* Criptografia de senhas
* Tratamento de exceções personalizadas

---

# 👨 Autor

**Saulin Samuel**

Desenvolvedor em formação com foco em:

* Java
* Spring Boot
* APIs REST
* Banco de dados
* Docker
* Desenvolvimento Full Stack

---

#  Próximas melhorias

* Implementação de testes automatizados
* Documentação com Swagger/OpenAPI
* Integração com gateway de pagamento
* Melhorias no painel administrativo
* Deploy em ambiente cloud
