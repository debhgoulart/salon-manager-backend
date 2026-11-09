package com.manager.salon.modules.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeCreateRequest(
        @Schema(description = "Nome completo do colaborador", example = "Carlos Souza")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome não pode exceder 150 caracteres.")
        String name,

        @Schema(description = "CPF do colaborador", example = "123.456.789-00")
        @NotBlank(message = "O CPF é obrigatório.")
        @Size(min = 11, max = 14, message = "CPF deve ter entre 11 e 14 caracteres.")
        String cpf,

        @Schema(description = "Telefone para contato", example = "(11) 98765-4321")
        @NotBlank(message = "O telefone é obrigatório.")
        String phone,

        @Schema(description = "E-mail do colaborador", example = "carlos@salonmanager.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ter um formato válido.")
        String email,

        @Schema(description = "Cargo ou especialidade", example = "Cabeleireiro")
        @NotBlank(message = "O cargo é obrigatório.")
        String roleTitle,

        @Schema(description = "Data de contratação", example = "2024-01-15")
        @NotNull(message = "A data de contratação é obrigatória.")
        @PastOrPresent(message = "A data de contratação não pode ser no futuro.")
        LocalDate hireDate,

        @Schema(description = "Percentual padrão de comissão (0.00 a 100.00)", example = "40.00")
        @NotNull(message = "O percentual de comissão é obrigatório.")
        @DecimalMin(value = "0.00", message = "A comissão não pode ser menor que 0%.")
        @DecimalMax(value = "100.00", message = "A comissão não pode ultrapassar 100%.")
        BigDecimal defaultCommissionRate,

        @Schema(description = "Indica se deve criar usuário de login no sistema", example = "true")
        Boolean createLoginUser,

        @Schema(description = "Senha para login caso createLoginUser seja true", example = "senha123")
        String password
) {}
