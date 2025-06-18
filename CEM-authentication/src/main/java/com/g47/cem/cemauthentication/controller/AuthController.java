package com.g47.cem.cemauthentication.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemauthentication.dto.request.LoginRequest;
import com.g47.cem.cemauthentication.dto.request.RegisterRequest;
import com.g47.cem.cemauthentication.dto.response.ApiResponse;
import com.g47.cem.cemauthentication.dto.response.AuthResponse;
import com.g47.cem.cemauthentication.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login request received for email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.login(request);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
            authResponse, 
            "Login successful"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Registration request received for email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.register(request);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
            authResponse, 
            "Registration successful"
        );
        response.setPath(httpRequest.getRequestURI());
        response.setStatus(HttpStatus.CREATED.value());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
        
        String refreshToken = authHeader.replace("Bearer ", "");
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        
        ApiResponse<AuthResponse> response = ApiResponse.success(
            authResponse, 
            "Token refresh successful"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout current user")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        authService.logout(authentication.getName());
        
        ApiResponse<Void> response = ApiResponse.success("Logout successful");
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address", description = "Verify user email using verification token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token,
            HttpServletRequest httpRequest) {
        
        authService.verifyEmail(token);
        
        ApiResponse<Void> response = ApiResponse.success("Email verification successful");
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
}