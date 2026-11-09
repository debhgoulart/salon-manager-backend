# Salon Manager — Backend API

> Sistema robusto e escalável para gestão integral de salões de beleza, barbearias e centros de estética.  
> Desenvolvido com **Java 21**, **Spring Boot 3.3.5**, **PostgreSQL 16**, **Flyway**, **Spring Security 6 (JWT)** e **Docker**.

---

## 🚀 1. Visão Geral da Arquitetura

O **Salon Manager Backend** foi projetado seguindo princípios de arquitetura limpa, separação rigorosa de domínios em módulos coesos e total aderência às boas práticas corporativas:
- **Segurança Stateless:** Autenticação e autorização via JWT (JJWT) com tokens de acesso (2h) e refresh tokens (7 dias).
- **Rigor Financeiro:** Todos os cálculos contábeis utilizam estritamente `BigDecimal` com escala de 2 casas decimais e arredondamento `RoundingMode.HALF_EVEN` (Banqueiro).
- **Concorrência Otimista:** Entidades críticas como `Appointment` utilizam versionamento `@Version` para blindagem contra sobrescritas simultâneas (*lost updates*).
- **Consistência Contábil:** Bloqueio transacional de períodos (`ClosedPeriodException`) impedindo modificações retrospectivas em meses com fechamento contábil encerrado (`CLOSED`).
- **Padrão de Respostas de Erro:** Implementação integral da **RFC 7807** (*Problem Details for HTTP APIs*).

---

## 🛠️ 2. Stack Tecnológica

| Componente | Tecnologia | Versão |
|---|---|---|
| **Linguagem** | Java | 21 LTS |
| **Framework** | Spring Boot | 3.3.5 |
| **Banco de Dados** | PostgreSQL | 16-alpine |
| **Migrações de BD** | Flyway | Integrado |
| **Segurança** | Spring Security + JJWT | 0.12.6 |
| **Documentação API** | Springdoc OpenAPI (Swagger UI) | 2.6.0 |
| **Mapeamento DTO** | MapStruct | 1.6.2 |
| **Testes** | JUnit 5 + Mockito + MockMvc + H2 | 5.x |
| **Conteinerização** | Docker & Docker Compose | Multi-stage |

---

## 📦 3. Módulos do Sistema

1. **`auth` (Segurança & Acesso):** Gestão de usuários, papéis (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_RECEPTIONIST`, `ROLE_PROFESSIONAL`), login, refresh token e auto-provisionamento de administrador padrão via `CommandLineRunner`.
2. **`employee` (Profissionais):** Cadastro de colaboradores, taxas de comissão padrão e desativação lógica (soft-delete).
3. **`client` (Clientes):** Cadastro de clientes, contatos, histórico e busca textual paginada.
4. **`service` (Catálogo de Serviços):** Definição de serviços, duração em minutos, preço e taxa de comissão customizada por serviço.
5. **`appointment` (Motor de Agendamento):** Gestão da agenda com cálculo automatizado de `endTime`, validação rigorosa de colisão de horários, máquina de estados (`AGENDADO` -> `CONFIRMADO` -> `EM_ATENDIMENTO` -> `CONCLUIDO` / `CANCELADO` / `NAO_COMPARECEU`) e cálculo de slots livres (`AvailabilityService`).
6. **`commission` (Motor de Comissões):** Cálculo automatizado na conclusão de agendamentos aplicando precedência (taxa do serviço > taxa do colaborador) e gravação de snapshots imutáveis.
7. **`financial` (Gestão Financeira):** Registro de lançamentos de receita (automático ou manual) e despesas operacionais com proteção de fechamento mensal.
8. **`closing` (Fechamento Mensal):** Consolidação atômica de faturamento, despesas e comissões por colaborador com travas de integridade e reabertura com log de auditoria.
9. **`dashboard` (KPIs & Relatórios):** Visão executiva em tempo real com faturamento total, despesas, lucro líquido, ranking dos serviços mais lucrativos e próximos agendamentos da agenda.

---

## 🗄️ 4. Estrutura do Banco de Dados

O banco de dados utiliza chaves primárias em **UUID v4** e índices estratégicos para suportar alto volume de agendamentos e concorrência:

```
 users (id [PK], email [UK], password, role, active, timestamps)
   │
   ├─────── employees (id [PK], name, email, phone, cpf [UK], default_commission_rate, active)
   │           │
   │           ├─────── appointments (id [PK], employee_id [FK], client_id [FK], service_id [FK],
   │           │           │          start_time, end_time, price, status, version)
   │           │           │
   │           │           └─────── commissions (id [PK], appointment_id [FK, UK], employee_id [FK],
   │           │                                  service_amount, applied_rate, commission_amount, status)
   │           │
   │           └─────── monthly_closing_items (id [PK], monthly_closing_id [FK], employee_id [FK],
   │                                           total_service_value, total_commission, appointments_count)
   │
   ├─────── clients (id [PK], name, email, phone, cpf, notes, active)
   ├─────── services (id [PK], name, price, duration_minutes, custom_commission_rate, active)
   ├─────── financial_transactions (id [PK], type, category, description, amount, payment_method, status)
   └─────── monthly_closings (id [PK], year, month, total_revenue, total_expenses, total_commissions, net_profit, status)
```

---

## ⚡ 5. Executando a Aplicação

### Pré-requisitos
- **Java 21** e **Maven 3.9+** (para execução local)
- **Docker** e **Docker Compose** (para execução conteinerizada)

---

### Opção A: Execução via Docker Compose (Recomendado)

Suba o ecossistema completo (PostgreSQL 16 + Backend) com um único comando:

```bash
docker compose up --build -d
```

O backend aguardará o *healthcheck* do PostgreSQL e iniciará na porta `8080`.

Para acompanhar os logs:
```bash
docker compose logs -f backend
```

Para encerrar o ambiente:
```bash
docker compose down
```

---

### Opção B: Execução Local com Maven

1. Inicie uma instância do PostgreSQL (ou use o serviço do compose):
   ```bash
   docker compose up -d postgres
   ```
2. Configure as variáveis de ambiente (ou copie `.env.example` para `.env`):
   ```bash
   cp .env.example .env
   ```
3. Compile e inicie o Spring Boot:
   ```bash
   mvn spring-boot:run
   ```

---

## 🔑 6. Credenciais Iniciais de Administrador (Seed)

Na primeira inicialização, a aplicação cria automaticamente um administrador padrão caso não exista nenhum usuário cadastrado:

- **E-mail:** `admin@salonmanager.com`
- **Senha:** `admin@123456`
- **Papel:** `ROLE_ADMIN`

---

## 📚 7. Documentação da API & Contrato Frontend

- **Swagger UI Interativo:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`
- **Contrato de Integração Vue.js:** Consulte o guia completo com interfaces TypeScript e interceptores em [`docs/frontend-integration-contract.md`](docs/frontend-integration-contract.md).

---

## 🧪 8. Testes Automatizados

Para executar a suíte completa de testes unitários e de integração:

```bash
mvn clean test
```

Para gerar o pacote executável (JAR) validando todos os testes:
```bash
mvn clean package
```

---

## 📄 9. Licença

Este projeto está sob licença proprietária de uso exclusivo para **Salon Manager**.
