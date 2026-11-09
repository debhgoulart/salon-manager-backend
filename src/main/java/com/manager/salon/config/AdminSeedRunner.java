package com.manager.salon.config;

import com.manager.salon.modules.auth.entity.User;
import com.manager.salon.modules.auth.entity.UserRole;
import com.manager.salon.modules.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.name:Administrador do Salão}")
    private String adminName;

    @Value("${app.admin.email:admin@salonmanager.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin@123456}")
    private String adminPassword;

    public AdminSeedRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            log.info("Inicializando usuário administrador inicial: {}", adminEmail);
            User admin = new User(
                    adminName,
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    UserRole.ROLE_ADMIN,
                    true
            );
            userRepository.save(admin);
            log.info("Administrador padrão criado com sucesso.");
        } else {
            log.info("Usuário administrador já existente na base de dados.");
        }
    }
}
