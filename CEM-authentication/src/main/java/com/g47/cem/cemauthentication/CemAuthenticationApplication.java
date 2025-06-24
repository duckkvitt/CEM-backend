package com.g47.cem.cemauthentication;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.g47.cem.cemauthentication.service.UserManagementService;

@SpringBootApplication
@EnableJpaAuditing
public class CemAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CemAuthenticationApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserManagementService userManagementService) {
        return args -> {
            userManagementService.initializeDefaultRoles();
            userManagementService.initializeDefaultAdmin();
        };
    }
}
