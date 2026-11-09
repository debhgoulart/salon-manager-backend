# Contrato de Integração Frontend — Salon Manager Backend
**Versão da API:** `v1`  
**Alvo do Frontend:** Vue.js 3 (Composition API, Pinia, TypeScript, Axios)  
**Formato de Dados:** JSON (`application/json`)  
**Padrão de Erros:** RFC 7807 (*Problem Details for HTTP APIs*)

---

## 1. Visão Geral e Protocolo de Comunicação

### 1.1 URLs Base
- **Desenvolvimento Local:** `http://localhost:8080`
- **Documentação OpenAPI / Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`
- **Healthcheck Actuator:** `http://localhost:8080/actuator/health`

### 1.2 Regras Globais de Tipagem
- **Valores Monetários e Taxas:** Serializados como `number` ou `string` decimal (`150.00`, nunca notação científica `1.5e2`).
- **Identificadores:** UUID v4 (`string` no TypeScript).
- **Datas e Horários:** Padrão ISO 8601 UTC / Offset (`YYYY-MM-DDTHH:mm:ssZ` ou `YYYY-MM-DDTHH:mm:ssXXX`), exemplo: `2026-11-10T14:30:00Z`.
- **Datas Simples:** `YYYY-MM-DD` (exemplo: `2026-11-10`).
- **Horários Simples:** `HH:mm` (exemplo: `14:30`).

---

## 2. Autenticação e Ciclo de Sessão JWT

### 2.1 Fluxo de Tokens
A aplicação utiliza Bearer Tokens com JWT assimétrico/HMAC:
- **`accessToken`**: Validade de 2 horas. Deve ser enviado no cabeçalho `Authorization: Bearer <accessToken>`.
- **`refreshToken`**: Validade de 7 dias. Utilizado para renovar o `accessToken` silenciosamente via interceptor HTTP.

### 2.2 Estrutura do Usuário e Sessão
```typescript
export interface UserSession {
  id: string;
  name: string;
  email: string;
  role: 'ROLE_ADMIN' | 'ROLE_MANAGER' | 'ROLE_RECEPTIONIST' | 'ROLE_PROFESSIONAL';
  active: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserSession;
}
```

### 2.3 Exemplo Recomendado de Interceptor Axios (Vue 3 / TypeScript)
```typescript
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const authStore = useAuthStore();
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry && authStore.refreshToken) {
      originalRequest._retry = true;
      try {
        const { data } = await axios.post(`${apiClient.defaults.baseURL}/api/auth/refresh`, {
          refreshToken: authStore.refreshToken
        });
        authStore.setTokens(data.accessToken, data.refreshToken);
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshErr) {
        authStore.logout();
        return Promise.reject(refreshErr);
      }
    }
    return Promise.reject(error);
  }
);
```

---

## 3. Padrão de Erros (RFC 7807)

Todas as respostas de erro `4xx` e `5xx` seguem a estrutura padronizada:

```typescript
export interface InvalidParam {
  field: string;
  message: string;
}

export interface ProblemDetails {
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  invalidParams?: InvalidParam[];
}
```

### Exemplos de Status HTTP
- `400 Bad Request`: Parâmetros inválidos ou violação de validação (`invalidParams` populado).
- `401 Unauthorized`: Token expirado, ausente ou credenciais inválidas.
- `403 Forbidden`: Usuário autenticado sem permissão para o recurso solicitado.
- `404 Not Found`: Recurso não localizado pelo UUID informado.
- `409 Conflict`: Conflito de negócio (ex.: agendamento em horário conflitante, inserção em mês com fechamento contábil encerrado, email/CPF já cadastrado).

---

## 4. Paginação Padrão Spring Data

Todas as listagens paginadas retornam o formato:
```typescript
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    offset: number;
  };
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

---

## 5. Catálogo Completo de Endpoints

### 5.1 Autenticação (`/api/auth`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `POST` | `/api/auth/login` | Realiza autenticação com e-mail e senha | Pública |
| `POST` | `/api/auth/refresh` | Renova token expirado via refresh token | Pública |
| `POST` | `/api/auth/register-admin` | Registra primeiro ou novo administrador | Pública |
| `GET` | `/api/auth/me` | Retorna dados do usuário autenticado | Autenticado |

---

### 5.2 Profissionais / Colaboradores (`/api/v1/employees`)
```typescript
export interface EmployeeRequest {
  name: string;
  email: string;
  phone: string;
  cpf: string;
  defaultCommissionRate: number; // ex: 35.00
}

export interface EmployeeResponse {
  id: string;
  name: string;
  email: string;
  phone: string;
  cpf: string;
  defaultCommissionRate: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
```
| Método | Endpoint | Parâmetros / Body | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/employees` | `search?: string`, `active?: boolean`, `page`, `size` | Lista profissionais paginados |
| `POST` | `/api/v1/employees` | `EmployeeRequest` | Cadastra profissional |
| `GET` | `/api/v1/employees/{id}` | UUID no path | Busca profissional por ID |
| `PUT` | `/api/v1/employees/{id}` | `EmployeeRequest` | Atualiza dados do profissional |
| `PATCH` | `/api/v1/employees/{id}/toggle-active` | UUID no path | Ativa/Desativa profissional (soft-delete) |

---

### 5.3 Clientes (`/api/v1/clients`)
```typescript
export interface ClientRequest {
  name: string;
  email: string;
  phone: string;
  cpf?: string;
  notes?: string;
}

export interface ClientResponse {
  id: string;
  name: string;
  email: string;
  phone: string;
  cpf?: string;
  notes?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
```
| Método | Endpoint | Parâmetros / Body | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/clients` | `search?: string`, `page`, `size` | Busca paginada de clientes |
| `POST` | `/api/v1/clients` | `ClientRequest` | Cadastra cliente |
| `GET` | `/api/v1/clients/{id}` | UUID no path | Detalhes do cliente |
| `PUT` | `/api/v1/clients/{id}` | `ClientRequest` | Atualiza cliente |
| `DELETE` | `/api/v1/clients/{id}` | UUID no path | Exclusão lógica do cliente |

---

### 5.4 Catálogo de Serviços (`/api/v1/services`)
```typescript
export interface ServiceRequest {
  name: string;
  description?: string;
  price: number; // ex: 80.00
  durationMinutes: number; // ex: 45
  customCommissionRate?: number; // ex: 40.00
}

export interface ServiceResponse {
  id: string;
  name: string;
  description?: string;
  price: number;
  durationMinutes: number;
  customCommissionRate?: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
```
| Método | Endpoint | Parâmetros / Body | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/services` | `activeOnly?: boolean`, `page`, `size` | Lista serviços cadastrados |
| `POST` | `/api/v1/services` | `ServiceRequest` | Cria novo serviço |
| `GET` | `/api/v1/services/{id}` | UUID no path | Detalhes do serviço |
| `PUT` | `/api/v1/services/{id}` | `ServiceRequest` | Edita dados do serviço |
| `PATCH` | `/api/v1/services/{id}/toggle-active` | UUID no path | Ativa ou desativa serviço |

---

### 5.5 Motor de Agendamentos e Agenda (`/api/v1/appointments`)
```typescript
export type AppointmentStatus = 'AGENDADO' | 'CONFIRMADO' | 'EM_ATENDIMENTO' | 'CONCLUIDO' | 'CANCELADO' | 'NAO_COMPARECEU';

export interface AppointmentCreateRequest {
  clientId: string;
  employeeId: string;
  serviceId: string;
  startTime: string; // ISO 8601 (ex: "2026-11-10T14:00:00Z")
  notes?: string;
}

export interface AppointmentResponse {
  id: string;
  client: ClientResponse;
  employee: EmployeeResponse;
  service: ServiceResponse;
  startTime: string;
  endTime: string;
  price: number;
  status: AppointmentStatus;
  notes?: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface TimeSlotResponse {
  startTime: string; // HH:mm (ex: "09:00")
  endTime: string;   // HH:mm (ex: "09:45")
  available: boolean;
}
```
| Método | Endpoint | Parâmetros / Body | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/appointments` | `employeeId?`, `clientId?`, `status?`, `startDate?`, `endDate?`, `page`, `size` | Lista agendamentos filtrados |
| `POST` | `/api/v1/appointments` | `AppointmentCreateRequest` | Cria agendamento com validação de colisão |
| `GET` | `/api/v1/appointments/{id}` | UUID no path | Detalhes do agendamento |
| `PATCH` | `/api/v1/appointments/{id}/status` | `{ "status": AppointmentStatus }` | Transição de estado (gatilha comissão se CONCLUIDO) |
| `PATCH` | `/api/v1/appointments/{id}/reschedule` | `{ "newStartTime": string }` | Reagenda para novo horário |
| `PATCH` | `/api/v1/appointments/{id}/cancel` | `{ "reason": string }` | Cancela agendamento |
| `GET` | `/api/v1/appointments/availability` | `employeeId: UUID`, `serviceId: UUID`, `date: YYYY-MM-DD` | Retorna grade de horários livres |

---

### 5.6 Comissões de Profissionais (`/api/v1/commissions`)
```typescript
export type CommissionStatus = 'PENDING' | 'PAID' | 'CANCELLED';

export interface CommissionResponse {
  id: string;
  appointmentId: string;
  employeeId: string;
  employeeName: string;
  serviceAmount: number;
  appliedRate: number;
  commissionAmount: number;
  status: CommissionStatus;
  monthlyClosingId?: string;
  createdAt: string;
}

export interface CommissionSummaryResponse {
  totalAmount: number;
  pendingAmount: number;
  paidAmount: number;
  count: number;
}
```
| Método | Endpoint | Parâmetros | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/commissions` | `employeeId?`, `status?`, `startDate?`, `endDate?`, `page`, `size` | Extrato de comissões |
| `GET` | `/api/v1/commissions/summary` | `startDate?`, `endDate?` | Resumo consolidado de comissões do período |
| `GET` | `/api/v1/commissions/{id}` | UUID no path | Detalhe de comissão específica |

---

### 5.7 Transações Financeiras (`/api/v1/financial`)
```typescript
export type TransactionType = 'INCOME' | 'EXPENSE';
export type TransactionStatus = 'COMPLETED' | 'PENDING' | 'CANCELLED';

export interface FinancialTransactionRequest {
  type: TransactionType;
  category: string;
  description: string;
  amount: number;
  paymentMethod: string;
  transactionDate: string; // ISO 8601
}

export interface FinancialTransactionResponse {
  id: string;
  type: TransactionType;
  category: string;
  description: string;
  amount: number;
  paymentMethod: string;
  status: TransactionStatus;
  appointmentId?: string;
  monthlyClosingId?: string;
  transactionDate: string;
  createdAt: string;
}

export interface FinancialSummaryResponse {
  totalRevenue: number;
  totalExpenses: number;
  netIncome: number;
  transactionsCount: number;
}
```
| Método | Endpoint | Parâmetros / Body | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/financial/transactions` | `type?`, `status?`, `category?`, `startDate?`, `endDate?`, `page`, `size` | Lista lançamentos financeiros |
| `POST` | `/api/v1/financial/transactions` | `FinancialTransactionRequest` | Cria lançamento de receita ou despesa manual |
| `GET` | `/api/v1/financial/transactions/{id}` | UUID no path | Detalhe de transação financeira |
| `GET` | `/api/v1/financial/summary` | `startDate: ISO`, `endDate: ISO` | Resumo financeiro de receitas, despesas e saldo líquido |

---

### 5.8 Fechamento Contábil Mensal (`/api/v1/closings`)
```typescript
export type MonthlyClosingStatus = 'OPEN' | 'CLOSED' | 'REOPENED';

export interface MonthlyClosingItemResponse {
  id: string;
  employeeId: string;
  employeeName: string;
  totalServiceValue: number;
  totalCommission: number;
  appointmentsCount: number;
}

export interface MonthlyClosingResponse {
  id: string;
  year: number;
  month: number;
  totalRevenue: number;
  totalExpenses: number;
  totalCommissions: number;
  netProfit: number;
  status: MonthlyClosingStatus;
  notes?: string;
  auditNotes?: string;
  closedAt?: string;
  closedBy?: string;
  reopenedAt?: string;
  reopenedBy?: string;
  items: MonthlyClosingItemResponse[];
}
```
| Método | Endpoint | Parâmetros / Body | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/closings` | `status?`, `year?` | Lista histórico de fechamentos contábeis |
| `GET` | `/api/v1/closings/{id}` | UUID no path | Detalhe completo de um fechamento mensal |
| `GET` | `/api/v1/closings/calculate` | `year: number`, `month: number` | Prévia/simulação dos cálculos antes de fechar |
| `POST` | `/api/v1/closings/close` | `{ "year": number, "month": number, "notes"?: string }` | Executa o fechamento contábil atômico |
| `POST` | `/api/v1/closings/{id}/reopen` | `{ "auditNotes": string }` | Reabre mês fechado com registro de auditoria |

---

### 5.9 Dashboard e Métricas Operacionais (`/api/v1/dashboard`)
```typescript
export interface DashboardOverviewResponse {
  totalRevenue: number;
  totalExpenses: number;
  totalCommissions: number;
  netProfit: number;
  completedAppointmentsCount: number;
  canceledAppointmentsCount: number;
  activeClientsCount: number;
  activeEmployeesCount: number;
}

export interface TopServiceResponse {
  serviceId: string;
  serviceName: string;
  executionCount: number;
  totalRevenue: number;
}

export interface TopEmployeeResponse {
  employeeId: string;
  employeeName: string;
  executionCount: number;
  totalBilled: number;
  totalCommission: number;
}

export interface UpcomingAppointmentResponse {
  appointmentId: string;
  clientName: string;
  employeeName: string;
  serviceName: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
}
```
| Método | Endpoint | Parâmetros | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/dashboard/overview` | `startDate?: ISO`, `endDate?: ISO` | Indicadores-chave (faturamento, lucro, contadores) |
| `GET` | `/api/v1/dashboard/top-services` | `startDate?: ISO`, `endDate?: ISO`, `limit?: number` | Ranking de serviços mais rentáveis e demandados |
| `GET` | `/api/v1/dashboard/top-employees` | `startDate?: ISO`, `endDate?: ISO`, `limit?: number` | Ranking de profissionais de maior faturamento |
| `GET` | `/api/v1/dashboard/upcoming-appointments` | `limit?: number` | Lista rápida dos próximos atendimentos da agenda |
