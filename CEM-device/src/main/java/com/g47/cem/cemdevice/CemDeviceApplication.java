package com.g47.cem.cemdevice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CEM Device Service Application
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.g47.cem.cemdevice.repository")
@EntityScan(basePackages = "com.g47.cem.cemdevice.entity")
@ComponentScan(basePackages = "com.g47.cem.cemdevice")
@EnableTransactionManagement
public class CemDeviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CemDeviceApplication.class, args);
    }

}
