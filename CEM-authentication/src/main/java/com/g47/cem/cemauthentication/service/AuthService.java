package com.g47.cem.cemauthentication.service;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemauthentication.dto.request.ChangePasswordRequest;
import com.g47.cem.cemauthentication.dto.request.ForgotPasswordRequest;
import com.g47.cem.cemauthentication.dto.request.LoginRequest;
import com.g47.cem.cemauthentication.dto.request.ResetPasswordRequest;
import com.g47.cem.cemauthentication.dto.request.UpdateProfileRequest;
import com.g47.cem.cemauthentication.dto.response.AuthResponse;
import com.g47.cem.cemauthentication.dto.response.RoleResponse;
import com.g47.cem.cemauthentication.dto.response.UserResponse;
import com.g47.cem.cemauthentication.entity.User;
import com.g47.cem.cemauthentication.exception.BusinessException;
import com.g47.cem.cemauthentication.exception.ResourceNotFoundException;
import com.g47.cem.cemauthentication.repository.UserRepository;
import com.g47.cem.cemauthentication.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Check if account is locked
            if (user.isLocked()) {
                throw new BusinessException("Account is temporarily locked due to multiple failed login attempts", HttpStatus.FORBIDDEN);
            }

            // Generate tokens
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            // Update last login time and reset login attempts
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginAttempts(0);
            userRepository.save(user);

            // Create user response
            UserResponse userResponse = mapToUserResponse(user);

            log.info("Login successful for user: {}", user.getEmail());

            return AuthResponse.of(accessToken, refreshToken, jwtUtil.getExpirationTime(), userResponse);

        } catch (BadCredentialsException e) {
            handleFailedLogin(request.getEmail());
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        try {
            String username = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (jwtUtil.validateToken(refreshToken, user)) {
                String newAccessToken = jwtUtil.generateToken(user);
                String newRefreshToken = jwtUtil.generateRefreshToken(user);

                UserResponse userResponse = mapToUserResponse(user);

                log.info("Token refresh successful for user: {}", user.getEmail());

                return AuthResponse.of(newAccessToken, newRefreshToken, jwtUtil.getExpirationTime(), userResponse);
            } else {
                throw new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new BusinessException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
    }

    public void logout(String email) {
        log.info("Logout for user: {}", email);
        // In a real implementation, you might want to blacklist the token
        // For now, we'll just log the logout event
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if account is active
        if (user.getStatus() != com.g47.cem.cemauthentication.entity.AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active", HttpStatus.FORBIDDEN);
        }

        // Generate reset token
        String resetToken = emailService.generateResetToken();
        
        // Set reset token and expiration (15 minutes from now)
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(15));
        
        // Save user with reset token
        userRepository.save(user);

        // Send password reset email asynchronously
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                resetToken
        );

        log.info("Password reset email sent successfully for user: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if reset token exists and matches
        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(request.getResetToken())) {
            throw new BusinessException("Invalid reset token", HttpStatus.BAD_REQUEST);
        }

        // Check if reset token has expired
        if (user.getPasswordResetExpiresAt() == null || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        // Check if account is active
        if (user.getStatus() != com.g47.cem.cemauthentication.entity.AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active", HttpStatus.FORBIDDEN);
        }

        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        user.setLoginAttempts(0); // Reset failed login attempts
        
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String userEmail) {
        log.info("Change password request for user: {}", userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if account is active
        if (user.getStatus() != com.g47.cem.cemauthentication.entity.AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active", HttpStatus.FORBIDDEN);
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from current password", HttpStatus.BAD_REQUEST);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLoginAttempts(0); // Reset failed login attempts
        
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String userEmail) {
        log.info("Get profile request for user: {}", userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if account is active
        if (user.getStatus() != com.g47.cem.cemauthentication.entity.AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active", HttpStatus.FORBIDDEN);
        }

        UserResponse userResponse = mapToUserResponse(user);
        
        log.info("Profile retrieved successfully for user: {}", user.getEmail());
        
        return userResponse;
    }

    @Transactional
    public UserResponse updateUserProfile(UpdateProfileRequest request, String userEmail) {
        log.info("Update profile request for user: {}", userEmail);

        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if account is active
        if (user.getStatus() != com.g47.cem.cemauthentication.entity.AccountStatus.ACTIVE) {
            throw new BusinessException("Account is not active", HttpStatus.FORBIDDEN);
        }

        // Update user information
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);

        UserResponse userResponse = mapToUserResponse(user);
        
        log.info("Profile updated successfully for user: {}", user.getEmail());
        
        return userResponse;
    }

    private void handleFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int attempts = user.getLoginAttempts() + 1;
            user.setLoginAttempts(attempts);

            // Lock account after 5 failed attempts for 30 minutes
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                log.warn("Account locked for user: {} due to {} failed login attempts", email, attempts);
            }

            userRepository.save(user);
        });
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(mapToRoleResponse(user.getRole()));
        return userResponse;
    }

    private RoleResponse mapToRoleResponse(com.g47.cem.cemauthentication.entity.Role role) {
        return modelMapper.map(role, RoleResponse.class);
    }
} 