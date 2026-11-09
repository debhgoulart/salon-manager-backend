# CONSTITUTION — SALON MANAGER BACKEND

## 1. Princípios Fundamentais (Spec Kit Constitution)

Este documento estabelece os princípios reitores, as diretrizes de governança e os padrões de engenharia para o backend do sistema **Salon Manager**.

### 1.1 Exclusividade de Escopo
- O repositório e os esforços de engenharia são **estritamente restritos ao BACKEND**.
- Nenhuma dependência, código, template ou biblioteca de frontend (HTML/CSS/JS/Vue) deve residir neste repositório.

### 1.2 Integridade e Rigor de Engenharia
- **Zero Gambiarras / Anti-patterns**: Código modular, extensível e com responsabilidades coesas.
- **Isolamento de Domínio**: Entidades JPA nunca transitam na borda HTTP da aplicação. A comunicação externa é sempre realizada através de DTOs (Data Transfer Objects).
- **Sem Lógica em Controllers**: Controllers são estritamente responsáveis pela serialização/deserialização HTTP, roteamento e repasse para a camada de serviço.
- **Consistência Financeira**: Todos os cálculos monetários e percentuais utilizam obrigatoriamente `java.math.BigDecimal` com precisão e escala explícitas (`RoundingMode.HALF_EVEN` / Bankers Rounding).
- **Imutabilidade Histórica**: Registros de histórico financeiro, comissões pagas e fechamentos contábeis não podem sofrer alterações silenciosas nem exclusão física (*hard delete*). Emprega-se desativação lógica (*soft delete*) e *snapshotting* de estado.
- **Atomicidade e Transacionalidade**: Operações compostas que afetam múltiplos agregados devem ser envolvidas em transações (`@Transactional`) com isolamento e controle de concorrência adequados.

### 1.3 Governança de Banco de Dados
- Toda e qualquer alteração de schema deve ser realizada via scripts migráveis versionados pelo **Flyway** (`src/main/resources/db/migration/V*.sql`).
- Chaves estrangeiras, restrições de unicidade (`UNIQUE`), índices de busca e restrições de integridade (`CHECK`) devem ser garantidos no banco de dados, e não apenas na camada de aplicação.

### 1.4 Segurança por Padrão (Security by Design)
- Todas as rotas por padrão são protegidas e autenticadas, exceto endpoints explicitamente públicos (e.g. `/api/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`).
- Senhas salvas com hash seguro e fator de trabalho apropriado via `BCryptPasswordEncoder`.
- Autenticação *stateless* com JSON Web Tokens (JWT) validados a cada requisição.

---

## 2. Padrões Arquiteturais e de Código

### 2.1 Arquitetura Modular por Funcionalidade (Package-by-Feature)
```
[ Controllers ] -> [ Services / Use Cases ] -> [ Repositories ] -> [ Database (PostgreSQL) ]
      |                      |
    [ DTOs ]              [ Entities ]
      ^                      ^
      +-------[ MapStruct Mappers ]----+
```

A estrutura do código é organizada por módulos funcionais (`com.manager.salon.modules.<feature>`):
1. **`common/`**: Classes base (`BaseEntity` com ID UUID e timestamps), utilitários e constantes.
2. **`config/`**: Configurações do Spring (OpenAPI/Swagger, Jackson, Timezone).
3. **`security/`**: Componentes do Spring Security 6 (Filtro JWT, TokenService, UserPrincipal).
4. **`exception/`**: Exceções de domínio e `GlobalExceptionHandler` formatando respostas com RFC 7807 (Problem Details).
5. **`modules/<feature>/`**: Cada módulo funcional agrega:
   - `controller/`: REST endpoints com OpenAPI annotations.
   - `dto/`: DTOs de entrada (com Bean Validation) e Records para respostas imutáveis.
   - `entity/`: Entidades JPA com chave primária UUID e versionamento `@Version` quando aplicável.
   - `repository/`: Interfaces `JpaRepository`.
   - `service/`: Regras de negócio, cálculos com `BigDecimal` e transações `@Transactional`.
   - `mapper/`: Interfaces MapStruct para conversão de alto desempenho entre DTOs e Entidades.

---

## 3. Padrão de Qualidade e Testabilidade

- Cobertura obrigatória de testes unitários para Services contendo regras de negócio críticas (cálculo de comissão, fechamento contábil, agendamento).
- Testes de integração cobrindo fluxos completos da API (`@SpringBootTest` / `MockMvc` / `Testcontainers`).
- Uso de Mockito para isolamento de dependências em testes unitários.

---

## 4. Gestão do Ciclo de Vida do Projeto (Spec Kit Flow)

O desenvolvimento segue estritamente as etapas do Spec Kit:
1. **Constitution** (Definição dos pilares, arquitetura e alinhamento de decisões) -> *ETAPA ATUAL*
2. **Specify** (Especificação formal das entidades, endpoints e contratos)
3. **Clarify** (Esclarecimento de cenários de borda, exceptions e validações)
4. **Plan** (Plano detalhado de implementação estruturado)
5. **Tasks** (Lista granular de tarefas para execução incremental)
6. **Analyze** (Auditoria de consistência e conformidade antes da codificação)
7. **Implement** (Implementação incremental orientada a testes)

Nenhuma linha de código de implementação de funcionalidade deve ser escrita antes da conclusão das especificações e da aprovação formal do usuário.
