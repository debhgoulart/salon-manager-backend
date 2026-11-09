package com.manager.salon.modules.auth.service;

import com.manager.salon.exception.BusinessException;
import com.manager.salon.exception.ResourceNotFoundException;
import com.manager.salon.exception.UnauthorizedException;
import com.manager.salon.modules.auth.dto.AuthResponse;
import com.manager.salon.modules.auth.dto.LoginRequest;
import com.manager.salon.modules.auth.dto.RefreshTokenRequest;
import com.manager.salon.modules.auth.dto.RegisterAdminRequest;
import com.manager.salon.modules.auth.dto.UserResponse;
import com.manager.salon.modules.auth.entity.User;
import com.manager.salon.modules.auth.entity.UserRole;
import com.manager.salon.modules.auth.repository.UserRepository;
import com.manager.salon.security.JwtTokenService;
import com.manager.salon.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário desativado.");
        }

        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getId(), user.getEmail());

        long expiresInSeconds = jwtTokenService.getExpirationHours() * 3600;

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresInSeconds,
                mapToResponse(user)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtTokenService.validateToken(token) || !"REFRESH".equals(jwtTokenService.getTokenType(token))) {
            throw new UnauthorizedException("Refresh token inválido ou expirado.");
        }

        UUID userId = jwtTokenService.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário desativado.");
        }

        String newAccessToken = jwtTokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenService.generateRefreshToken(user.getId(), user.getEmail());

        long expiresInSeconds = jwtTokenService.getExpirationHours() * 3600;

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                expiresInSeconds,
                mapToResponse(user)
        );
    }

    @Transactional
    public UserResponse registerAdmin(RegisterAdminRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com o e-mail: " + request.email());
        }

        User user = new User(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password()),
                UserRole.ROLE_ADMIN,
                true
        );

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Usuário não autenticado.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }
}
