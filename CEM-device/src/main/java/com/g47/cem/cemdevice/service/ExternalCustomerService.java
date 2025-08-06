package com.g47.cem.cemdevice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalCustomerService {
    
    private final WebClient webClient;
    
    @Value("${app.services.customer-service.url}")
    private String customerServiceUrl;
    
    /**
     * Get customer information by email
     */
    public CustomerInfo getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            log.error("Email is null or blank. Cannot fetch customer by email.");
            return null;
        }
        
        String url = customerServiceUrl + "/email/" + email;
        String tokenToUse = extractAuthTokenOrServiceToken();
        
        try {
            log.debug("Calling customer service at: {}", url);
            
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
            
            // Add authorization header if token is available
            if (tokenToUse != null && !tokenToUse.isBlank()) {
                request = request.header("Authorization", "Bearer " + tokenToUse);
                log.debug("Using authorization token for customer service call");
            } else {
                log.warn("No authorization token available for customer service call");
            }
            
            CustomerApiResponse response = request.retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                    clientResponse.bodyToMono(String.class).map(body -> {
                        log.error("Customer Service error for email {}: {} - {}", email, clientResponse.statusCode().value(), body);
                        return new RuntimeException(clientResponse.statusCode() + " - " + body);
                    })
                )
                .bodyToMono(CustomerApiResponse.class)
                .block();
                
            if (response != null && response.isSuccess() && response.getData() != null) {
                CustomerResponse customer = response.getData();
                CustomerInfo info = new CustomerInfo();
                info.setId(customer.getId());
                info.setEmail(customer.getEmail());
                info.setCompanyName(customer.getCompanyName());
                info.setContactName(customer.getLegalRepresentative());
                log.debug("Successfully retrieved customer info for email {}: customerId={}", email, customer.getId());
                return info;
            } else {
                log.error("Customer service returned null or unsuccessful response for email: {}. Response: {}", email, response);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to fetch customer info from Customer Service for email {}: {}", email, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract authorization token from current request context
     */
    private String extractAuthTokenOrServiceToken() {
        // Try to extract from current request
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null && attrs.getRequest() != null) {
            String header = attrs.getRequest().getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        }
        return null;
    }
    
    @Data
    public static class CustomerInfo {
        private Long id;
        private String email;
        private String companyName;
        private String contactName;
    }
    
    @Data
    private static class CustomerApiResponse {
        private boolean success;
        private String message;
        private CustomerResponse data;
        private Object errors;
        private String path;
        private Integer status;
    }
    
    @Data
    private static class CustomerResponse {
        private Long id;
        private String email;
        private String companyName;
        private String legalRepresentative;
    }
} 