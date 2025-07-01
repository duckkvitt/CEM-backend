package com.g47.cem.cemcontract.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.g47.cem.cemcontract.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service for communicating with external microservices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalService {
    
    private final WebClient webClient;
    
    @Value("${app.services.customer-service.url}")
    private String customerServiceUrl;
    
    @Value("${app.services.device-service.url}")
    private String deviceServiceUrl;
    
    @Value("${app.services.auth-service.url}")
    private String authServiceUrl;
    
    /**
     * Get customer information by ID
     */
    public Map<String, Object> getCustomerById(Long customerId, String authToken) {
        try {
            return webClient.get()
                    .uri(customerServiceUrl + "/api/customers/" + customerId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to get customer info for ID: {}", customerId, e);
            throw new BusinessException("Could not retrieve customer information");
        }
    }
    
    /**
     * Get device information by ID
     */
    public Map<String, Object> getDeviceById(Long deviceId, String authToken) {
        try {
            return webClient.get()
                    .uri(deviceServiceUrl + "/api/devices/" + deviceId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to get device info for ID: {}", deviceId, e);
            // Don't throw exception for device - it's optional
            return null;
        }
    }
    
    /**
     * Get user information by ID
     */
    public Map<String, Object> getUserById(Long userId, String authToken) {
        try {
            return webClient.get()
                    .uri(authServiceUrl + "/api/users/" + userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to get user info for ID: {}", userId, e);
            throw new BusinessException("Could not retrieve user information");
        }
    }
    
    /**
     * Create customer account when contract is signed
     */
    public Map<String, Object> createCustomerAccount(
            String customerEmail, 
            String customerName, 
            String tempPassword,
            String authToken) {
        try {
            Map<String, Object> requestBody = Map.of(
                "email", customerEmail,
                "name", customerName,
                "password", tempPassword,
                "role", "CUSTOMER",
                "accountStatus", "ACTIVE"
            );
            
            return webClient.post()
                    .uri(authServiceUrl + "/api/auth/create-user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                    
        } catch (Exception e) {
            log.error("Failed to create customer account for email: {}", customerEmail, e);
            throw new BusinessException("Could not create customer account");
        }
    }
    
    /**
     * Check if customer account exists
     */
    public boolean customerAccountExists(String customerEmail, String authToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(authServiceUrl + "/api/users/check-email/" + customerEmail)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            return response != null && Boolean.TRUE.equals(response.get("exists"));
            
        } catch (Exception e) {
            log.error("Failed to check customer account existence for email: {}", customerEmail, e);
            return false;
        }
    }
    
    /**
     * Generate temporary password for customer
     */
    public String generateTempPassword() {
        // Generate a secure temporary password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
}
