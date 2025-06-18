package com.g47.cem.cemauthentication.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemauthentication.dto.request.LoginRequest;
import com.g47.cem.cemauthentication.dto.request.RegisterRequest;
import com.g47.cem.cemauthentication.dto.response.AuthResponse;
import com.g47.cem.cemauthentication.dto.response.UserResponse;
import com.g47.cem.cemauthentication.entity.AccountStatus;
import com.g47.cem.cemauthentication.entity.Role;
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
    private final PasswordEncoder passwordEncoder;
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
            UserResponse userResponse = modelMapper.map(user, UserResponse.class);
            userResponse.setFullName(user.getFullName());

            log.info("Login successful for user: {}", user.getEmail());

            return AuthResponse.of(accessToken, refreshToken, jwtUtil.getExpirationTime(), userResponse);

        } catch (BadCredentialsException e) {
            handleFailedLogin(request.getEmail());
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Password confirmation does not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists", HttpStatus.CONFLICT);
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .loginAttempts(0)
                .build();

        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Create user response
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setFullName(user.getFullName());

        log.info("Registration successful for user: {}", user.getEmail());

        return AuthResponse.of(accessToken, refreshToken, jwtUtil.getExpirationTime(), userResponse);
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

                UserResponse userResponse = modelMapper.map(user, UserResponse.class);
                userResponse.setFullName(user.getFullName());

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

    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getEmail());
    }
} 