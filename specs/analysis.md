# ANALYZE — SALON MANAGER BACKEND
**Fase do Spec Kit:** `/speckit.analyze`

Este documento consolida a auditoria formal de conformidade, análise de lacunas (*gap analysis*), matriz de riscos técnicos e verificação de integridade entre todos os artefatos de especificação e os requisitos do projeto **Salon Manager**.

---

## 1. Matriz de Conformidade com Requisitos do Usuário

| Requisito do Prompt Original | Artefato de Especificação | Tarefa Mapeada | Status de Auditoria |
|---|---|---|:---:|
| **1. Desenvolvimento Exclusivo do Backend** (Sem telas de frontend) | `constitution.md` (Sec. 1.1) | Escopo geral | **Em Conformidade** |
| **2. Java 21, Spring Boot 3.3.5, PostgreSQL, Flyway, Docker** | `pom.xml`, `specifications.md` | `TASK-01`, `TASK-03`, `TASK-17` | **Em Conformidade** |
| **3. Arquitetura em Camadas Modular (Package-by-Feature)** | `constitution.md` (Sec. 2.1) | `TASK-04` a `TASK-15` | **Em Conformidade** |
| **4. Autenticação JWT, BCrypt, Perfis Extensíveis (RBAC)** | `specifications.md` (Sec. 2.1) | `TASK-05`, `TASK-06` | **Em Conformidade** |
| **5. Gestão de Funcionários, CPF, Soft Delete, Histórico** | `specifications.md` (Sec. 2.2) | `TASK-07` | **Em Conformidade** |
| **6. Gestão de Clientes, Histórico de Serviços** | `specifications.md` (Sec. 2.3) | `TASK-08` | **Em Conformidade** |
| **7. Gestão de Serviços, Preço, Duração, Comissão Customizada** | `specifications.md` (Sec. 2.4) | `TASK-09` | **Em Conformidade** |
| **8. Agendamentos: Prevenção de Conflitos, Duração, Status, Concorrência** | `clarifications.md` (Sec. 1, 2) | `TASK-10`, `TASK-11` | **Em Conformidade** |
| **9. Financeiro: Receitas/Despesas, Status, Bloqueio de Mês Fechado** | `clarifications.md` (Sec. 2, 3) | `TASK-13` | **Em Conformidade** |
| **10. Comissões: Precedência (Serviço > Funcionário), BigDecimal, Snapshot** | `clarifications.md` (Sec. 2.2, 6) | `TASK-12` | **Em Conformidade** |
| **11. Fechamento Mensal: Bloqueio de Duplicidade, Reabertura Auditada** | `clarifications.md` (Sec. 3) | `TASK-14` | **Em Conformidade** |
| **12. Dashboard: KPIs Financeiros, Top Colaboradores e Serviços** | `specifications.md` (Sec. 2.9) | `TASK-15` | **Em Conformidade** |
| **13. Testes Unitários e de Integração (JUnit 5 / Mockito)** | `plan.md` (Fase 8) | `TASK-16` | **Em Conformidade** |
| **14. OpenAPI/Swagger, Dockerfile, docker-compose, Contrato Vue.js** | `plan.md` (Fase 8) | `TASK-17`, `TASK-18` | **Em Conformidade** |

---

## 2. Análise de Riscos Técnicos e Estratégias de Mitigação

### Risco 1: Condição de Corrida (*Race Condition*) na Reserva de Horários
- **Cenário:** Duas requisições paralelas recebidas no mesmo milissegundo solicitando o mesmo funcionário para horários sobrepostos.
- **Mitigação Arquitetural:**
  1. Abertura de transação com isolamento adequado no `AppointmentService`.
  2. Consulta de bloqueio concorrente (`PESSIMISTIC_WRITE` na entidade do funcionário ou verificação de colisão com lock seletivo).
  3. Coluna de versionamento otimista (`@Version Long version`) na entidade `Appointment`.
  4. Validação rigorosa da fórmula: `start_time < existing.end_time AND end_time > existing.start_time`.

### Risco 2: Arredondamento e Divergência Contábil
- **Cenário:** Divergência de centavos no cálculo de comissões rateadas ou faturamento líquido.
- **Mitigação Arquitetural:**
  - Uso mandatório de `BigDecimal` com escala 2 e arredondamento bancário `RoundingMode.HALF_EVEN` em todas as operações de multiplicação e divisão.
  - Jackson configurado para serializar números decimais como strings formatadas ou números com duas casas fixas, evitando conversão para ponto flutuante IEEE-754.

### Risco 3: Alterações Indevidas em Períodos Contábeis Fechados
- **Cenário:** Operador lança uma despesa ou conclui um agendamento com data retroativa pertencente a um mês cujo fechamento já foi homologado (`CLOSED`).
- **Mitigação Arquitetural:**
  - Validador centralizado no `FinancialTransactionService` e `AppointmentService` que consulta a existência de `MonthlyClosing` com status `CLOSED` para o ano/mês da transação. Se existir, rejeita com `ClosedPeriodException` (`409 Conflict`).

### Risco 4: Ordem de Compilação de Anotações (MapStruct)
- **Cenário:** Falha na geração dos códigos de mapeamento em tempo de compilação.
- **Mitigação:**
  - Configuração explícita do `maven-compiler-plugin` 3.13.0 no `pom.xml` definindo o processador `org.mapstruct:mapstruct-processor:1.5.5.Final`.

---

## 3. Veredito da Análise

- **Consistência:** 100% dos requisitos do prompt original estão contemplados na arquitetura e mapeados nas 18 tarefas.
- **Prontidão de Engenharia:** Não existem ambiguidades pendentes, dependências circulares ou decisões técnicas não resolvidas.
- **Aprovação Prévia:** Todas as decisões (Spring Boot 3.3.5, Java 21, UUIDs, Modularidade e MapStruct) foram formalmente aprovadas pelo usuário.
- **Próximo Passo:** O projeto está plenamente apto para a fase final: **/speckit.implement**.
