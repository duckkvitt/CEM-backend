package com.g47.cem.cemgateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication service APIs proxied through Gateway")
public class AuthProxyController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String AUTH_SERVICE_URL = "http://localhost:8081/api/auth";

    @PostMapping("/v1/auth/login")
    @Operation(
        summary = "User Login",
        description = "Authenticate user with email and password",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Bad request")
        }
    )
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Login credentials",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"email\":\"user@example.com\",\"password\":\"password123\"}")
                )
            )
            @RequestBody Object loginRequest,
            HttpServletRequest request) {
        return proxyRequest("/v1/auth/login", HttpMethod.POST, loginRequest, request);
    }

    @PostMapping("/v1/auth/register")
    @Operation(
        summary = "User Registration",
        description = "Register a new user account",
        responses = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "409", description = "User already exists")
        }
    )
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Registration details",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"email\":\"user@example.com\",\"password\":\"password123\",\"firstName\":\"John\",\"lastName\":\"Doe\"}")
                )
            )
            @RequestBody Object registerRequest,
            HttpServletRequest request) {
        return proxyRequest("/v1/auth/register", HttpMethod.POST, registerRequest, request);
    }

    @PostMapping("/v1/auth/refresh-token")
    @Operation(
        summary = "Refresh Access Token",
        description = "Get new access token using refresh token",
        responses = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
        }
    )
    public ResponseEntity<?> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token request",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"refreshToken\":\"your-refresh-token\"}")
                )
            )
            @RequestBody Object refreshRequest,
            HttpServletRequest request) {
        return proxyRequest("/v1/auth/refresh-token", HttpMethod.POST, refreshRequest, request);
    }

    @PostMapping("/v1/auth/logout")
    @Operation(
        summary = "User Logout",
        description = "Logout user and invalidate tokens",
        responses = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<?> logout(HttpServletRequest request) {
        return proxyRequest("/v1/auth/logout", HttpMethod.POST, null, request);
    }

    @GetMapping("/v1/auth/profile")
    @Operation(
        summary = "Get User Profile",
        description = "Get current user profile information",
        responses = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        return proxyRequest("/v1/auth/profile", HttpMethod.GET, null, request);
    }

    @PostMapping("/v1/auth/forgot-password")
    @Operation(
        summary = "Forgot Password",
        description = "Send password reset email",
        responses = {
            @ApiResponse(responseCode = "200", description = "Reset email sent"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    public ResponseEntity<?> forgotPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Email for password reset",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"email\":\"user@example.com\"}")
                )
            )
            @RequestBody Object forgotPasswordRequest,
            HttpServletRequest request) {
        return proxyRequest("/v1/auth/forgot-password", HttpMethod.POST, forgotPasswordRequest, request);
    }

    private ResponseEntity<?> proxyRequest(String path, HttpMethod method, Object body, HttpServletRequest request) {
        try {
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host")) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            }

            // Create request entity
            HttpEntity<?> entity = new HttpEntity<>(body, headers);

            // Make request to auth service
            String url = AUTH_SERVICE_URL + path;
            ResponseEntity<Object> response = restTemplate.exchange(url, method, entity, Object.class);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Gateway error: " + e.getMessage() + "\"}");
        }
    }
} 