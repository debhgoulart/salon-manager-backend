package com.manager.salon.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailsResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/not-found",
                "Recurso Não Encontrado",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetailsResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/business-rule-violation",
                "Violação de Regra de Negócio",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<ProblemDetailsResponse> handleScheduleConflict(ScheduleConflictException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/schedule-conflict",
                "Conflito de Horário",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ClosedPeriodException.class)
    public ResponseEntity<ProblemDetailsResponse> handleClosedPeriod(ClosedPeriodException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/closed-period",
                "Período Contábil Encerrado",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailsResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ProblemDetailsResponse.InvalidParam> params = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                    return new ProblemDetailsResponse.InvalidParam(field, error.getDefaultMessage());
                })
                .toList();

        ProblemDetailsResponse response = ProblemDetailsResponse.ofWithParams(
                "https://salonmanager.com/errors/validation-error",
                "Erro de Validação de Dados",
                HttpStatus.BAD_REQUEST.value(),
                "Um ou mais campos contêm valores inválidos.",
                request.getRequestURI(),
                params
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetailsResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/unauthorized",
                "Credenciais Inválidas",
                HttpStatus.UNAUTHORIZED.value(),
                "E-mail ou senha incorretos.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetailsResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/unauthorized",
                "Não Autorizado",
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetailsResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/forbidden",
                "Acesso Negado",
                HttpStatus.FORBIDDEN.value(),
                "Você não possui privilégios para executar esta ação.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado na requisição: {}", request.getRequestURI(), ex);
        ProblemDetailsResponse response = ProblemDetailsResponse.of(
                "https://salonmanager.com/errors/internal-server-error",
                "Erro Interno do Servidor",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno inesperado. Por favor, contate o administrador.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
