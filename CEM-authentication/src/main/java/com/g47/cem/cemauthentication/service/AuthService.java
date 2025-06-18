package com.g47.cem.cemauthentication.service;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemauthentication.dto.request.LoginRequest;
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