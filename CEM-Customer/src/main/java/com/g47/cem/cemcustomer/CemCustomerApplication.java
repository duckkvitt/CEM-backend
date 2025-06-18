package com.g47.cem.cemcustomer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CEM Customer Service Application
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class CemCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CemCustomerApplication.class, args);
    }

}
