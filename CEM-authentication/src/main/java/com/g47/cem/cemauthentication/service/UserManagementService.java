package com.g47.cem.cemauthentication.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.g47.cem.cemauthentication.event.UserCreatedEvent;
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
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.admin.email:admin@cem.local}")
    private String defaultAdminEmail;

    @Value("${app.admin.firstName:System}")
    private String defaultAdminFirstName;

    @Value("${app.admin.lastName:Administrator}")
    private String defaultAdminLastName;

    @Value("${app.admin.password:}")
    private String defaultAdminPassword;

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

        // Publish event to send account email AFTER transaction commits
        eventPublisher.publishEvent(new UserCreatedEvent(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                temporaryPassword));

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
        log.info("Initializing default roles (only if table is empty)");

        if (roleRepository.count() > 0) {
            log.info("Roles already present -> seeding skipped");
            return;
        }

        Role userRole = new Role("USER", "Standard user with basic access");
        Role adminRole = new Role("ADMIN", "Administrator with full access");
        Role staffRole = new Role("STAFF", "Staff member who works directly with customers");
        Role managerRole = new Role("MANAGER", "Manager with permission to oversee operations and make strategic decisions");
        Role supportRole = new Role("SUPPORT_TEAM", "Support team responsible for handling customer support requests");
        Role techRole = new Role("TECHNICIAN", "Technician handling maintenance and repairs");
        Role leadTechRole = new Role("LEAD_TECH", "Lead technician supervising technicians and liaising with support team");

        roleRepository.saveAll(List.of(userRole, adminRole, staffRole, managerRole, supportRole, techRole, leadTechRole));
        log.info("Seeded default roles list");
    }

    /**
     * Create a default administrator account on startup if it does not already exist.
     * A strong random password is generated using the EmailService utility method and
     * logged to the console. Make sure to store this password securely (e.g., in a
     * secret manager) after first launch.
     */
    @Transactional
    public void initializeDefaultAdmin() {
        // Determine the password to use – prefer configured value, otherwise generate a random one
        String resolvedPassword = (defaultAdminPassword != null && !defaultAdminPassword.isBlank())
                ? defaultAdminPassword
                : emailService.generateTemporaryPassword();

        // Ensure roles have been seeded
        Role adminRole = roleRepository.findByName("SUPER_ADMIN")
                .orElseGet(() -> roleRepository.findByName("ADMIN")
                        .orElseThrow(() -> new ResourceNotFoundException("Admin role not found")));

        userRepository.findByEmail(defaultAdminEmail).ifPresentOrElse(existing -> {
            // Admin exists – do NOT override role or password to avoid unintended changes
            log.info("Default admin account already exists – leaving role/password unchanged");
        }, () -> {
            // Create new admin user
            User admin = User.builder()
                    .email(defaultAdminEmail)
                    .password(passwordEncoder.encode(resolvedPassword))
                    .firstName(defaultAdminFirstName)
                    .lastName(defaultAdminLastName)
                    .role(adminRole)
                    .status(AccountStatus.ACTIVE)
                    .emailVerified(true)
                    .loginAttempts(0)
                    .createdBy("SYSTEM")
                    .build();

            userRepository.save(admin);
            log.warn("Default admin account created -> email: {} , password: {}", defaultAdminEmail, resolvedPassword);
        });
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

    // NEW CODE: Retrieve users with dynamic filters and pagination
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersWithFilters(String search, Long roleId, AccountStatus status, Pageable pageable) {
        // Ensure search parameter is non-null to avoid PostgreSQL lower(bytea) issue
        String safeSearch = (search == null || search.trim().isEmpty()) ? "" : search.trim();

        log.info("Fetching users with filters search={}, roleId={}, status={} page={} size={}", safeSearch, roleId, status, pageable.getPageNumber(), pageable.getPageSize());

        Page<User> page = userRepository.findUsersWithFilters(safeSearch, roleId, status, pageable);
        return page.map(this::mapToUserResponse);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        log.info("Deactivating user with id {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        user.setStatus(AccountStatus.INACTIVE);
        userRepository.save(user);

        return mapToUserResponse(user);
    }
} 