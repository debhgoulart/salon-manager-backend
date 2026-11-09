package com.manager.salon.modules.closing.controller;

import com.manager.salon.modules.closing.dto.MonthlyClosingCreateRequest;
import com.manager.salon.modules.closing.dto.MonthlyClosingReopenRequest;
import com.manager.salon.modules.closing.dto.MonthlyClosingResponse;
import com.manager.salon.modules.closing.dto.MonthlyClosingSummaryResponse;
import com.manager.salon.modules.closing.service.MonthlyClosingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/monthly-closings")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Fechamento Mensal", description = "Endpoints para fechamento contábil mensal, consolidação e reabertura auditada")
public class MonthlyClosingController {

    private final MonthlyClosingService closingService;

    public MonthlyClosingController(MonthlyClosingService closingService) {
        this.closingService = closingService;
    }

    @PostMapping
    @Operation(summary = "Executar fechamento mensal", description = "Consolida receitas, despesas e comissões do mês, criando snapshot imutável e bloqueando alterações no período.")
    public ResponseEntity<MonthlyClosingResponse> closeMonth(
            @Valid @RequestBody MonthlyClosingCreateRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "ADMIN";
        MonthlyClosingResponse response = closingService.closeMonth(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar fechamentos mensais", description = "Retorna histórico paginado de fechamentos contábeis já realizados.")
    public ResponseEntity<Page<MonthlyClosingSummaryResponse>> findAll(
            @PageableDefault(size = 12, sort = "referenceYear") Pageable pageable
    ) {
        Page<MonthlyClosingSummaryResponse> page = closingService.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar demonstrativo do fechamento por ID", description = "Retorna demonstrativo analítico completo contendo totalizadores e comissões por colaborador.")
    public ResponseEntity<MonthlyClosingResponse> findById(@PathVariable UUID id) {
        MonthlyClosingResponse response = closingService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reopen")
    @Operation(summary = "Reabertura controlada do fechamento", description = "Permite a reabertura do mês exigindo justificativa de auditoria obrigatória.")
    public ResponseEntity<MonthlyClosingResponse> reopen(
            @PathVariable UUID id,
            @Valid @RequestBody MonthlyClosingReopenRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "ADMIN";
        MonthlyClosingResponse response = closingService.reopen(id, request, username);
        return ResponseEntity.ok(response);
    }
}
