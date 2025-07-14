package com.g47.cem.cemcontract.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import com.g47.cem.cemcontract.dto.request.external.CreateUserRequest;
import com.g47.cem.cemcontract.dto.response.external.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalService {

    private final WebClient webClient;
    @Value("${app.services.auth-service.url}")
    private String authServiceUrl; // e.g., http://localhost:8081/api/auth
    
    @Value("${app.services.customer-service.url}")
    private String customerServiceUrl;

    private final RestTemplate restTemplate;

    /**
     * Resolve current JWT token from the active HTTP request (set by JwtAuthenticationFilter).
     */
    private String getCurrentJwt() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        Object tokenAttr = attrs.getRequest().getAttribute("jwt");
        if (tokenAttr instanceof String token && !token.isBlank()) {
            return token;
        }
        String header = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    public Mono<UserResponse> createUser(CreateUserRequest createUserRequest) {
        String token = getCurrentJwt();
        if (token == null || token.isBlank()) {
            return Mono.error(new IllegalStateException("Authorization token not found for createUser call"));
        }

        String baseAuthUrl = authServiceUrl.replaceAll("/api/auth$", "");
        return webClient.post()
                .uri(baseAuthUrl + "/v1/auth/admin/create-user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(createUserRequest)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }
    
    public Mono<Object> getCustomerById(Long customerId, String authToken) {
        String baseCustomerUrl = customerServiceUrl.replaceAll("/api/customer$", "");
        return webClient.get()
                .uri(baseCustomerUrl + "/v1/customers/" + customerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .bodyToMono(Object.class);
    }

    public CustomerDto getCustomerInfo(Long customerId, String authToken) {
        if (authToken == null || authToken.isBlank()) {
            authToken = getCurrentJwt();
        }
        
        if (authToken == null || authToken.isBlank()) {
            log.error("No authorization token available for customer service call. Customer ID: {}", customerId);
            return null;
        }
        
        return getCustomerInfoWithRetry(customerId, authToken, 3);
    }
    
    private CustomerDto getCustomerInfoWithRetry(Long customerId, String authToken, int maxAttempts) {
        String baseCustomerUrl = customerServiceUrl.replaceAll("/api/customer$", "");
        String url = baseCustomerUrl + "/v1/customers/" + customerId;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(authToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                log.debug("Attempting to fetch customer info for ID: {} (attempt {}/{})", customerId, attempt, maxAttempts);
                ResponseEntity<CustomerDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, CustomerDto.class);
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.debug("Successfully fetched customer info for ID: {} on attempt {}", customerId, attempt);
                    return response.getBody();
                } else {
                    log.warn("Failed to fetch customer info for ID: {} on attempt {}. Status: {}", 
                            customerId, attempt, response.getStatusCode());
                    
                    if (attempt == maxAttempts) {
                        log.error("All {} attempts failed to fetch customer info for ID: {}. Final status: {}, URL: {}", 
                                 maxAttempts, customerId, response.getStatusCode(), url);
                        return null;
                    }
                }
            } catch (Exception e) {
                log.warn("Error fetching customer info for ID: {} on attempt {}. Error: {}", 
                        customerId, attempt, e.getMessage());
                
                if (attempt == maxAttempts) {
                    log.error("All {} attempts failed to fetch customer info for ID: {} from URL: {}. Final error: {}", 
                             maxAttempts, customerId, customerServiceUrl, e.getMessage(), e);
                    return null;
                }
            }
            
            // Wait before retry (exponential backoff)
            if (attempt < maxAttempts) {
                try {
                    long delay = (long) Math.pow(2, attempt - 1) * 1000; // 1s, 2s, 4s...
                    log.debug("Waiting {}ms before retry attempt {} for customer ID: {}", delay, attempt + 1, customerId);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted for customer ID: {}", customerId);
                    return null;
                }
            }
        }
        
        return null;
    }

    public UserResponse getUserById(Long userId, String authToken) {
        try {
            String baseAuthUrl = authServiceUrl.replaceAll("/api/auth$", "");
            String url = baseAuthUrl + "/v1/auth/admin/users/" + userId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<UserResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, UserResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Failed to fetch user info for ID: {}, status: {}", userId, response.getStatusCode());
                return null; // Or a placeholder
            }
        } catch (Exception e) {
            log.error("Error fetching user info for ID: {}", userId, e);
            return null; // Or a placeholder
        }
    }
    
    /**
     * Get role by name from Auth Service
     */
    public RoleDto getRoleByName(String roleName) {
        String token = getCurrentJwt();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authorization token not found for getRoleByName call");
        }

        try {
            String baseAuthUrl = authServiceUrl.replaceAll("/api/auth$", "");
            String url = baseAuthUrl + "/v1/auth/admin/roles/by-name/" + roleName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<RoleDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, RoleDto.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Failed to fetch role info for name: {}, status: {}", roleName, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching role info for name: {}", roleName, e);
            return null;
        }
    }
    

    
    /**
     * DTO for Customer information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDto {
        private Long id;
        private String companyName;
        private String contactName;
        private String address;
        private String phone;
        private String email;
        private String businessCode;
        private String taxCode;
    }

    /**
     * DTO for Role information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleDto {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @AllArgsConstructor
    private static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    private static class TokenResponse {
        private String accessToken;
    }
}
