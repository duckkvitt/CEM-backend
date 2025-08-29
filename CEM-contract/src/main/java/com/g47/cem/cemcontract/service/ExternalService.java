package com.g47.cem.cemcontract.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import com.g47.cem.cemcontract.dto.request.external.CreateUserRequest;
import com.g47.cem.cemcontract.dto.response.DeviceDto;
import com.g47.cem.cemcontract.dto.response.external.UserResponse;

import lombok.Data;
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
    @Value("${app.services.device-service.url}")
    private String deviceServiceUrl;
    private final RestTemplate restTemplate;
    // ... (rest of the ExternalService implementation, including all methods and static inner DTOs)

    public CustomerDto getCustomerInfo(Long customerId, String authToken) {
        if (customerId == null) {
            log.error("Customer ID is null. Cannot fetch customer info.");
            return null;
        }
        String url = customerServiceUrl + "/" + customerId;
        String tokenToUse = authToken;
        if (tokenToUse == null || tokenToUse.isBlank()) {
            // Try to extract from current request
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                String header = attrs.getRequest().getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    tokenToUse = header.substring(7);
                }
            }
        }
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url)
                .header("Authorization", "Bearer " + tokenToUse);
            ApiResponseWrapper response = request.retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                    clientResponse.bodyToMono(String.class).map(body -> {
                        log.error("Customer Service error for ID {}: {} - {}", customerId, clientResponse.statusCode().value(), body);
                        return new RuntimeException(clientResponse.statusCode() + " - " + body);
                    })
                )
                .bodyToMono(ApiResponseWrapper.class)
                .block();
            if (response == null || response.getData() == null) {
                log.error("Customer Service returned null or missing data for ID: {}", customerId);
                return null;
            }
            CustomerResponse customer = response.getData();
            CustomerDto dto = new CustomerDto();
            dto.setId(customer.getId());
            dto.setCompanyName(customer.getCompanyName());
            dto.setContactName(customer.getLegalRepresentative());
            dto.setAddress(customer.getCompanyAddress() != null ? customer.getCompanyAddress() : customer.getAddress());
            dto.setPhone(customer.getPhone());
            dto.setEmail(customer.getEmail());
            dto.setBusinessCode(null);
            dto.setTaxCode(customer.getCompanyTaxCode());
            dto.setTitle(customer.getTitle());
            dto.setIdentityNumber(customer.getIdentityNumber());
            dto.setIdentityIssueDate(customer.getIdentityIssueDate());
            dto.setIdentityIssuePlace(customer.getIdentityIssuePlace());
            dto.setFax(customer.getFax());
            return dto;
        } catch (Exception e) {
            log.error("Failed to fetch customer info from Customer Service for ID {}: {}", customerId, e.getMessage());
        return null;
        }
    }
    public RoleDto getRoleByName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            log.error("Role name is null or blank. Cannot fetch role info.");
            return null;
        }
        String url = authServiceUrl + "/admin/roles/by-name/" + roleName;
        String tokenToUse = extractAuthTokenOrServiceToken();
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenToUse)
                .accept(MediaType.APPLICATION_JSON);
            ApiRoleResponseWrapper response = request.retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                    clientResponse.bodyToMono(String.class).map(body -> {
                        log.error("Auth Service error for role '{}': {} - {}", roleName, clientResponse.statusCode().value(), body);
                        return new RuntimeException(clientResponse.statusCode() + " - " + body);
                    })
                )
                .bodyToMono(ApiRoleResponseWrapper.class)
                .block();
            if (response == null || response.getData() == null) {
                log.error("Auth Service returned null or missing data for role: {}", roleName);
                return null;
            }
            RoleResponse role = response.getData();
            return new RoleDto(role.getId(), role.getName(), role.getDescription());
        } catch (Exception e) {
            log.error("Failed to fetch role '{}' from Auth Service: {}", roleName, e.getMessage());
            return null;
        }
    }

    public Mono<UserResponse> createUser(CreateUserRequest createUserRequest) {
        String url = authServiceUrl + "/admin/create-user";
        String tokenToUse = extractAuthTokenOrServiceToken();
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenToUse)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createUserRequest)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                    clientResponse.bodyToMono(String.class).map(body -> {
                        log.error("Auth Service error creating user: {} - {}", clientResponse.statusCode().value(), body);
                        return new RuntimeException(clientResponse.statusCode() + " - " + body);
                    })
                )
                .bodyToMono(ApiUserResponseWrapper.class)
                .map(wrapper -> {
                    if (wrapper == null || wrapper.getData() == null) {
                        throw new RuntimeException("Auth Service returned null or missing data for user creation");
                    }
                    return wrapper.getData();
                });
    }

    public DeviceDto getDeviceInfo(Long deviceId, String authToken) {
        if (deviceId == null) {
            log.error("Device ID is null. Cannot fetch device info.");
            return null;
        }
        String url = deviceServiceUrl + "/devices/" + deviceId;
        String tokenToUse = authToken;
        if (tokenToUse == null || tokenToUse.isBlank()) {
            // Try to extract from current request
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                String header = attrs.getRequest().getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    tokenToUse = header.substring(7);
                }
            }
        }
        
        log.debug("Calling device service at: {} for deviceId: {}", url, deviceId);
        
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url);
            
            // Add authorization header if token is available
            if (tokenToUse != null && !tokenToUse.isBlank()) {
                request = request.header("Authorization", "Bearer " + tokenToUse);
                log.debug("Using authorization token for device service call");
            } else {
                log.warn("No authorization token available for device service call");
            }
            
            DeviceApiResponseWrapper response = request.retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                    clientResponse.bodyToMono(String.class).map(body -> {
                        log.error("Device Service error for ID {}: {} - {}", deviceId, clientResponse.statusCode().value(), body);
                        return new RuntimeException(clientResponse.statusCode() + " - " + body);
                    })
                )
                .bodyToMono(DeviceApiResponseWrapper.class)
                .block();
                
            if (response == null) {
                log.error("Device Service returned null response for ID: {}", deviceId);
                return null;
            }
            
            if (!response.isSuccess()) {
                log.error("Device Service returned unsuccessful response for ID {}: {}", deviceId, response.getMessage());
                return null;
            }
            
            if (response.getData() == null) {
                log.error("Device Service returned null data for ID: {}", deviceId);
                return null;
            }
            
            DeviceResponse device = response.getData();
            DeviceDto dto = new DeviceDto();
            dto.setId(device.getId());
            dto.setName(device.getName());
            dto.setModel(device.getModel());
            dto.setSerialNumber(device.getSerialNumber());
            dto.setPrice(device.getPrice());
            dto.setUnit(device.getUnit());
            
            log.debug("Successfully fetched device info for ID {}: name={}, model={}", deviceId, device.getName(), device.getModel());
            return dto;
        } catch (Exception e) {
            log.error("Failed to fetch device info from Device Service for ID {}: {}", deviceId, e.getMessage(), e);
            return null;
        }
    }

    public CustomerDto getCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            log.error("Email is null or blank. Cannot fetch customer by email.");
            return null;
        }
        String url = customerServiceUrl + "/email/" + email;
        try {
            WebClient.RequestHeadersSpec<?> request = webClient.get().uri(url)
                .header("Authorization", "Bearer " + extractAuthTokenOrServiceToken());
            ApiResponseWrapper response = request.retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                    clientResponse.bodyToMono(String.class).map(body -> {
                        log.error("Customer Service error for email {}: {} - {}", email, clientResponse.statusCode().value(), body);
                        return new RuntimeException(clientResponse.statusCode() + " - " + body);
                    })
                )
                .bodyToMono(ApiResponseWrapper.class)
                .block();
            if (response == null || response.getData() == null) {
                log.error("Customer Service returned null or missing data for email: {}", email);
                return null;
            }
            CustomerResponse customer = response.getData();
            CustomerDto dto = new CustomerDto();
            dto.setId(customer.getId());
            dto.setCompanyName(customer.getCompanyName());
            dto.setContactName(customer.getLegalRepresentative());
            dto.setAddress(customer.getCompanyAddress() != null ? customer.getCompanyAddress() : customer.getAddress());
            dto.setPhone(customer.getPhone());
            dto.setEmail(customer.getEmail());
            dto.setBusinessCode(null);
            dto.setTaxCode(customer.getCompanyTaxCode());
            dto.setTitle(customer.getTitle());
            dto.setIdentityNumber(customer.getIdentityNumber());
            dto.setIdentityIssueDate(customer.getIdentityIssueDate());
            dto.setIdentityIssuePlace(customer.getIdentityIssuePlace());
            dto.setFax(customer.getFax());
            dto.setTitle(customer.getTitle());
            dto.setIdentityNumber(customer.getIdentityNumber());
            dto.setIdentityIssueDate(customer.getIdentityIssueDate());
            dto.setIdentityIssuePlace(customer.getIdentityIssuePlace());
            dto.setFax(customer.getFax());
            return dto;
        } catch (Exception e) {
            log.error("Failed to fetch customer info from Customer Service for email {}: {}", email, e.getMessage());
            return null;
        }
    }

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

    // Helper classes for deserialization
    @Data
    private static class ApiResponseWrapper {
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
        private String name;
        private String email;
        private String phone;
        private String address;
        private String companyName;
        private String companyTaxCode;
        private String companyAddress;
        private String legalRepresentative;
        private String title;
        private String identityNumber;
        private String identityIssueDate;
        private String identityIssuePlace;
        private String fax;
        private Boolean isHidden;
        private String createdBy;
    }

    // Helper classes for deserialization
    @Data
    private static class RoleResponse {
        private Long id;
        private String name;
        private String description;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }
    @Data
    private static class ApiRoleResponseWrapper {
        private boolean success;
        private String message;
        private RoleResponse data;
        private Object errors;
        private String path;
        private Integer status;
    }
    @Data
    private static class ApiUserResponseWrapper {
        private boolean success;
        private String message;
        private UserResponse data;
        private Object errors;
        private String path;
        private Integer status;
    }

    @Data
    private static class DeviceApiResponseWrapper {
        private boolean success;
        private String message;
        private DeviceResponse data;
        private Object errors;
        private String path;
        private Integer status;
    }

    @Data
    private static class DeviceResponse {
        private Long id;
        private String name;
        private String model;
        private String serialNumber;
        private java.math.BigDecimal price;
        private String unit;
        private String status;
        private String createdBy;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }
} 