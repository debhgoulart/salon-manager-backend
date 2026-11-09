package com.manager.salon.modules.auth.controller;

import com.manager.salon.modules.auth.dto.AuthResponse;
import com.manager.salon.modules.auth.dto.LoginRequest;
import com.manager.salon.modules.auth.dto.RefreshTokenRequest;
import com.manager.salon.modules.auth.dto.RegisterAdminRequest;
import com.manager.salon.modules.auth.dto.UserResponse;
import com.manager.salon.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para login, renovação de token e gestão de acesso")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticação de usuário", description = "Valida credenciais (e-mail e senha) e retorna os tokens JWT de acesso e renovação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou usuário inativo."),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos.")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovação do token de acesso", description = "Gera um novo token de acesso através do refresh token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso."),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado.")
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-admin")
    @Operation(summary = "Cadastro de administrador", description = "Cadastra um novo usuário com perfil de administrador.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrador cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já existente.")
    })
    public ResponseEntity<UserResponse> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        UserResponse response = authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado", description = "Retorna os dados cadastrais e permissões do usuário atualmente logado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados recuperados com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autenticado.")
    })
    public ResponseEntity<UserResponse> me() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
