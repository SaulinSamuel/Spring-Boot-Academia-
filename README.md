#  Academia API

API REST desenvolvida em **Java + Spring Boot** para gerenciamento de academias, permitindo autenticação de usuários, controle de mensalidades e gerenciamento do acesso dos alunos.

##  Tecnologias

- Java 21
- Spring Boot 4
- Spring Security
- JWT (JSON Web Token)
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Lombok
- Bean Validation

---

#  Funcionalidades

##  Autenticação

- Login com JWT
- Autenticação Stateless
- Controle de permissões por Roles

### Perfis

- ROLE_ADMIN
- ROLE_FUNCIONARIO
- ROLE_USER

---

##  Usuários

- Cadastro de usuários
- Atualização de dados
- Exclusão
- Buscar usuário logado
- Controle de permissões

---

##  Mensalidades

- Criação automática de mensalidades
- Pagamento de mensalidade
- Controle de vencimento
- Histórico de mensalidades
- Alteração da mensalidade
- Regras de negócio para impedir pagamentos inválidos

---

##  Controle de Acesso

- Registrar entrada na academia
- Controle semanal de acessos
- Buscar acesso do usuário logado
- Consulta de acessos
- Pesquisa de usuários por nome

---

##  Busca Inteligente

A API possui busca por nome utilizando pesquisa parcial.

Exemplo:

```
GET /academia/pesquisar?nome=jo
```

Resultado:

- João
- José
- João Paulo

Ideal para integração com pesquisas em tempo real no frontend.

---

#  Segurança

A autenticação é realizada através de JWT.

Exemplo:

```
Authorization: Bearer SEU_TOKEN
```

Todas as rotas protegidas utilizam Spring Security.

---

#  Estrutura do Projeto

```
src
├── Config
├── Controllers
├── DTOS
├── Exceptions
├── Mappers
├── Models
├── Repositories
├── Security
├── Services
└── Utils
```

---

#  Configuração

Clone o projeto

```bash
git clone https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git
```

Entre na pasta

```bash
cd auth
```

Configure o arquivo `.env`

Exemplo:

```
DB_URL=
DB_USERNAME=
DB_PASSWORD=

JWT_SECRET=
```

Execute

```bash
mvn spring-boot:run
```

A API ficará disponível em

```
http://localhost:8080
```

---

#  Principais Endpoints

## Autenticação

| Método | Endpoint |
|---------|----------|
| POST | /auth/login |
| POST | /auth/register |

---

## Usuários

| Método | Endpoint |
|---------|----------|
| GET | /usuarios/me |
| PUT | /usuarios |
| DELETE | /usuarios |

---

## Mensalidades

| Método | Endpoint |
|---------|----------|
| POST | /mensalidades |
| PUT | /mensalidades/pagar |
| GET | /mensalidades |

---

## Academia

| Método | Endpoint |
|---------|----------|
| POST | /academia/entrar |
| GET | /academia/buscar |
| GET | /academia/pesquisar?nome= |

---

#  Regras de Negócio

- Apenas usuários autenticados podem acessar recursos protegidos.
- Funcionários possuem permissões específicas.
- Administradores possuem acesso total.
- O controle de acesso considera os dias permitidos por semana.
- Mensalidades vencidas ou pendentes seguem regras específicas para pagamento.

---

#  Melhorias Futuras

- Dashboard administrativo
- Relatórios
- Notificações
- Upload de foto do usuário
- Docker
- RabbitMQ
- Testes automatizados
- Documentação Swagger/OpenAPI

---

#  Desenvolvedor

Projeto desenvolvido por **Saulo** utilizando Java e Spring Boot como prática de desenvolvimento backend e arquitetura de APIs REST.

---

##  Objetivo

Este projeto tem como objetivo aplicar conceitos modernos de desenvolvimento backend, incluindo:

- Arquitetura em camadas
- Spring Security
- JWT
- JPA/Hibernate
- DTOs
- Mappers
- Tratamento de exceções
- Validações
- Regras de negócio
- APIs REST
