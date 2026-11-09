package com.manager.salon.modules.employee.controller;

import com.manager.salon.modules.employee.dto.EmployeeCreateRequest;
import com.manager.salon.modules.employee.dto.EmployeeResponse;
import com.manager.salon.modules.employee.dto.EmployeeUpdateRequest;
import com.manager.salon.modules.employee.dto.UpdateCommissionRateRequest;
import com.manager.salon.modules.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Colaboradores", description = "Endpoints para gerenciamento de funcionários do salão")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar colaborador", description = "Cadastra um novo funcionário e opcionalmente cria credenciais de acesso.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Colaborador cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "CPF duplicado ou dados inválidos."),
            @ApiResponse(responseCode = "403", description = "Permissão negada (apenas administradores).")
    })
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar colaboradores", description = "Retorna página de colaboradores com filtros por nome e status ativo/inativo.")
    public ResponseEntity<Page<EmployeeResponse>> findAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        Page<EmployeeResponse> page = employeeService.findAll(active, name, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar colaborador por ID", description = "Retorna os detalhes completos de um colaborador.")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable UUID id) {
        EmployeeResponse response = employeeService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar colaborador", description = "Atualiza os dados cadastrais do funcionário.")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        EmployeeResponse response = employeeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/commission-rate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar taxa de comissão padrão", description = "Atualiza o percentual padrão de comissão do colaborador.")
    public ResponseEntity<EmployeeResponse> updateCommissionRate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommissionRateRequest request
    ) {
        EmployeeResponse response = employeeService.updateCommissionRate(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar colaborador", description = "Realiza desativação lógica (soft delete) do colaborador e seu usuário de acesso.")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        employeeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
