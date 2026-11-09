package com.manager.salon.modules.financial.controller;

import com.manager.salon.modules.financial.dto.FinancialSummaryResponse;
import com.manager.salon.modules.financial.dto.FinancialTransactionCreateRequest;
import com.manager.salon.modules.financial.dto.FinancialTransactionResponse;
import com.manager.salon.modules.financial.dto.FinancialTransactionUpdateRequest;
import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import com.manager.salon.modules.financial.service.FinancialTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/financial")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Financeiro", description = "Endpoints para controle de despesas, receitas e fluxo de caixa do salão")
public class FinancialController {

    private final FinancialTransactionService financialService;

    public FinancialController(FinancialTransactionService financialService) {
        this.financialService = financialService;
    }

    @PostMapping("/transactions")
    @Operation(summary = "Criar lançamento financeiro manual", description = "Registra uma despesa ou receita adicional avulsa.")
    public ResponseEntity<FinancialTransactionResponse> create(@Valid @RequestBody FinancialTransactionCreateRequest request) {
        FinancialTransactionResponse response = financialService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transactions")
    @Operation(summary = "Listar lançamentos financeiros", description = "Retorna página de lançamentos com filtros por tipo, status, categoria e período.")
    public ResponseEntity<Page<FinancialTransactionResponse>> findAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 10, sort = "transactionDate") Pageable pageable
    ) {
        Page<FinancialTransactionResponse> page = financialService.findAll(type, status, category, startDate, endDate, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/transactions/{id}")
    @Operation(summary = "Buscar lançamento financeiro por ID", description = "Retorna os detalhes completos do lançamento.")
    public ResponseEntity<FinancialTransactionResponse> findById(@PathVariable UUID id) {
        FinancialTransactionResponse response = financialService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/transactions/{id}")
    @Operation(summary = "Atualizar lançamento financeiro", description = "Atualiza valores e dados de um lançamento (bloqueado em períodos já fechados).")
    public ResponseEntity<FinancialTransactionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialTransactionUpdateRequest request
    ) {
        FinancialTransactionResponse response = financialService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transactions/{id}")
    @Operation(summary = "Cancelar lançamento financeiro", description = "Cancela o lançamento financeiro (bloqueado em períodos já fechados).")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        financialService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumo analítico financeiro", description = "Retorna os totais de receitas e despesas realizadas e pendentes no período informado.")
    public ResponseEntity<FinancialSummaryResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        FinancialSummaryResponse response = financialService.getSummary(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
