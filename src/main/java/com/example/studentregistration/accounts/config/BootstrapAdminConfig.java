package com.example.studentregistration.accounts.config;

import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.accounts.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class BootstrapAdminConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminConfig.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bootstrap.admin.email:admin@example.com}")
    private String email;

    @Value("${bootstrap.admin.password:admin12345}")
    private String password;

    @Value("${bootstrap.admin.name:System Admin}")
    private String name;

    public BootstrapAdminConfig(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // මෙතන අපි check කරන්නේ නෑ. හැම වතාවකම අලුත් admin කෙනෙක් හදනවා (Test කරන්න).
        // if (adminRepository.existsAnyActiveAdmin()) {
        //     log.info("Bootstrap admin skipped (already exists).");
        //     return;
        // }

        Instant now = Instant.now();
        Admin admin = new Admin(
                "ADM_" + UUID.randomUUID(),
                name,
                email.trim().toLowerCase(),
                passwordEncoder.encode(password),
                AccountStatus.ACTIVE,
                now,
                now
        );
        adminRepository.save(admin);
        log.info("Bootstrap admin created: {}", email);
    }
}