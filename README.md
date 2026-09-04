# Academia API

API REST para gerenciamento completo de uma academia — alunos, instrutores, mensalidades, controle de acesso, aulas, agendamentos e advertências — construída com **Java 21 + Spring Boot 4**.

O projeto foi desenvolvido do zero com foco em práticas usadas em times profissionais de backend: arquitetura em camadas, autenticação stateless com JWT, autorização granular por papel, tratamento centralizado de erros, migrações versionadas de banco, testes automatizados em múltiplas camadas e uma esteira de CI/CD que builda, testa e faz deploy da aplicação a cada push.

**API em produção:** [academia-api-3wmy.onrender.com](https://academia-api-3wmy.onrender.com)
> Hospedada no plano gratuito do Render — a primeira requisição após um período de inatividade pode levar alguns segundos a mais para responder (cold start).

---

## Índice

- [Stack técnica](#stack-técnica)
- [Arquitetura](#arquitetura)
- [Domínio da aplicação](#domínio-da-aplicação)
- [Segurança](#segurança)
- [Endpoints](#endpoints)
- [Regras de negócio](#regras-de-negócio)
- [Testes](#testes)
- [CI/CD](#cicd)
- [Como rodar localmente](#como-rodar-localmente)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Roadmap](#roadmap)

---

## Stack técnica

| Categoria | Tecnologia |
|---|---|
| Linguagem / Runtime | Java 21 |
| Framework | Spring Boot 4 |
| Segurança | Spring Security + JWT (jjwt) |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | MySQL 8.4 |
| Migrações | Flyway |
| Testes | JUnit 5, Mockito, AssertJ, Testcontainers, H2 |
| Rate limiting | Bucket4j |
| Build | Maven |
| Containerização | Docker + Docker Compose (build multi-stage) |
| CI/CD | GitHub Actions (build, testes, imagem Docker, deploy) |
| Registro de imagens | GitHub Container Registry (GHCR) |
| Deploy | Render (serviço orientado a imagem Docker) |

---

## Arquitetura

O projeto segue uma arquitetura em camadas, separando claramente responsabilidades de apresentação, regra de negócio e persistência:

```
src/main/java/com/academia/auth
├── Config          → segurança, CORS, relógio injetável (testabilidade)
├── Controllers      → camada HTTP, validação de entrada, delega para Services
├── DTOS             → contratos de entrada/saída, desacoplados das entidades
├── Events           → eventos de domínio publicados via ApplicationEventPublisher
├── Exceptions        → exceções de negócio + handler global
├── Filters          → rate limiting por IP
├── Listeners        → reagem a eventos (ex: gerar histórico ao excluir)
├── Mappers          → conversão entre Entidade ↔ DTO
├── Models           → entidades JPA
├── Repositories      → acesso a dados (Spring Data JPA)
├── Schedulers        → jobs agendados (@Scheduled)
├── Security          → filtro JWT, geração/validação de token, UserDetailsService
├── Services          → regras de negócio
├── Specifications     → filtros dinâmicos e combináveis (Spring Data Specification)
└── Utils             → utilitários gerais
```

**Decisões de design relevantes:**

- **Eventos de domínio em vez de acoplamento direto entre Services.** Ações como gerar histórico de advertência excluída, ou reagir a uma mudança de status de mensalidade, são feitas via `ApplicationEventPublisher` + `@TransactionalEventListener`, evitando que um Service dependa diretamente de outro só para efeitos colaterais.
- **Specifications para busca dinâmica.** Em vez de multiplicar métodos de repositório para cada combinação de filtro, os endpoints de pesquisa combinam critérios opcionais (`Specification<T>`) que são compostos em tempo de execução.
- **`Clock` injetável como bean.** Toda lógica sensível a tempo (agendamentos, expiração de advertência, vencimento de mensalidade) depende de um `Clock` gerenciado pelo Spring, e não de `LocalDateTime.now()` direto — isso torna o comportamento determinístico e testável.
- **DTOs em todas as bordas.** Nenhuma entidade JPA é exposta diretamente pela API; toda entrada e saída passa por um DTO dedicado e um Mapper.

---

## Domínio da aplicação

| Entidade | Responsabilidade |
|---|---|
| `Usuario` | Alunos, instrutores, funcionários e administradores. Papel definido por `RoleUser`. |
| `Mensalidade` | Plano financeiro do aluno — status (`PENDENTE`, `PAGA`, `CANCELADA`, `ATRASADA`), controle de dias de acesso semanal. |
| `HistoricoMensalidade` | Registro de auditoria de mudanças de status da mensalidade. |
| `AcessoAcademia` | Registro de entrada do aluno/funcionário na academia. |
| `Aula` | Aula ministrada por um instrutor, com status próprio (`PENDENTE`, `CONFIRMADA`, `CONCLUIDA`, `CANCELADA`). |
| `Agendamento` | Inscrição de um aluno em uma aula. |
| `Advertencia` | Advertência emitida entre usuários (ex: instrutor → aluno), com nível de gravidade. |
| `HistoricoAdvertencia` | Auditoria de advertências excluídas, preservando remetente, destinatário e responsável pela exclusão. |
| `AvaliacaoFisica` | Registro de avaliação física de um aluno. |

---

## Segurança

Autenticação **stateless** via JWT — sem sessão em servidor, o token carrega a identidade e o papel do usuário a cada requisição.

- **BCrypt** para hash de senha.
- **Filtro JWT** (`JwtAuthenticationFilter`) intercepta cada requisição, valida o token e popula o contexto de segurança do Spring antes de qualquer `Controller`.
- **Autorização por papel** aplicada método a método com `@PreAuthorize`, não apenas no nível de rota — cada operação declara explicitamente quais papéis podem executá-la.
- **Rate limiting** por IP (Bucket4j): 10 requisições, com reposição de 1 a cada 6 segundos. Ao exceder o limite, a API responde `429 Too Many Requests` com header `Retry-After`.
- **CORS** configurado de forma explícita, não com wildcard aberto.

**Papéis (`RoleUser`):**

| Papel | Descrição |
|---|---|
| `ROLE_USER` | Aluno |
| `ROLE_INSTRUTOR` | Instrutor — cria e gerencia aulas |
| `ROLE_FUNCIONARIO` | Funcionário — operação do dia a dia (mensalidades, acessos, advertências) |
| `ROLE_ADMIN` | Administrador — inclui promoção/rebaixamento de usuários |

Rotas públicas: apenas `/auth/register`, `/auth/login` e `/actuator/health`. Todo o restante exige token JWT válido, e a maioria das operações exige ainda um papel específico.

---

## Endpoints

Visão geral dos recursos principais. Cada endpoint abaixo é protegido por um ou mais papéis via `@PreAuthorize` (ver código-fonte dos Controllers para a regra exata de cada rota).

### Autenticação — `/auth`
| Método | Rota | Acesso |
|---|---|---|
| POST | `/auth/register` | Público |
| POST | `/auth/login` | Público |

### Usuários — `/usuario`
| Método | Rota | Acesso |
|---|---|---|
| GET | `/usuario/me` | Autenticado |
| PUT | `/usuario/atualizar` | Autenticado |
| DELETE | `/usuario` | Autenticado |
| GET | `/usuario/listar` | Admin, Funcionário, Instrutor |
| GET | `/usuario/pesquisar` | Admin, Funcionário, Instrutor |
| PATCH | `/usuario/{id}/promover-funcionario` | Admin |
| PATCH | `/usuario/{id}/rebaixar-usuario` | Admin |

### Mensalidades — `/mensalidade`
| Método | Rota | Acesso |
|---|---|---|
| POST | `/mensalidade` | Autenticado |
| PUT | `/mensalidade/atualizar` | Autenticado |
| GET | `/mensalidade/me` | Autenticado |
| PATCH | `/mensalidade/pagar` | Autenticado |
| PATCH | `/mensalidade/cancelar` | Autenticado |
| DELETE | `/mensalidade/deletar` | Autenticado |
| GET | `/mensalidade/buscar` | Admin, Funcionário, Instrutor |
| GET | `/mensalidade/pesquisar` | Admin, Funcionário, Instrutor |

### Controle de acesso — `/acesso`
| Método | Rota | Acesso |
|---|---|---|
| GET | `/acesso/me` | Autenticado |
| POST | `/acesso/alunos` | Admin, Funcionário, Instrutor |
| POST | `/acesso/funcionarios` | Admin, Funcionário, Instrutor |
| GET | `/acesso/buscar` | Admin, Funcionário, Instrutor |
| GET | `/acesso/pesquisar` | Admin, Funcionário, Instrutor |

### Aulas — `/aula`
| Método | Rota | Acesso |
|---|---|---|
| POST | `/aula` | Instrutor |
| PUT | `/aula` | Instrutor |
| PATCH | `/aula/confirmar` | Instrutor |
| PATCH | `/aula/cancelar` | Instrutor |
| GET | `/aula/me` | Instrutor |
| DELETE | `/aula/{id}/excluir` | Instrutor |
| GET | `/aula/buscar/todas` | Autenticado |
| GET | `/aula/{id}/buscar` | Autenticado |

### Agendamentos — `/agendamento`
| Método | Rota | Acesso |
|---|---|---|
| POST | `/agendamento/{id}/criar` | Aluno |
| DELETE | `/agendamento/{id}/cancelar` | Aluno |
| GET | `/agendamento/buscar/me` | Instrutor |
| GET | `/agendamento/buscar/todos` | Admin, Funcionário, Instrutor |
| GET | `/agendamento/{id}/buscar` | Autenticado |

### Advertências — `/advertencia`
| Método | Rota | Acesso |
|---|---|---|
| POST | `/advertencia/{id}/enviar` | Admin, Funcionário, Instrutor |
| GET | `/advertencia/pesquisar` | Admin, Funcionário, Instrutor |
| GET | `/advertencia/{id}/buscar` | Admin, Funcionário, Instrutor |
| GET | `/advertencia/recebidas/me` | Autenticado |
| GET | `/advertencia/enviadas/me` | Admin, Funcionário, Instrutor |
| DELETE | `/advertencia/{id}/deletar` | Admin, Funcionário, Instrutor |

### Histórico — `/historico`, `/historico-acesso`, `/historico-mensalidade`
| Método | Rota | Acesso |
|---|---|---|
| GET | `/historico` | Admin, Funcionário |
| GET | `/historico/{id}/buscar` | Admin, Funcionário |
| GET | `/historico-acesso` | Admin, Funcionário |
| GET | `/historico-acesso/{id}/buscar` | Admin, Funcionário |
| GET | `/historico-mensalidade` | Admin, Funcionário |

### Avaliação física — `/avaliacao-fisica`
| Método | Rota | Acesso |
|---|---|---|
| POST | `/avaliacao-fisica/{id}` | Admin, Funcionário, Instrutor |
| PUT | `/avaliacao-fisica/{id}` | Admin, Funcionário, Instrutor |
| GET | `/avaliacao-fisica/me` | Admin, Funcionário, Aluno |
| GET | `/avaliacao-fisica/{id}/buscar` | Admin, Funcionário, Instrutor |
| GET | `/avaliacao-fisica/pesquisar` | Admin, Funcionário, Instrutor |
| DELETE | `/avaliacao-fisica/{id}` | Admin, Funcionário, Instrutor |

### Dashboard — `/dashboard`
| Método | Rota | Acesso |
|---|---|---|
| GET | `/dashboard/buscar` | Admin, Funcionário |

Retorna indicadores agregados: quantidade de alunos, funcionários, mensalidades por status, faturamento total e acessos na semana.

**Pesquisas e listagens são paginadas** via Spring Data (`?page=0&size=10&sort=campo`), e os endpoints de "pesquisar" aceitam filtros combináveis via Specifications — todos opcionais.

---

## Regras de negócio

- Um usuário não pode ter mais de uma mensalidade ativa simultaneamente.
- Apenas mensalidades com status `PENDENTE` ou `ATRASADA` podem ser marcadas como pagas.
- O acesso à academia depende de uma mensalidade válida e do controle de dias semanais contratados no plano.
- Mensalidades em atraso são identificadas automaticamente por um `Scheduler`.
- Aulas passam por transições de status controladas (`PENDENTE` → `CONFIRMADA` → `CONCLUIDA`/`CANCELADA`), com jobs agendados encerrando ou cancelando aulas conforme a data/horário.
- Ao excluir uma advertência, seus dados são preservados automaticamente em `HistoricoAdvertencia` antes da remoção — garantindo rastreabilidade mesmo após exclusão.
- Verificações de posse (ex: um usuário só pode alterar a própria mensalidade/agendamento) são feitas de forma fail-fast no início da regra de negócio, prevenindo acesso indevido a dados de terceiros (IDOR).

---

## Testes

O projeto conta com **mais de 200 testes automatizados**, cobrindo diferentes camadas da aplicação:

- **Testes unitários** de `Services`, com Mockito e AssertJ, isolando a lógica de negócio de infraestrutura.
- **Testes de fatia (`@WebMvcTest`, `@DataJpaTest`)** para validar o comportamento de Controllers e Repositories de forma isolada.
- **Testes de integração com Testcontainers**, subindo um container real de MySQL para validar o comportamento completo com o banco de dados, incluindo migrações Flyway.
- **Autenticação de teste customizada**, já que o projeto usa um `UserDetails` próprio (`Usuario`), incompatível com `@WithMockUser` padrão — testes de segurança usam autenticação customizada equivalente.
- **Tempo determinístico**: testes que dependem de datas/horários usam um `Clock` mockado, evitando flakiness.

Rodar a suíte completa:

```bash
./mvnw verify
```

---

## CI/CD

O projeto tem uma esteira de integração e entrega contínua real, não apenas simbólica:

**CI (`ci.yml`)** — roda a cada push/PR para `main` e `develop`:
1. Checkout do código
2. Setup do Java 21 com cache do Maven
3. `./mvnw verify` — compila e executa toda a suíte de testes

**CD (`cd.yml`)** — roda a cada push em `main`, após o build passar:
1. Executa os testes novamente como porta de qualidade
2. Builda a imagem Docker da aplicação
3. Publica a imagem no GitHub Container Registry (GHCR), versionada pelo SHA do commit
4. Dispara um deploy no Render apontando explicitamente para essa imagem
5. **Monitora o status do deploy via API do Render** e só marca o pipeline como concluído com sucesso quando o serviço reporta status `live` — se o deploy falhar do lado do Render, o pipeline falha também, mesmo que os testes tenham passado

Ou seja: um push em `main` só chega a produção se passar em testes **e** subir corretamente no provedor de hospedagem — o pipeline não "dispara e torce".

---

## Como rodar localmente

### Pré-requisitos
- Docker e Docker Compose

### Passos

```bash
git clone https://github.com/SaulinSamuel/Spring-Boot-Academia-.git
cd Spring-Boot-Academia-
cp .env.docker.example .env
```

Edite o `.env` preenchendo `DB_PASSWORD` e `JWT_SECRET` (uma chave base64 de pelo menos 256 bits — pode gerar com `openssl rand -base64 32`).

```bash
docker compose up -d
```

Isso sobe o MySQL e a API juntos, com a API aguardando o banco ficar saudável antes de iniciar. A aplicação fica disponível em `http://localhost:8080`.

### Rodando sem Docker (dev local)

1. Suba um MySQL acessível e configure um `.env` baseado em `.env.example`.
2. Rode com o profile de desenvolvimento:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DATABASE_URL` | URL JDBC do banco MySQL |
| `DB_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `JWT_SECRET` | Chave secreta para assinatura dos tokens JWT (base64, 256+ bits) |
| `SPRING_PROFILES_ACTIVE` | Profile ativo (`dev` ou `prod`) |

O arquivo `.env` nunca é versionado — use `.env.example` (execução local) ou `.env.docker.example` (Docker Compose) como referência.

---

## Roadmap

- [ ] Documentação interativa da API (OpenAPI/Swagger)
- [ ] Recuperação de senha por e-mail
- [ ] Controle de entrada via QR Code
- [ ] Cache com Redis para consultas de leitura frequente
- [ ] Métricas customizadas expostas via Actuator/Prometheus

---

## Autor

**Saulo Samuel**
Desenvolvedor Backend em formação, com foco em Java, Spring Boot e arquitetura de APIs REST.