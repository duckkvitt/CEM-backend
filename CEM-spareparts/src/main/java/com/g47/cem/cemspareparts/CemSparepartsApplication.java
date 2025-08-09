package com.g47.cem.cemspareparts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.g47.cem.cemspareparts.repository")
public class CemSparepartsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CemSparepartsApplication.class, args);
    }

}
