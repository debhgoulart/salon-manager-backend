# TASKS — SALON MANAGER BACKEND
**Fase do Spec Kit:** `/speckit.tasks`

Este documento decompõe o plano de engenharia em tarefas granulares, ordenadas por dependência técnica e rastreáveis por critérios de aceitação objetivos.

---

## Matriz de Rastreabilidade de Tarefas

| Tarefa | Módulo / Componente | Dependências | Critério de Conclusão Principal |
|---|---|---|---|
| **TASK-01** | Infraestrutura & Configurações | Nenhuma | `.env.example`, `application.yaml`, OpenAPI config e Jackson config |
| **TASK-02** | Tratamento Global de Exceções | TASK-01 | `GlobalExceptionHandler`, RFC 7807 DTOs e classes de exceção de domínio |
| **TASK-03** | Banco de Dados & Migração Flyway | TASK-01 | `V1__initial_schema.sql` com 8 tabelas, UUIDs, PKs, FKs e índices |
| **TASK-04** | Base Entity & Modelagem Comum | TASK-01, 03 | `BaseEntity` com UUID, timestamps e suporte a JPA |
| **TASK-05** | Segurança & Autenticação (Auth) | TASK-02, 03, 04 | `User`, `JwtTokenService`, `JwtAuthenticationFilter`, `SecurityConfig`, `AuthController` |
| **TASK-06** | Seed Inicial do Administrador | TASK-05 | `AdminSeedRunner` criando o primeiro admin com BCrypt se banco vazio |
| **TASK-07** | Gestão de Colaboradores (Employee) | TASK-02, 04, 05 | CRUD de funcionários, desativação lógica, DTOs e MapStruct mapper |
| **TASK-08** | Gestão de Clientes (Client) | TASK-02, 04, 05 | CRUD de clientes, busca paginada, DTOs e MapStruct mapper |
| **TASK-09** | Gestão de Serviços (Service) | TASK-02, 04, 05 | CRUD de serviços com preço, duração e taxa de comissão customizada |
| **TASK-10** | Motor de Agendamento (Appointment) | TASK-07, 08, 09 | Verificação de conflito de horários, cálculo de término, optimistic lock |
| **TASK-11** | Consulta de Disponibilidade de Horários | TASK-10 | Endpoint de slots livres por funcionário e data |
| **TASK-12** | Motor de Cálculo de Comissões | TASK-07, 09, 10 | Regra de precedência, precisão `BigDecimal`, snapshot imutável |
| **TASK-13** | Gestão Financeira (Transactions) | TASK-02, 10, 12 | Lançamento de despesas e receitas avulsas, resumo financeiro, bloqueio de período |
| **TASK-14** | Fechamento Mensal (Monthly Closing) | TASK-12, 13 | Consolidação mensal atômica, snapshot de valores, reabertura auditada |
| **TASK-15** | Dashboard & Indicadores | TASK-10, 13, 14 | Métricas agregadas de faturamento, despesas, top serviços e funcionários |
| **TASK-16** | Suíte de Testes Automatizados | TASK-10 a 15 | Testes unitários com JUnit 5/Mockito e testes de integração com MockMvc |
| **TASK-17** | Conteinerização & Infra Docker | TASK-01 a 15 | `Dockerfile` multi-stage, `docker-compose.yml` com Postgres e healthchecks |
| **TASK-18** | Contrato de Integração Vue.js & Docs | TASK-01 a 17 | `docs/frontend-integration-contract.md` e `README.md` completo |

---

## Detalhamento das Tarefas de Execução

### Bloco A: Fundação e Segurança

#### `TASK-01`: Infraestrutura e Configurações Base
- **Arquivos:**
  - `.env.example`
  - `src/main/resources/application.yaml`
  - `src/main/resources/application-test.yaml`
  - `com.manager.salon.config.OpenApiConfig`
  - `com.manager.salon.config.JacksonConfig`
- **Critérios de Aceitação:**
  - Variáveis de ambiente declaradas com valores padrão claros para desenvolvimento.
  - Formatação de `BigDecimal` com escala 2 e sem notação científica serializada pelo Jackson.
  - Documentação OpenAPI / Swagger acessível em `/swagger-ui/index.html`.

#### `TASK-02`: Camada Comum de Exceções (RFC 7807)
- **Arquivos:**
  - `com.manager.salon.exception.ProblemDetailsResponse`
  - `com.manager.salon.exception.ResourceNotFoundException`
  - `com.manager.salon.exception.BusinessException`
  - `com.manager.salon.exception.ScheduleConflictException`
  - `com.manager.salon.exception.ClosedPeriodException`
  - `com.manager.salon.exception.GlobalExceptionHandler`
- **Critérios de Aceitação:**
  - Erros de validação do Bean Validation (`MethodArgumentNotValidException`) mapeados em lista de campos inválidos (`invalidParams`).
  - Respostas HTTP consistentes com RFC 7807.

#### `TASK-03`: Flyway Migration Schema Inicial
- **Arquivos:**
  - `src/main/resources/db/migration/V1__initial_schema.sql`
- **Critérios de Aceitação:**
  - Script SQL com tabelas `users`, `employees`, `clients`, `services`, `appointments`, `commissions`, `financial_transactions`, `monthly_closings`, `monthly_closing_items`.
  - Índices otimizados para consultas de sobreposição de agenda e consultas financeiras por data.

#### `TASK-04`: Base Entity e Estrutura Comum
- **Arquivos:**
  - `com.manager.salon.common.BaseEntity`
- **Critérios de Aceitação:**
  - Classe `@MappedSuperclass` com `@Id UUID id`, `@CreationTimestamp createdAt`, `@UpdateTimestamp updatedAt`.

#### `TASK-05`: Segurança, JWT e Módulo de Autenticação
- **Arquivos:**
  - `com.manager.salon.security.JwtTokenService`
  - `com.manager.salon.security.JwtAuthenticationFilter`
  - `com.manager.salon.security.SecurityConfig`
  - `com.manager.salon.security.UserPrincipal`
  - `com.manager.salon.modules.auth.entity.User`
  - `com.manager.salon.modules.auth.entity.UserRole`
  - `com.manager.salon.modules.auth.repository.UserRepository`
  - `com.manager.salon.modules.auth.service.AuthService`
  - `com.manager.salon.modules.auth.dto.*`
  - `com.manager.salon.modules.auth.controller.AuthController`
- **Critérios de Aceitação:**
  - Autenticação stateless funcionando com JWT (Bearer token).
  - Rotas públicas liberadas e rotas privadas protegidas com status `401 Unauthorized` na ausência de token válido.

#### `TASK-06`: Seed Inicial de Administrador
- **Arquivos:**
  - `com.manager.salon.config.AdminSeedRunner`
- **Critérios de Aceitação:**
  - Na subida da aplicação, se `userRepository.count() == 0`, insere usuário com credenciais padrão configuráveis.

---

### Bloco B: Cadastros Essenciais

#### `TASK-07`: Módulo de Funcionários (Employees)
- **Arquivos:**
  - `com.manager.salon.modules.employee.entity.Employee`
  - `com.manager.salon.modules.employee.repository.EmployeeRepository`
  - `com.manager.salon.modules.employee.dto.*`
  - `com.manager.salon.modules.employee.mapper.EmployeeMapper`
  - `com.manager.salon.modules.employee.service.EmployeeService`
  - `com.manager.salon.modules.employee.controller.EmployeeController`
- **Critérios de Aceitação:**
  - CRUD funcional com paginação e busca por nome/status.
  - Desativação lógica (`active = false`) preservando integridade referencial.
  - Validação de unicidade de CPF.

#### `TASK-08`: Módulo de Clientes (Clients)
- **Arquivos:**
  - `com.manager.salon.modules.client.entity.Client`
  - `com.manager.salon.modules.client.repository.ClientRepository`
  - `com.manager.salon.modules.client.dto.*`
  - `com.manager.salon.modules.client.mapper.ClientMapper`
  - `com.manager.salon.modules.client.service.ClientService`
  - `com.manager.salon.modules.client.controller.ClientController`
- **Critérios de Aceitação:**
  - CRUD funcional com paginação e busca textual.
  - Histórico de agendamentos e serviços prestados por cliente.

#### `TASK-09`: Módulo de Serviços (Services)
- **Arquivos:**
  - `com.manager.salon.modules.service.entity.ServiceEntity`
  - `com.manager.salon.modules.service.repository.ServiceRepository`
  - `com.manager.salon.modules.service.dto.*`
  - `com.manager.salon.modules.service.mapper.ServiceMapper`
  - `com.manager.salon.modules.service.service.ServiceManagementService`
  - `com.manager.salon.modules.service.controller.ServiceController`
- **Critérios de Aceitação:**
  - Preço em `BigDecimal`, duração em minutos, taxa customizada opcional.
  - Desativação lógica.

---

### Bloco C: Motores de Negócio (Agendamento, Comissões e Finanças)

#### `TASK-10`: Motor de Agendamentos (Appointments)
- **Arquivos:**
  - `com.manager.salon.modules.appointment.entity.Appointment`
  - `com.manager.salon.modules.appointment.entity.AppointmentStatus`
  - `com.manager.salon.modules.appointment.repository.AppointmentRepository`
  - `com.manager.salon.modules.appointment.dto.*`
  - `com.manager.salon.modules.appointment.mapper.AppointmentMapper`
  - `com.manager.salon.modules.appointment.service.AppointmentService`
  - `com.manager.salon.modules.appointment.controller.AppointmentController`
- **Critérios de Aceitação:**
  - `end_time` calculado automaticamente.
  - Rejeição com erro `409 Conflict` se houver colisão de horário para o funcionário.
  - Controle de concorrência com `@Version`.

#### `TASK-11`: Consulta de Disponibilidade (Slots)
- **Arquivos:**
  - `com.manager.salon.modules.appointment.service.AvailabilityService`
  - DTOs de slots livres (`TimeSlotResponse`)
- **Critérios de Aceitação:**
  - Dado funcionário, data e serviço, retorna os intervalos vagos dentro do horário de expediente.

#### `TASK-12`: Motor de Comissões (Commissions)
- **Arquivos:**
  - `com.manager.salon.modules.commission.entity.Commission`
  - `com.manager.salon.modules.commission.repository.CommissionRepository`
  - `com.manager.salon.modules.commission.service.CommissionCalculatorService`
  - `com.manager.salon.modules.commission.dto.*`
  - `com.manager.salon.modules.commission.controller.CommissionController`
- **Critérios de Aceitação:**
  - Prioridade: taxa do serviço > taxa do funcionário.
  - Cálculo determinístico com `BigDecimal.setScale(2, RoundingMode.HALF_EVEN)`.
  - Snapshot imutável gravado ao concluir o serviço.

#### `TASK-13`: Módulo Financeiro (Financial Transactions)
- **Arquivos:**
  - `com.manager.salon.modules.financial.entity.FinancialTransaction`
  - `com.manager.salon.modules.financial.entity.TransactionType`
  - `com.manager.salon.modules.financial.entity.TransactionStatus`
  - `com.manager.salon.modules.financial.repository.FinancialTransactionRepository`
  - `com.manager.salon.modules.financial.service.FinancialTransactionService`
  - `com.manager.salon.modules.financial.dto.*`
  - `com.manager.salon.modules.financial.controller.FinancialController`
- **Critérios de Aceitação:**
  - Lançamento automático de receita ao concluir agendamento.
  - Lançamentos manuais de despesas e receitas extras.
  - Bloqueio imediato (`409 Conflict`) de inserções/edições em meses com fechamento `CLOSED`.
  - Resumo financeiro consolidado por período.

#### `TASK-14`: Fechamento Contábil Mensal (Monthly Closings)
- **Arquivos:**
  - `com.manager.salon.modules.closing.entity.MonthlyClosing`
  - `com.manager.salon.modules.closing.entity.MonthlyClosingItem`
  - `com.manager.salon.modules.closing.repository.MonthlyClosingRepository`
  - `com.manager.salon.modules.closing.service.MonthlyClosingService`
  - `com.manager.salon.modules.closing.dto.*`
  - `com.manager.salon.modules.closing.controller.MonthlyClosingController`
- **Critérios de Aceitação:**
  - Transação atômica consolidando receitas, despesas e comissões por profissional.
  - Prevenção de duplicidade por mês/ano.
  - Reabertura auditada com justificativa obrigatória.

#### `TASK-15`: Dashboard & Métricas
- **Arquivos:**
  - `com.manager.salon.modules.dashboard.service.DashboardService`
  - `com.manager.salon.modules.dashboard.dto.*`
  - `com.manager.salon.modules.dashboard.controller.DashboardController`
- **Critérios de Aceitação:**
  - Endpoints fornecendo indicadores consolidados de faturamento, despesas, resultado líquido, top colaboradores e serviços mais realizados.

---

### Bloco D: Garantia de Qualidade, Documentação e Entrega

#### `TASK-16`: Suíte de Testes Automatizados
- **Arquivos:**
  - `com.manager.salon.modules.commission.CommissionCalculatorServiceTest`
  - `com.manager.salon.modules.appointment.AppointmentServiceTest`
  - `com.manager.salon.modules.closing.MonthlyClosingServiceTest`
  - `com.manager.salon.modules.financial.FinancialTransactionServiceTest`
  - `com.manager.salon.modules.auth.AuthControllerIT`
- **Critérios de Aceitação:**
  - 100% dos testes cobrindo as regras críticas aprovadas passando com sucesso.

#### `TASK-17`: Dockerfile e Docker Compose
- **Arquivos:**
  - `Dockerfile`
  - `docker-compose.yml`
- **Critérios de Aceitação:**
  - Construção multi-stage funcional gerando imagem compacta e segura.
  - `docker compose up` sobe PostgreSQL e backend integrados.

#### `TASK-18`: Contrato de Integração Vue.js e README
- **Arquivos:**
  - `docs/frontend-integration-contract.md`
  - `README.md`
- **Critérios de Aceitação:**
  - Documento detalhado para consumo do frontend Vue.js (rotas, payloads, cabeçalhos, formato de erros e paginação).
  - README com guia passo a passo para execução e testes.
