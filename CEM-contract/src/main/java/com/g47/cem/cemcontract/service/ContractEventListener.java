package com.g47.cem.cemcontract.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.g47.cem.cemcontract.dto.request.external.CreateUserRequest;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.event.SellerSignedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractEventListener {

    private final ExternalService externalService;
    private final EmailService emailService;

    @EventListener
    public void handleSellerSignedEvent(SellerSignedEvent event) {
        Contract contract = event.getContract();
        log.info("Handling SellerSignedEvent for contract ID: {}", contract.getId());

        // Extract customer info from contract's buyer details (assuming stored in description or a new field)
        // This part needs a robust way to get buyer details.
        // For demonstration, we'll use placeholder data.
        String customerEmail = "customer+" + contract.getId() + "@example.com"; // Placeholder
        String customerFirstName = "Customer"; // Placeholder
        String customerLastName = "User"; // Placeholder
        String tempPassword = "password123"; // This should be securely generated

        // 1. Create User in Auth Service
        CreateUserRequest createUserRequest = new CreateUserRequest(
                customerEmail,
                customerFirstName,
                customerLastName,
                null, // phone
                3L,   // Assuming Role ID 3 is CUSTOMER
                true
        );

        externalService.createUser(createUserRequest)
            .doOnSuccess(userResponse -> {
                log.info("Successfully created user for contract {}: {}", contract.getId(), userResponse.getEmail());
                
                // Call the correct EmailService method
                emailService.sendContractSignedNotification(
                    userResponse.getEmail(),
                    customerFirstName + " " + customerLastName,
                    contract.getContractNumber(),
                    tempPassword // A securely generated password should be used here
                );
                
                log.info("Sent signing invitation email to {}", userResponse.getEmail());
            })
            .doOnError(error -> {
                log.error("Failed to create user for contract {}: {}", contract.getId(), error.getMessage());
                // TODO: Implement retry logic or notify admin
            })
            .onErrorResume(e -> Mono.empty())
            .subscribe();
    }
} 