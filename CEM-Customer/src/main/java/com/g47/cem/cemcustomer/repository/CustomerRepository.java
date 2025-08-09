package com.g47.cem.cemcustomer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcustomer.entity.Customer;

/**
 * Repository interface for Customer entity
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Page<Customer> findByIsHidden(Boolean isHidden, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.isHidden = false")
    Page<Customer> findAllVisible(Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE c.isHidden = true")
    Page<Customer> findAllHidden(Pageable pageable);
    
    @Query(value = "SELECT * FROM customers c WHERE " +
           "(:name IS NULL OR LOWER(c.name::text) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email::text) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phone IS NULL OR c.phone::text LIKE CONCAT('%', :phone, '%')) AND " +
           "(:isHidden IS NULL OR c.is_hidden = :isHidden)",
           countQuery = "SELECT COUNT(*) FROM customers c WHERE " +
           "(:name IS NULL OR LOWER(c.name::text) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email::text) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:phone IS NULL OR c.phone::text LIKE CONCAT('%', :phone, '%')) AND " +
           "(:isHidden IS NULL OR c.is_hidden = :isHidden)",
           nativeQuery = true)
    Page<Customer> findCustomersWithFilters(
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("isHidden") Boolean isHidden,
            Pageable pageable);
    
    @Query(value = "SELECT * FROM customers c WHERE " +
           "jsonb_array_length(c.tags) > 0 AND " +
           "c.tags::text ILIKE CONCAT('%\"', :tag, '\"%')", 
           nativeQuery = true)
    List<Customer> findByTag(@Param("tag") String tag);
} 