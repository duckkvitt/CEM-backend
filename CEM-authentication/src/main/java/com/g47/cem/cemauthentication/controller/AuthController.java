package com.g47.cem.cemauthentication.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.g47.cem.cemauthentication.dto.request.UpdateUserRoleRequest;
import com.g47.cem.cemauthentication.dto.response.ApiResponse;
import com.g47.cem.cemauthentication.dto.response.AuthResponse;
import com.g47.cem.cemauthentication.dto.response.RoleResponse;
import com.g47.cem.cemauthentication.dto.response.UserResponse;
import com.g47.cem.cemauthentication.entity.AccountStatus;
import com.g47.cem.cemauthentication.entity.Role;
import com.g47.cem.cemauthentication.service.AuthService;
import com.g47.cem.cemauthentication.service.UserManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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



    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
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

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token (legacy)", description = "Generate new access token using refresh token")
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
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
    @Operation(summary = "Get all roles", description = "Retrieve all available roles (Admin/Manager/Support/TechLead)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPPORT_TEAM', 'LEAD_TECH')")
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

    @GetMapping("/admin/roles/by-name/{roleName}")
    @Operation(summary = "Get role by name", description = "Retrieve role by name (Admin/Manager only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
            @PathVariable String roleName,
            HttpServletRequest httpRequest) {
        
        log.info("Get role by name request received: {}", roleName);
        
        Role role = userManagementService.getRoleByName(roleName);
        RoleResponse roleResponse = new RoleResponse();
        roleResponse.setId(role.getId());
        roleResponse.setName(role.getName());
        roleResponse.setDescription(role.getDescription());
        
        ApiResponse<RoleResponse> response = ApiResponse.success(
            roleResponse, 
            "Role retrieved successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/roles/health")
    @Operation(summary = "Health check for default roles", description = "Verify all default roles exist in the database")
    public ResponseEntity<ApiResponse<Object>> checkDefaultRolesHealth() {
        List<String> requiredRoles = List.of("USER", "ADMIN", "STAFF", "MANAGER", "CUSTOMER", "SUPPORT_TEAM", "TECHNICIAN", "LEAD_TECH");
        List<String> missing = requiredRoles.stream()
            .filter(role -> !userManagementService.roleExists(role))
            .toList();
        if (missing.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("All default roles exist"));
        } else {
            return ResponseEntity.status(500).body(ApiResponse.error("Missing roles: " + missing, 500));
        }
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

    @GetMapping("/admin/users")
    @Operation(summary = "Get users with filters", description = "Retrieve paginated list of users with optional filters (Admin/Manager/Support/TechLead)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN', 'MANAGER', 'SUPPORT_TEAM', 'LEAD_TECH')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(
            @Parameter(description = "Search keyword (first name, last name or email)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by role ID")
            @RequestParam(required = false) Long roleId,
            @Parameter(description = "Filter by account status")
            @RequestParam(required = false) AccountStatus status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {

        log.info("Get users request received: search={}, roleId={}, status={}", search, roleId, status);

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserResponse> usersPage = userManagementService.getUsersWithFilters(search, roleId, status, pageable);

        ApiResponse<Page<UserResponse>> response = ApiResponse.success(usersPage, "Users retrieved successfully");
        response.setPath(httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a single user by ID (requires authentication)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        UserResponse user = userManagementService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(user, "User retrieved successfully");
        response.setPath(httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/users/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Set user's status to INACTIVE (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        UserResponse userResponse = userManagementService.deactivateUser(id);

        ApiResponse<UserResponse> response = ApiResponse.success(userResponse, "User deactivated successfully");
        response.setPath(httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/users/{id}/activate")
    @Operation(summary = "Activate user", description = "Set user's status to ACTIVE (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        UserResponse userResponse = userManagementService.activateUser(id);

        ApiResponse<UserResponse> response = ApiResponse.success(userResponse, "User activated successfully");
        response.setPath(httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/users/{id}/role")
    @Operation(summary = "Update user role", description = "Update user's role (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            HttpServletRequest httpRequest) {

        UserResponse userResponse = userManagementService.updateUserRole(id, request);

        ApiResponse<UserResponse> response = ApiResponse.success(userResponse, "User role updated successfully");
        response.setPath(httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }
}