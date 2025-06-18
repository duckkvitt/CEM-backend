package com.g47.cem.cemauthentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class CemAuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CemAuthenticationApplication.class, args);
    }

}
