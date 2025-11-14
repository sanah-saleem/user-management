package com.project.usermanagement.config;

import com.project.usermanagement.entity.User;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdmin {
    
    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            String adminEmail = System.getProperty("app.admin.email", "admin@example.com");
            String adminPass = System.getProperty("app.admin.password", "Admin!234");
            if (repo.findByEmail(adminEmail).isEmpty()) {
                var u = User.builder()
                        .email(adminEmail)
                        .fullName("System Admin")
                        .passwordHash(encoder.encode(adminPass))
                        .role(Role.ADMIN)
                        .status(AccountStatus.ACTIVE)
                        .build();
                repo.save(u);
                System.out.println("Seeded admin: " + adminEmail + " / (provided password)");   
            }
        };
    }

}
