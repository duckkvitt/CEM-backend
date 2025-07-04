package com.g47.cem.cemauthentication.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemauthentication.entity.AccountStatus;
import com.g47.cem.cemauthentication.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    Optional<User> findByPasswordResetToken(String token);
    
    boolean existsByEmail(String email);
    
    Page<User> findByStatus(AccountStatus status, Pageable pageable);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = :attempts WHERE u.id = :userId")
    void updateLoginAttempts(@Param("userId") Long userId, @Param("attempts") Integer attempts);
    
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockUserAccount(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);
    
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified, u.emailVerificationToken = NULL WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") Long userId, @Param("verified") Boolean verified);

    // NEW CODE: add dynamic filter query for user search
    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:roleId IS NULL OR u.role.id = :roleId) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findUsersWithFilters(@Param("search") String search,
                                    @Param("roleId") Long roleId,
                                    @Param("status") AccountStatus status,
                                    Pageable pageable);
} 