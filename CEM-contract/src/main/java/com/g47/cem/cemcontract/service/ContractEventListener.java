package com.g47.cem.cemcontract.service;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.g47.cem.cemcontract.dto.request.external.CreateUserRequest;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.event.SellerSignedEvent;
import com.g47.cem.cemcontract.service.CustomerDto;
import com.g47.cem.cemcontract.service.RoleDto;

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

        // Get customer info from Customer service
        if (contract.getCustomerId() == null) {
            log.error("Contract {} has no customer ID. Cannot create user account.", contract.getId());
            return;
        }

        try {
            // Fetch customer info from customer service
            CustomerDto customer = externalService.getCustomerInfo(contract.getCustomerId(), null);
            
            if (customer == null) {
                log.error("Failed to fetch customer info from Customer Service for contract ID: {}. Customer ID: {}. " +
                         "This could be due to: 1) Customer service is down, 2) Authentication failed, 3) Customer not found, " +
                         "4) Network connectivity issues. Cannot create user account.", 
                         contract.getId(), contract.getCustomerId());
                return;
            }

            log.debug("Successfully fetched customer info: ID={}, Company={}, Contact={}, Email={}", 
                     customer.getId(), customer.getCompanyName(), customer.getContactName(), customer.getEmail());

            // Extract customer details
            String customerEmail = customer.getEmail();
            String customerName = customer.getContactName() != null ? customer.getContactName() : customer.getCompanyName();
            String companyName = customer.getCompanyName();
            
            // Validate customer email
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                log.error("Customer ID {} (Company: '{}', Contact: '{}') has no email address in the database. " +
                         "Email is required to create user account for contract {}. Please update customer email in Customer Service.", 
                         customer.getId(), customer.getCompanyName(), customer.getContactName(), contract.getId());
                return;
            }

            // Generate secure temporary password
            String tempPassword = generateSecurePassword();
            
            // Parse customer name into first and last name
            String[] nameParts = customerName != null ? customerName.trim().split("\\s+", 2) : new String[]{"Customer", "User"};
            String customerFirstName = nameParts.length > 0 ? nameParts[0] : "Customer";
            String customerLastName = nameParts.length > 1 ? nameParts[1] : (companyName != null ? companyName : "User");

            log.info("Creating user account for customer: {} ({})", customerName, customerEmail);

            // Get CUSTOMER role ID dynamically from Auth Service
            RoleDto customerRole = externalService.getRoleByName("CUSTOMER");
            if (customerRole == null) {
                log.error("CUSTOMER role not found in Auth Service. Cannot create user account for contract {}", contract.getId());
                return;
            }

            // Create User in Auth Service
            CreateUserRequest createUserRequest = new CreateUserRequest(
                    customerEmail,
                    customerFirstName,
                    customerLastName,
                    customer.getPhone(), // phone
                    customerRole.getId(),   // Use dynamically resolved CUSTOMER role ID
                    true
            );

            externalService.createUser(createUserRequest)
                .doOnSuccess(userResponse -> {
                    log.info("Successfully created user for contract {}: {} (User ID: {})", 
                            contract.getId(), userResponse.getEmail(), userResponse.getId());
                    
                    // TODO: Update customer record with userId for future reference
                    // This will allow linking Customer (CEM-Customer service) with User (CEM-Authentication service)
                    // For now, we log the mapping for manual verification
                    log.info("CUSTOMER MAPPING: Customer ID {} -> User ID {} (Email: {})", 
                            customer.getId(), userResponse.getId(), userResponse.getEmail());
                    
                    // Send notification email with account credentials
                    try {
                        emailService.sendContractSignedNotification(
                            userResponse.getEmail(),
                            customerName,
                            contract.getContractNumber(),
                            tempPassword
                        );
                        log.info("Sent contract signed notification email to {} for contract {}", 
                                userResponse.getEmail(), contract.getContractNumber());
                    } catch (Exception emailError) {
                        log.error("Failed to send email notification to {} for contract {}: {}", 
                                userResponse.getEmail(), contract.getContractNumber(), emailError.getMessage());
                        // Don't fail the whole process if email fails
                    }
                })
                .doOnError(error -> {
                    log.error("Failed to create user for contract {}: {}", contract.getId(), error.getMessage());
                    // TODO: Implement retry logic or notify admin
                })
                .onErrorResume(e -> Mono.empty())
                .subscribe();
                
        } catch (Exception e) {
            log.error("Error handling SellerSignedEvent for contract {}: {}", contract.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Generate a secure random password
     */
    private String generateSecurePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        
        return IntStream.range(0, 12)
                .map(i -> random.nextInt(chars.length()))
                .mapToObj(randomIndex -> String.valueOf(chars.charAt(randomIndex)))
                .collect(Collectors.joining());
    }
} 