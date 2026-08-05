#  Academia API - Spring Boot

Uma API REST desenvolvida com **Java + Spring Boot** para gerenciamento completo de uma academia.

O projeto foi criado com foco em **boas práticas de desenvolvimento Backend**, utilizando arquitetura em camadas, autenticação JWT, controle de permissões, DTOs, Mappers, Specifications, Docker e regras de negócio que simulam um sistema real de academias.

---

#  Tecnologias Utilizadas

## Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* JWT (JSON Web Token)
* Bean Validation
* Lombok
* Maven

## Banco de Dados

* MySQL / MariaDB

## Infraestrutura

* Docker
* Docker Compose

## Ferramentas

* IntelliJ IDEA/VS CODE
* Postman
* Git
* GitHub

---

#  Arquitetura

O projeto segue uma arquitetura organizada em camadas.

```text
src/main/java/com/academia/auth

├── Config
├── Controllers
├── DTOS
├── Entities
├── Exceptions
├── Mappers
├── Repositories
├── Schedulers
├── Security
├── Services
├── Specifications
└── Utils
```

---

#  Segurança

A autenticação da API é realizada utilizando **JWT**.

Funcionalidades implementadas:

* Login com Token JWT
* Criptografia de senhas utilizando BCrypt
* Controle de acesso por Roles
* Proteção de endpoints com Spring Security
* Autorização baseada em perfis

Perfis disponíveis:

```text
ROLE_ADMIN
ROLE_FUNCIONARIO
ROLE_USER
```

---

# 👤 Usuários

Funcionalidades disponíveis:

* Cadastro de usuários
* Atualização de dados
* Exclusão
* Consulta individual
* Consulta paginada
* Controle de permissões
* Associação com mensalidades
* Associação com acessos

---

#  Mensalidades

Sistema responsável pelo gerenciamento financeiro dos alunos.

Funcionalidades:

* Criar mensalidade
* Atualizar plano
* Realizar pagamento
* Consultar mensalidades
* Controle automático de status

Status disponíveis:

```text
PENDENTE
PAGA
ATRASADA
```

### Regras de negócio

* Um usuário não pode possuir múltiplas mensalidades ativas.
* Apenas mensalidades pendentes ou atrasadas podem ser pagas.
* O acesso à academia depende de uma mensalidade válida.

---

# 🚪 Controle de Acesso

O sistema controla o acesso semanal dos alunos à academia.

Funcionalidades:

* Validação do usuário
* Validação de senha
* Verificação da mensalidade
* Controle de dias permitidos por semana
* Registro dos acessos

Exemplo:

Um aluno com plano de **3 dias por semana** terá sua entrada limitada a três acessos durante a semana vigente.

---

#  Advertências

O sistema possui gerenciamento completo de advertências.

Funcionalidades:

* Criar advertência
* Atualizar advertência
* Consultar advertências
* Excluir advertências
* Controle de permissões para exclusão

---

#  Histórico de Advertências

Antes que uma advertência seja excluída, ela é automaticamente registrada em um histórico.

As informações armazenadas incluem:

* Remetente
* Destinatário
* Usuário responsável pela exclusão
* Nível da advertência
* Mensagem
* Data de criação
* Data de expiração
* Data da exclusão

Esse histórico permite auditoria e rastreabilidade das advertências removidas.

---

#  Filtros Dinâmicos (Specifications)

O projeto utiliza **Spring Data JPA Specifications**, permitindo pesquisas flexíveis sem necessidade de criar diversos métodos no Repository.

É possível combinar filtros como:

* Remetente
* Destinatário
* Usuário que realizou a exclusão
* Nível da advertência
* Período de exclusão

Todos os filtros são opcionais e podem ser utilizados em conjunto.

---

#  Paginação

As consultas utilizam paginação através do Spring Data.

Exemplo:

```text
?page=0&size=10&sort=remetente
```

---

#  Executando com Docker

## Pré-requisitos

* Docker
* Docker Compose

---

## Configuração

Crie um arquivo:

```text
.env
```

Baseado em:

```text
.env.docker.example
```

Configure:

```text
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
JWT_SECRET=
```

---

## Executando

```bash
docker compose up -d
```

A aplicação e o banco serão iniciados automaticamente.

---

#  Principais Endpoints

## Autenticação

```http
POST /auth/login
POST /auth/register
```

---

## Usuários

```http
GET    /usuarios
GET    /usuarios/{id}
POST   /usuarios
PUT    /usuarios/{id}
DELETE /usuarios/{id}
```

---

## Mensalidades

```http
POST /mensalidades
GET  /mensalidades
PUT  /mensalidades/{id}/pagar
```

---

## Controle de Acesso

```http
POST /acesso
```

---

## Advertências

```http
POST   /advertencias
GET    /advertencias
PUT    /advertencias/{id}
DELETE /advertencias/{id}
```

---

## Histórico de Advertências

```http
GET /historico-advertencias
```

Filtros disponíveis:

* remetente
* destinatario
* excluidoPor
* nivelAdvertencia
* inicio
* fim

Todos opcionais.

---

#  Boas Práticas Aplicadas

* Arquitetura em camadas
* DTO Pattern
* Mapper Pattern
* Repository Pattern
* Specification Pattern
* Paginação
* Bean Validation
* Tratamento global de exceções
* JWT Authentication
* Spring Security
* Docker
* Variáveis de ambiente
* Separação de responsabilidades
* Regras de negócio centralizadas nos Services

---

#  Variáveis de Ambiente

O projeto utiliza variáveis de ambiente para proteger informações sensíveis.

Exemplo:

```text
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=
JWT_SECRET=
```

Nunca envie o arquivo `.env` para o GitHub.

---

#  Roadmap

Próximas funcionalidades planejadas:

* Sistema de Treinos
* Avaliação Física
* Dashboard Administrativo
* Recuperação de Senha por E-mail
* Controle de Entrada via QR Code
* Testes Unitários e de Integração
* RabbitMQ
* Redis
* CI/CD com GitHub Actions
* Monitoramento com Spring Boot Actuator
* Prometheus
* Grafana

---

#  Autor

**Saulo Samuel**

Desenvolvedor Backend Java com foco em:

* Java
* Spring Boot
* APIs REST
* Spring Security
* Banco de Dados
* Docker
* Arquitetura de Software
* Desenvolvimento Full Stack

---

 Este projeto está em constante evolução com o objetivo de aplicar conceitos utilizados em sistemas reais e aprofundar conhecimentos em desenvolvimento Backend com Java e Spring Boot.
