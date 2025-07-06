package com.g47.cem.cemcontract.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
    private String authServiceUrl;
    
    @Value("${app.services.customer-service.url}")
    private String customerServiceUrl;

    private final RestTemplate restTemplate;

    public Mono<UserResponse> createUser(CreateUserRequest createUserRequest, String authToken) {
        return webClient.post()
                .uri(authServiceUrl + "/admin/create-user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .body(Mono.just(createUserRequest), CreateUserRequest.class)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }
    
    public Mono<Object> getCustomerById(Long customerId, String authToken) {
        return webClient.get()
                .uri(customerServiceUrl + "/customers/" + customerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .bodyToMono(Object.class);
    }

    public CustomerDto getCustomerInfo(Long customerId) {
        try {
            String url = customerServiceUrl + "/api/customers/" + customerId;
            ResponseEntity<CustomerDto> response = restTemplate.getForEntity(url, CustomerDto.class);
            
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
}
