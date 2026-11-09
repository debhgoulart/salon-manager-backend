package com.manager.salon.modules.financial.dto;

import com.manager.salon.modules.financial.entity.TransactionStatus;
import com.manager.salon.modules.financial.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialTransactionCreateRequest(
        @Schema(description = "Descrição detalhada do lançamento", example = "Pagamento de conta de luz")
        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
        String description,

        @Schema(description = "Categoria do lançamento", example = "CONTAS")
        @NotBlank(message = "A categoria é obrigatória.")
        String category,

        @Schema(description = "Valor da transação em reais", example = "350.00")
        @NotNull(message = "O valor é obrigatório.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        BigDecimal amount,

        @Schema(description = "Data do lançamento financeiro", example = "2026-11-09")
        @NotNull(message = "A data da transação é obrigatória.")
        LocalDate transactionDate,

        @Schema(description = "Tipo da transação: INCOME (receita) ou EXPENSE (despesa)")
        @NotNull(message = "O tipo da transação é obrigatório.")
        TransactionType type,

        @Schema(description = "Status da transação: ESTIMATED, PENDING, COMPLETED, CANCELLED")
        @NotNull(message = "O status da transação é obrigatório.")
        TransactionStatus status,

        @Schema(description = "Observações adicionais", example = "Referente ao consumo do mês de Outubro.")
        String notes
) {}
