package com.g47.cem.cemdevice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CEM Device Service Application
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class CemDeviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CemDeviceApplication.class, args);
    }

}
