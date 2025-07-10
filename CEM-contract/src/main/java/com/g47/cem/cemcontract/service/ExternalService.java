package com.g47.cem.cemcontract.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    @Value("${app.services.auth-service.admin-username:admin}")
    private String adminUsername;

    @Value("${app.services.auth-service.admin-password:password}")
    private String adminPassword;

    private Mono<String> fetchAdminToken() {
        String baseAuthUrl = authServiceUrl.replaceAll("/api/auth$", "");
        return webClient.post()
                .uri(baseAuthUrl + "/v1/auth/login")
                .bodyValue(new LoginRequest(adminUsername, adminPassword))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::getAccessToken);
    }

    public Mono<UserResponse> createUser(CreateUserRequest createUserRequest) {
        String baseAuthUrl = authServiceUrl.replaceAll("/api/auth$", "");
        return fetchAdminToken().flatMap(token ->
            webClient.post()
                .uri(baseAuthUrl + "/v1/auth/admin/create-user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(createUserRequest)
                .retrieve()
                .bodyToMono(UserResponse.class)
        );
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
        try {
            String baseCustomerUrl = customerServiceUrl.replaceAll("/api/customer$", "");
            String url = baseCustomerUrl + "/v1/customers/" + customerId;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<CustomerDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, CustomerDto.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("Failed to fetch customer info for ID: {}, status: {}", customerId, response.getStatusCode());
                return createPlaceholderCustomer(customerId);
            }
        } catch (Exception e) {
            log.error("Error fetching customer info for ID: {}", customerId, e);
            return createPlaceholderCustomer(customerId);
        }
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
    
    private CustomerDto createPlaceholderCustomer(Long customerId) {
        CustomerDto placeholder = new CustomerDto();
        placeholder.setId(customerId);
        placeholder.setCompanyName("KHÁCH HÀNG");
        placeholder.setContactName("NGƯỜI ĐẠI DIỆN");
        placeholder.setAddress("ĐỊA CHỈ KHÁCH HÀNG");
        placeholder.setPhone("");
        placeholder.setEmail("");
        return placeholder;
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
