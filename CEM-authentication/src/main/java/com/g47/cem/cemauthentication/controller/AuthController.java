package com.g47.cem.cemauthentication.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemauthentication.dto.request.ChangePasswordRequest;
import com.g47.cem.cemauthentication.dto.request.CreateUserRequest;
import com.g47.cem.cemauthentication.dto.request.ForgotPasswordRequest;
import com.g47.cem.cemauthentication.dto.request.LoginRequest;
import com.g47.cem.cemauthentication.dto.request.ResetPasswordRequest;
import com.g47.cem.cemauthentication.dto.request.UpdateProfileRequest;
import com.g47.cem.cemauthentication.dto.response.ApiResponse;
import com.g47.cem.cemauthentication.dto.response.AuthResponse;
import com.g47.cem.cemauthentication.dto.response.RoleResponse;
import com.g47.cem.cemauthentication.dto.response.UserResponse;
import com.g47.cem.cemauthentication.service.AuthService;
import com.g47.cem.cemauthentication.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user management APIs")
public class AuthController {

    private final AuthService authService;
    private final UserManagementService userManagementService;

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
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        authService.logout(authentication.getName());
        
        ApiResponse<Void> response = ApiResponse.success("Logout successful");
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Send password reset email to user")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Forgot password request received for email: {}", request.getEmail());
        
        authService.forgotPassword(request);
        
        ApiResponse<Void> response = ApiResponse.success(
            "If an account with this email exists, a password reset email has been sent."
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Reset password request received for email: {}", request.getEmail());
        
        authService.resetPassword(request);
        
        ApiResponse<Void> response = ApiResponse.success(
            "Password has been reset successfully. You can now login with your new password."
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/create-user")
    @Operation(summary = "Create user account", description = "Create a new user account (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Create user request received by admin: {} for email: {}", 
                authentication.getName(), request.getEmail());
        
        UserResponse userResponse = userManagementService.createUser(request, authentication.getName());
        
        ApiResponse<UserResponse> response = ApiResponse.success(
            userResponse, 
            "User account created successfully. Login credentials have been sent to the user's email."
        );
        response.setPath(httpRequest.getRequestURI());
        response.setStatus(HttpStatus.CREATED.value());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/roles")
    @Operation(summary = "Get all roles", description = "Retrieve all available roles (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(
            HttpServletRequest httpRequest) {
        
        log.info("Get all roles request received");
        
        List<RoleResponse> roles = userManagementService.getAllRoles();
        
        ApiResponse<List<RoleResponse>> response = ApiResponse.success(
            roles, 
            "Roles retrieved successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Change password request received for user: {}", authentication.getName());
        
        authService.changePassword(request, authentication.getName());
        
        ApiResponse<Void> response = ApiResponse.success(
            "Password changed successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/assign-role")
    @Operation(summary = "Assign role to user", description = "Assign system rights by role (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @RequestParam Long userId,
            @RequestParam Long roleId,
            HttpServletRequest httpRequest) {
        
        log.info("Assigning role {} to user {} by admin", roleId, userId);
        
        userManagementService.assignRole(userId, roleId);
        
        ApiResponse<Void> response = ApiResponse.success(
            "Role assigned successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/deactivate-user")
    @Operation(summary = "Deactivate user", description = "Deactivate unused user accounts (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @RequestParam Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("Deactivating user {} by admin", userId);
        
        userManagementService.deactivateUser(userId);
        
        ApiResponse<Void> response = ApiResponse.success(
            "User deactivated successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Get current user profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Get profile request received for user: {}", authentication.getName());
        
        UserResponse userResponse = authService.getUserProfile(authentication.getName());
        
        ApiResponse<UserResponse> response = ApiResponse.success(
            userResponse,
            "Profile retrieved successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update current user profile information")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Update profile request received for user: {}", authentication.getName());
        
        UserResponse userResponse = authService.updateUserProfile(request, authentication.getName());
        
        ApiResponse<UserResponse> response = ApiResponse.success(
            userResponse,
            "Profile updated successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
}