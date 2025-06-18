package com.g47.cem.cemauthentication.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemauthentication.dto.request.CreateUserRequest;
import com.g47.cem.cemauthentication.dto.response.RoleResponse;
import com.g47.cem.cemauthentication.dto.response.UserResponse;
import com.g47.cem.cemauthentication.entity.AccountStatus;
import com.g47.cem.cemauthentication.entity.Role;
import com.g47.cem.cemauthentication.entity.User;
import com.g47.cem.cemauthentication.exception.BusinessException;
import com.g47.cem.cemauthentication.exception.ResourceNotFoundException;
import com.g47.cem.cemauthentication.repository.RoleRepository;
import com.g47.cem.cemauthentication.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Transactional
    public UserResponse createUser(CreateUserRequest request, String adminEmail) {
        log.info("Creating user account by admin: {} for email: {}", adminEmail, request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists", HttpStatus.CONFLICT);
        }

        // Find the role
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + request.getRoleId()));

        // Generate temporary password
        String temporaryPassword = emailService.generateTemporaryPassword();

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .status(AccountStatus.ACTIVE)
                .emailVerified(request.getEmailVerified())
                .emailVerificationToken(null)
                .loginAttempts(0)
                .createdBy(adminEmail)
                .build();

        user = userRepository.save(user);

        // Send account creation email asynchronously
        emailService.sendAccountCreationEmail(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                temporaryPassword
        );

        // Create user response
        UserResponse userResponse = mapToUserResponse(user);

        log.info("User account created successfully by admin: {} for email: {}", adminEmail, user.getEmail());

        return userResponse;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.info("Fetching all roles");
        return roleRepository.findAll()
                .stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void initializeDefaultRoles() {
        log.info("Initializing default roles");

        if (!roleRepository.existsByName("USER")) {
            Role userRole = new Role("USER", "Standard user with basic access");
            roleRepository.save(userRole);
            log.info("Created default USER role");
        }

        if (!roleRepository.existsByName("ADMIN")) {
            Role adminRole = new Role("ADMIN", "Administrator with full access");
            roleRepository.save(adminRole);
            log.info("Created default ADMIN role");
        }

        if (!roleRepository.existsByName("MODERATOR")) {
            Role moderatorRole = new Role("MODERATOR", "Moderator with limited administrative access");
            roleRepository.save(moderatorRole);
            log.info("Created default MODERATOR role");
        }

        if (!roleRepository.existsByName("SUPER_ADMIN")) {
            Role superAdminRole = new Role("SUPER_ADMIN", "Super administrator with ultimate access");
            roleRepository.save(superAdminRole);
            log.info("Created default SUPER_ADMIN role");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        userResponse.setFullName(user.getFullName());
        userResponse.setRole(mapToRoleResponse(user.getRole()));
        return userResponse;
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return modelMapper.map(role, RoleResponse.class);
    }
} 