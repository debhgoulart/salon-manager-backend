package com.manager.salon.modules.auth;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.UnauthorizedException;
import com.manager.salon.modules.auth.dto.AuthResponse;
import com.manager.salon.modules.auth.dto.LoginRequest;
import com.manager.salon.modules.auth.dto.RefreshTokenRequest;
import com.manager.salon.modules.auth.dto.RegisterAdminRequest;
import com.manager.salon.modules.auth.dto.UserResponse;
import com.manager.salon.modules.auth.entity.User;
import com.manager.salon.modules.auth.entity.UserRole;
import com.manager.salon.modules.auth.repository.UserRepository;
import com.manager.salon.modules.auth.service.AuthService;
import com.manager.salon.security.JwtTokenService;
import com.manager.salon.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User("Admin", "admin@salon.com", "hash", UserRole.ROLE_ADMIN, true);
        sampleUser.setId(sampleUserId);
    }

    @Test
    @DisplayName("Deve autenticar com sucesso e retornar tokens JWT")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("admin@salon.com", "secret123");
        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal(sampleUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(jwtTokenService.generateAccessToken(sampleUserId, "admin@salon.com", "ROLE_ADMIN")).thenReturn("access-token-123");
        when(jwtTokenService.generateRefreshToken(sampleUserId, "admin@salon.com")).thenReturn("refresh-token-123");
        when(jwtTokenService.getExpirationHours()).thenReturn(2L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token-123", response.accessToken());
        assertEquals("refresh-token-123", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(7200L, response.expiresInSeconds());
        assertEquals("admin@salon.com", response.user().email());
    }

    @Test
    @DisplayName("Deve lançar exceção se credenciais forem inválidas")
    void shouldThrowExceptionWhenCredentialsInvalid() {
        LoginRequest request = new LoginRequest("admin@salon.com", "wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar login com usuário desativado")
    void shouldThrowExceptionWhenUserIsInactive() {
        sampleUser.setActive(false);
        LoginRequest request = new LoginRequest("admin@salon.com", "secret123");
        Authentication authentication = mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal(sampleUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Deve renovar token com sucesso quando refresh token for válido")
    void shouldRefreshTokenSuccessfully() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(jwtTokenService.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenService.getTokenType("valid-refresh-token")).thenReturn("REFRESH");
        when(jwtTokenService.getUserIdFromToken("valid-refresh-token")).thenReturn(sampleUserId);
        when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(sampleUser));
        when(jwtTokenService.generateAccessToken(sampleUserId, "admin@salon.com", "ROLE_ADMIN")).thenReturn("new-access-token");
        when(jwtTokenService.generateRefreshToken(sampleUserId, "admin@salon.com")).thenReturn("new-refresh-token");
        when(jwtTokenService.getExpirationHours()).thenReturn(2L);

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
    }

    @Test
    @DisplayName("Deve registrar novo administrador com sucesso")
    void shouldRegisterAdminSuccessfully() {
        RegisterAdminRequest request = new RegisterAdminRequest("Novo Admin", "novo@salon.com", "senha123");

        when(userRepository.existsByEmail("novo@salon.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed-pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        UserResponse response = authService.registerAdmin(request);

        assertNotNull(response);
        assertEquals("Novo Admin", response.name());
        assertEquals("novo@salon.com", response.email());
        assertEquals(UserRole.ROLE_ADMIN, response.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao registrar admin com e-mail duplicado")
    void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        RegisterAdminRequest request = new RegisterAdminRequest("Novo Admin", "existente@salon.com", "senha123");
        when(userRepository.existsByEmail("existente@salon.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.registerAdmin(request));
    }
}
