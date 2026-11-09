# SPECIFICATIONS — SALON MANAGER BACKEND
**Fase do Spec Kit:** `/speckit.specify`

Este documento detalha as especificações formais de requisitos funcionais, contratos de API (endpoints, parâmetros, payloads, status HTTP), regras de validação e modelo de persistência para o backend do **Salon Manager**.

---

## 1. Mapeamento de Domínio e Modelo Relacional

Todas as entidades herdam da classe base abstrata `BaseEntity`:
- `id` (UUID, Primary Key, gerada automaticamente)
- `createdAt` (OffsetDateTime / Instant, imutável)
- `updatedAt` (OffsetDateTime / Instant, atualizado em cada alteração)

### 1.1 Esquema Relacional de Banco de Dados (Flyway Migrations)

```sql
-- Resumo dos Schemas (DDL planejado)

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL, -- 'ADMIN', 'EMPLOYEE'
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL,
    role_title VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    default_commission_rate NUMERIC(5, 2) NOT NULL CHECK (default_commission_rate >= 0 AND default_commission_rate <= 100),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    registration_date DATE NOT NULL DEFAULT CURRENT_DATE,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    custom_commission_rate NUMERIC(5, 2) CHECK (custom_commission_rate IS NULL OR (custom_commission_rate >= 0 AND custom_commission_rate <= 100)),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    service_id UUID NOT NULL REFERENCES services(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    estimated_price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL, -- 'AGENDADO', 'CONFIRMADO', 'EM_ATENDIMENTO', 'CONCLUIDO', 'CANCELADO', 'NAO_COMPARECEU'
    notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_appointments_employee_period ON appointments (employee_id, start_time, end_time);
CREATE INDEX idx_appointments_client ON appointments (client_id);
CREATE INDEX idx_appointments_status ON appointments (status);

CREATE TABLE monthly_closings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_year INTEGER NOT NULL,
    reference_month INTEGER NOT NULL CHECK (reference_month BETWEEN 1 AND 12),
    gross_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0,
    extra_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_expenses NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_commissions NUMERIC(12, 2) NOT NULL DEFAULT 0,
    net_result NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL, -- 'CLOSED', 'REOPENED'
    closed_at TIMESTAMPTZ NOT NULL,
    closed_by VARCHAR(150) NOT NULL,
    audit_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_closing_year_month UNIQUE (reference_year, reference_month)
);

CREATE TABLE monthly_closing_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    monthly_closing_id UUID NOT NULL REFERENCES monthly_closings(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id),
    total_services_count INTEGER NOT NULL DEFAULT 0,
    total_gross_generated NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_commission_due NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE commissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID UNIQUE NOT NULL REFERENCES appointments(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    monthly_closing_id UUID REFERENCES monthly_closings(id),
    applied_rate NUMERIC(5, 2) NOT NULL,
    service_amount NUMERIC(12, 2) NOT NULL,
    commission_amount NUMERIC(12, 2) NOT NULL,
    salon_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- 'PENDING', 'PAID', 'CANCELLED'
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE financial_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID REFERENCES appointments(id),
    monthly_closing_id UUID REFERENCES monthly_closings(id),
    description VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL, -- 'SERVICO', 'PRODUTO', 'ALUGUEL', 'CONTAS', 'MATERIAL', 'OUTROS'
    amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    transaction_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'INCOME', 'EXPENSE'
    status VARCHAR(20) NOT NULL, -- 'ESTIMATED', 'PENDING', 'COMPLETED', 'CANCELLED'
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_fin_trans_date_type ON financial_transactions (transaction_date, type, status);
```

---

## 2. Especificação dos Endpoints REST por Módulo

### 2.1 Módulo: Autenticação e Usuários (`/api/auth`)

| Método | Endpoint | Descrição | Permissão | Payload Entrada | Resposta Sucesso | Erros Previstos |
|---|---|---|---|---|---|---|
| `POST` | `/api/auth/login` | Autentica usuário com email e senha | Pública | `LoginRequest` | `200 OK` (`AuthResponse`) | `401 Unauthorized`, `400 Bad Request` |
| `POST` | `/api/auth/register-admin` | Cadastra usuário administrador inicial | Restrito (ou primeiro boot) | `AdminRegisterRequest` | `201 Created` (`UserResponse`) | `409 Conflict` (email duplicado) |
| `POST` | `/api/auth/refresh` | Renova token JWT via refresh token | Pública | `RefreshTokenRequest` | `200 OK` (`AuthResponse`) | `401 Unauthorized` |
| `GET` | `/api/auth/me` | Retorna dados do usuário autenticado | Autenticado | N/A | `200 OK` (`UserResponse`) | `401 Unauthorized` |

#### Contratos DTO:
- **`LoginRequest`:**
  - `email`: String (NotBlank, Email)
  - `password`: String (NotBlank, Min: 6)
- **`AuthResponse`:**
  - `accessToken`: String
  - `refreshToken`: String
  - `tokenType`: "Bearer"
  - `expiresInSeconds`: Long (ex: 7200)
  - `user`: `UserSummaryResponse` (id, name, email, role)

---

### 2.2 Módulo: Funcionários (`/api/employees`)

| Método | Endpoint | Descrição | Permissão | Query Params | Resposta Sucesso |
|---|---|---|---|---|---|
| `POST` | `/api/employees` | Cadastra novo funcionário | `ADMIN` | - | `201 Created` (`EmployeeResponse`) |
| `GET` | `/api/employees` | Lista funcionários paginados | `ADMIN`, `EMPLOYEE` | `page, size, sort, active, name` | `200 OK` (`Page<EmployeeResponse>`) |
| `GET` | `/api/employees/{id}` | Busca funcionário por ID | `ADMIN`, `EMPLOYEE` | - | `200 OK` (`EmployeeResponse`) |
| `PUT` | `/api/employees/{id}` | Atualiza dados cadastrais | `ADMIN` | - | `200 OK` (`EmployeeResponse`) |
| `DELETE` | `/api/employees/{id}` | Desativação lógica (*soft delete*) | `ADMIN` | - | `204 No Content` |
| `GET` | `/api/employees/{id}/services-history` | Histórico de serviços prestados | `ADMIN`, `EMPLOYEE` (próprio) | `startDate, endDate, page, size` | `200 OK` (`Page<AppointmentSummaryResponse>`) |
| `GET` | `/api/employees/{id}/commissions` | Extrato de comissões | `ADMIN`, `EMPLOYEE` (próprio) | `startDate, endDate, status, page, size` | `200 OK` (`Page<CommissionResponse>`) |
| `PATCH` | `/api/employees/{id}/commission-rate` | Atualiza taxa padrão de comissão | `ADMIN` | - | `200 OK` (`EmployeeResponse`) |

#### Contratos DTO:
- **`EmployeeCreateRequest`:**
  - `name`: String (NotBlank, Max 150)
  - `cpf`: String (NotBlank, validação formato CPF)
  - `phone`: String (NotBlank)
  - `email`: String (NotBlank, Email)
  - `roleTitle`: String (NotBlank, e.g. "Cabeleireiro", "Manicure")
  - `hireDate`: LocalDate (NotNull, PastOrPresent)
  - `defaultCommissionRate`: BigDecimal (NotNull, DecimalMin 0.00, DecimalMax 100.00)
  - `createLoginUser`: Boolean (opcional, se true cria acesso ao sistema)
  - `password`: String (opcional, obrigatório se createLoginUser=true)

---

### 2.3 Módulo: Clientes (`/api/clients`)

| Método | Endpoint | Descrição | Permissão | Query Params | Resposta Sucesso |
|---|---|---|---|---|---|
| `POST` | `/api/clients` | Cadastra novo cliente | `ADMIN`, `EMPLOYEE` | - | `201 Created` (`ClientResponse`) |
| `GET` | `/api/clients` | Lista clientes paginados com filtro | `ADMIN`, `EMPLOYEE` | `query, active, page, size, sort` | `200 OK` (`Page<ClientResponse>`) |
| `GET` | `/api/clients/{id}` | Detalhes do cliente | `ADMIN`, `EMPLOYEE` | - | `200 OK` (`ClientResponse`) |
| `PUT` | `/api/clients/{id}` | Atualiza cliente | `ADMIN`, `EMPLOYEE` | - | `200 OK` (`ClientResponse`) |
| `DELETE` | `/api/clients/{id}` | Desativa cliente (*soft delete*) | `ADMIN` | - | `204 No Content` |
| `GET` | `/api/clients/{id}/appointments` | Histórico de agendamentos | `ADMIN`, `EMPLOYEE` | `page, size` | `200 OK` (`Page<AppointmentSummaryResponse>`) |

---

### 2.4 Módulo: Serviços (`/api/services`)

| Método | Endpoint | Descrição | Permissão | Resposta Sucesso |
|---|---|---|---|---|
| `POST` | `/api/services` | Cadastra novo serviço | `ADMIN` | `201 Created` (`ServiceResponse`) |
| `GET` | `/api/services` | Lista serviços cadastrados | `ADMIN`, `EMPLOYEE` | `200 OK` (`List<ServiceResponse>` ou `Page`) |
| `GET` | `/api/services/{id}` | Busca serviço por ID | `ADMIN`, `EMPLOYEE` | `200 OK` (`ServiceResponse`) |
| `PUT` | `/api/services/{id}` | Atualiza preço, duração ou comissão | `ADMIN` | `200 OK` (`ServiceResponse`) |
| `DELETE` | `/api/services/{id}` | Desativa serviço (*soft delete*) | `ADMIN` | `204 No Content` |

#### Contratos DTO:
- **`ServiceCreateRequest`:**
  - `name`: String (NotBlank)
  - `description`: String (opcional)
  - `price`: BigDecimal (NotNull, DecimalMin 0.01)
  - `durationMinutes`: Integer (NotNull, Min 5, Max 480)
  - `customCommissionRate`: BigDecimal (opcional, DecimalMin 0.00, DecimalMax 100.00)

---

### 2.5 Módulo: Agendamentos (`/api/appointments`)

| Método | Endpoint | Descrição | Regras Críticas | Resposta Sucesso |
|---|---|---|---|---|
| `POST` | `/api/appointments` | Cria agendamento | Calcula `end_time = start_time + duration`. Valida ausência de sobreposição para o funcionário. | `201 Created` |
| `GET` | `/api/appointments` | Consulta agenda por período/funcionário | Filtros: `employeeId, clientId, status, startDate, endDate` | `200 OK` (`List<AppointmentResponse>`) |
| `GET` | `/api/appointments/{id}` | Detalhes do agendamento | Retorna dados completos com cliente, serviço e funcionário | `200 OK` |
| `PUT` | `/api/appointments/{id}` | Reagenda ou altera dados | Revalida sobreposição se o horário ou funcionário mudarem | `200 OK` |
| `PATCH` | `/api/appointments/{id}/status` | Transição de status | Dispara cálculo de comissão e receita quando status for `CONCLUIDO` | `200 OK` |
| `GET` | `/api/appointments/availability` | Consulta slots disponíveis por profissional e data | Calcula intervalos livres com base na duração do serviço | `200 OK` (`List<TimeSlotResponse>`) |

#### Transições de Status Válidas:
```
AGENDADO -> CONFIRMADO -> EM_ATENDIMENTO -> CONCLUIDO
    |            |               |
    v            v               v
CANCELADO    CANCELADO       CANCELADO
    |            |
    v            v
NAO_COMPARECEU  NAO_COMPARECEU
```

---

### 2.6 Módulo: Financeiro (`/api/financial`)

| Método | Endpoint | Descrição | Permissão | Resposta Sucesso |
|---|---|---|---|---|
| `POST` | `/api/financial/transactions` | Registra despesa ou receita avulsa | `ADMIN` | `201 Created` |
| `GET` | `/api/financial/transactions` | Lista lançamentos financeiros com filtros | `ADMIN` | `200 OK` (`Page<FinancialTransactionResponse>`) |
| `GET` | `/api/financial/transactions/{id}` | Busca lançamento por ID | `ADMIN` | `200 OK` |
| `PUT` | `/api/financial/transactions/{id}` | Altera lançamento (proibido se período fechado) | `ADMIN` | `200 OK` |
| `DELETE` | `/api/financial/transactions/{id}` | Cancela transação (proibido se período fechado) | `ADMIN` | `204 No Content` |
| `GET` | `/api/financial/summary` | Balanço sintético por período | `ADMIN` | `200 OK` (`FinancialSummaryResponse`) |

---

### 2.7 Módulo: Comissões (`/api/commissions`)

| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/api/commissions` | Lista comissões geradas por período/profissional | `ADMIN` |
| `GET` | `/api/commissions/{id}` | Detalhes do snapshot da comissão | `ADMIN` |
| `PATCH` | `/api/commissions/{id}/pay` | Registra quitação manual da comissão | `ADMIN` |

---

### 2.8 Módulo: Fechamento Mensal (`/api/monthly-closings`)

| Método | Endpoint | Descrição | Regras Críticas | Resposta Sucesso |
|---|---|---|---|---|
| `POST` | `/api/monthly-closings` | Gera fechamento de mês/ano | Transação atômica. Impede fechamento duplicado. Gera snapshot por profissional. | `201 Created` (`MonthlyClosingResponse`) |
| `GET` | `/api/monthly-closings` | Lista fechamentos realizados | Histórico paginado | `200 OK` (`List<MonthlyClosingSummaryResponse>`) |
| `GET` | `/api/monthly-closings/{id}` | Demonstrativo completo | Exibe faturamento, despesas, comissões consolidadas e itens por profissional | `200 OK` (`MonthlyClosingDetailResponse`) |
| `POST` | `/api/monthly-closings/{id}/reopen` | Reabertura controlada | Exige justificativa (`auditNotes`). Altera status para `REOPENED` e audita usuário responsável. | `200 OK` |

---

### 2.9 Módulo: Dashboard & Indicadores (`/api/dashboard`)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/dashboard/overview` | Retorna KPIs principais do salão para o período (mês atual por padrão): Faturamento Bruto, Despesas Totais, Comissões Totais, Resultado Líquido, Total de Agendamentos, Taxa de Cancelamento |
| `GET` | `/api/dashboard/top-services` | Ranking dos serviços mais demandados (quantidade e faturamento gerado) |
| `GET` | `/api/dashboard/top-employees` | Ranking de colaboradores por faturamento e atendimentos |
| `GET` | `/api/dashboard/upcoming-appointments` | Lista dos próximos agendamentos imediatos |

---

## 3. Padrão Global de Resposta de Erros (RFC 7807 Problem Details)

Em casos de erro (HTTP 4xx ou 5xx), a API responde com o formato padronizado:

```json
{
  "type": "https://salonmanager.com/errors/schedule-conflict",
  "title": "Conflito de Agendamento",
  "status": 409,
  "detail": "O profissional João da Silva já possui um atendimento entre 14:00 e 15:00 na data 2026-11-15.",
  "instance": "/api/appointments",
  "timestamp": "2026-11-09T19:45:00Z",
  "invalidParams": [
    {
      "name": "startTime",
      "reason": "Horário coincide com agendamento existente (ID: 3fa85f64-5717-4562-b3fc-2c963f66afa6)"
    }
  ]
}
```
