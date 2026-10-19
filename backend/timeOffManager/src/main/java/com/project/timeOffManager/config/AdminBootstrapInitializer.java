package com.project.timeOffManager.config;

import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Role;
import com.project.timeOffManager.repository.BalanceRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class AdminBootstrapInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Value("${app.bootstrap.admin-email:alice.smith@timeoff.com}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password:password123}")
    private String adminPassword;

    @Value("${app.bootstrap.admin-first-name:Alice}")
    private String adminFirstName;

    @Value("${app.bootstrap.admin-last-name:Smith}")
    private String adminLastName;

    public AdminBootstrapInitializer(UserRepository userRepository,
                                     BalanceRepository balanceRepository,
                                     PasswordEncoder passwordEncoder,
                                     Environment environment) {
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isLocal = activeProfiles.isEmpty() || activeProfiles.contains("local");

        if (isLocal) {
            // Ensure local seeded users have a known dev password ('password123') if still using placeholder hash
            String devHash = passwordEncoder.encode(UserService.DEFAULT_INITIAL_PASSWORD);
            for (User user : userRepository.findAll()) {
                if (user.getPasswordHash() == null || !passwordEncoder.matches(UserService.DEFAULT_INITIAL_PASSWORD, user.getPasswordHash())) {
                    user.setPasswordHash(devHash);
                    userRepository.save(user);
                }
            }
        } else if (userRepository.count() == 0) {
            // In deployed/prod environments starting with an empty database, bootstrap the initial Admin account
            User admin = new User(
                    adminFirstName,
                    adminLastName,
                    adminEmail.trim().toLowerCase(),
                    passwordEncoder.encode(adminPassword),
                    null,
                    Role.ADMIN
            );
            User savedAdmin = userRepository.save(admin);
            balanceRepository.save(new Balance(null, savedAdmin, 28L, "Vacation", 28L));
            balanceRepository.save(new Balance(null, savedAdmin, 15L, "Sick Leave", 15L));
        }
    }
}
