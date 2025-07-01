package com.g47.cem.cemcontract;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for CEM Contract Service
 * 
 * This service manages contract operations including:
 * - Contract creation and management
 * - Digital signature workflow
 * - Customer account creation upon contract signing
 * - Email notifications for contract events
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableTransactionManagement
public class CemContractApplication {

	public static void main(String[] args) {
		SpringApplication.run(CemContractApplication.class, args);
	}

} 