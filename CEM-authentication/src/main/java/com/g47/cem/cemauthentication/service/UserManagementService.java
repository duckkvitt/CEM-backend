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

        if (!roleRepository.existsByName("ADMINISTRATOR")) {
            Role adminRole = new Role("ADMINISTRATOR", "System Administrator with full access to all features and settings");
            roleRepository.save(adminRole);
            log.info("Created default ADMINISTRATOR role");
        }

        if (!roleRepository.existsByName("STAFF")) {
            Role staffRole = new Role("STAFF", "Staff member with access to daily operations and customer management");
            roleRepository.save(staffRole);
            log.info("Created default STAFF role");
        }

        if (!roleRepository.existsByName("MANAGER")) {
            Role managerRole = new Role("MANAGER", "Manager with access to staff management and advanced operations");
            roleRepository.save(managerRole);
            log.info("Created default MANAGER role");
        }

        if (!roleRepository.existsByName("CUSTOMER")) {
            Role customerRole = new Role("CUSTOMER", "Customer with access to their own account and services");
            roleRepository.save(customerRole);
            log.info("Created default CUSTOMER role");
        }

        if (!roleRepository.existsByName("SUPPORT_TEAM")) {
            Role supportRole = new Role("SUPPORT_TEAM", "Support team member with access to customer service functions");
            roleRepository.save(supportRole);
            log.info("Created default SUPPORT_TEAM role");
        }
    }

    @Transactional
    public void initializeDefaultAdmin() {
        log.info("Initializing default administrator account");
        
        // Check if admin account already exists
        if (!userRepository.existsByEmail("admin@cem.com")) {
            // Find ADMINISTRATOR role
            Role adminRole = roleRepository.findByName("ADMINISTRATOR")
                    .orElseThrow(() -> new BusinessException("ADMINISTRATOR role not found", HttpStatus.INTERNAL_SERVER_ERROR));
            
            // Create default admin user
            User admin = User.builder()
                    .email("admin@cem.com")
                    .password(passwordEncoder.encode("Admin@CEM2025"))
                    .firstName("System")
                    .lastName("Administrator")
                    .phone("+84901234567")
                    .role(adminRole)
                    .status(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .loginAttempts(0)
                    .createdBy("SYSTEM")
                    .build();
            
            userRepository.save(admin);
            log.info("Default administrator account created successfully: admin@cem.com");
        } else {
            log.info("Default administrator account already exists");
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

    @Transactional
    public void assignRole(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        user.setRole(role);
        userRepository.save(user);

        log.info("Role {} assigned to user {} successfully", role.getName(), user.getEmail());
    }

    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setStatus(AccountStatus.INACTIVE);
        userRepository.save(user);

        log.info("User {} deactivated successfully", user.getEmail());
    }
} 