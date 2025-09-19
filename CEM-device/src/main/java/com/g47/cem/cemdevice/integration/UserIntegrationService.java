package com.g47.cem.cemdevice.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for integrating with Authentication service to fetch user data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${app.auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    /**
     * Get users by role name from authentication service using current user's token
     */
    public List<UserDto> getUsersByRole(String roleName, String bearerToken) {
        try {
            log.info("Fetching users with role: {} from authentication service", roleName);

            // First, get the role ID for the role name
            Long roleId = getRoleIdByName(roleName, bearerToken);
            if (roleId == null) {
                log.warn("Role not found: {}", roleName);
                return List.of();
            }

            // Build URL with parameters
            String url = UriComponentsBuilder.fromUriString(authServiceUrl + "/v1/auth/admin/users")
                    .queryParam("roleId", roleId)
                    .queryParam("status", "ACTIVE")
                    .queryParam("size", "100") // Get up to 100 users
                    .build()
                    .toUriString();

            // Set up headers with current user's token
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the API call
            ResponseEntity<ApiResponse<PageResponse<UserDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<PageResponse<UserDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                List<UserDto> users = response.getBody().getData().getContent();
                log.info("Successfully fetched {} users with role: {}", users != null ? users.size() : 0, roleName);
                return users != null ? users : List.of();
            } else {
                log.warn("Failed to fetch users with role: {} - Response body: {}", roleName, response.getBody());
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error fetching users with role: {}", roleName, e);
            return List.of();
        }
    }

    /**
     * Get role ID by role name using current user's token
     */
    private Long getRoleIdByName(String roleName, String bearerToken) {
        try {
            String url = authServiceUrl + "/v1/auth/admin/roles";

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<List<RoleDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<RoleDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                Optional<RoleDto> role = response.getBody().getData().stream()
                    .filter(r -> roleName.equals(r.getName()))
                    .findFirst();

                return role.map(RoleDto::getId).orElse(null);
            }

            return null;
        } catch (Exception e) {
            log.error("Error fetching role ID for role: {}", roleName, e);
            return null;
        }
    }

    /**
     * Get user by ID using current user's token
     */
    public Optional<UserDto> getUserById(Long userId, String bearerToken) {
        try {
            log.debug("Fetching user with ID: {} from authentication service", userId);
            // Build headers with bearer token
            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Use non-admin endpoint (controller base is /v1/auth inside Auth service)
            String userUrl = authServiceUrl + "/v1/auth/users/" + userId;
            try {
                ResponseEntity<ApiResponse<UserDto>> response = restTemplate.exchange(
                    userUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<ApiResponse<UserDto>>() {}
                );
                if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                    return Optional.of(response.getBody().getData());
                }
            } catch (Exception nonAdminEx) {
                log.warn("User lookup failed for user {}: {}", userId, nonAdminEx.getMessage());
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching user with ID: {}", userId, e);
            return Optional.empty();
        }
    }

    // DTOs for API responses
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageResponse<T> {
        private List<T> content;
        private int totalPages;
        private long totalElements;
        private int size;
        private int number;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private RoleDto role;
        private String status;
        private Boolean emailVerified;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoleDto {
        private Long id;
        private String name;
        private String description;
    }
}
