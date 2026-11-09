package com.manager.salon.modules.commission.controller;

import com.manager.salon.modules.commission.dto.CommissionResponse;
import com.manager.salon.modules.commission.entity.CommissionStatus;
import com.manager.salon.modules.commission.service.CommissionCalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/commissions")
@Tag(name = "Comissões", description = "Endpoints para consulta e liquidação de comissões de colaboradores")
public class CommissionController {

    private final CommissionCalculatorService commissionService;

    public CommissionController(CommissionCalculatorService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar comissões", description = "Retorna página de comissões com filtros por colaborador, status e período.")
    public ResponseEntity<Page<CommissionResponse>> findAll(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) CommissionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<CommissionResponse> page = commissionService.findAll(employeeId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar comissão por ID", description = "Retorna detalhes e snapshot de cálculo da comissão.")
    public ResponseEntity<CommissionResponse> findById(@PathVariable UUID id) {
        CommissionResponse response = commissionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Marcar comissão como paga", description = "Altera o status da comissão para PAID.")
    public ResponseEntity<CommissionResponse> markAsPaid(@PathVariable UUID id) {
        CommissionResponse response = commissionService.markAsPaid(id);
        return ResponseEntity.ok(response);
    }
}
