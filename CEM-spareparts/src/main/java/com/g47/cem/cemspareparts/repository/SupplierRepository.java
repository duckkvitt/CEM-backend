package com.g47.cem.cemspareparts.repository;

import com.g47.cem.cemspareparts.entity.Supplier;
import com.g47.cem.cemspareparts.enums.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {
    
    Optional<Supplier> findByCompanyName(String companyName);
    
    Optional<Supplier> findByEmail(String email);
    
    /**
     * Find supplier by ID with eager loading of spare parts to prevent lazy loading issues.
     */
    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.spareParts WHERE s.id = :id")
    Optional<Supplier> findByIdWithSpareParts(@Param("id") Long id);
    
    Page<Supplier> findByStatus(SupplierStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT s FROM Supplier s LEFT JOIN FETCH s.spareParts sp WHERE sp.id = :sparePartId AND s.status = 'ACTIVE' ORDER BY s.id")
    List<Supplier> findActiveSuppliersBySparePartId(@Param("sparePartId") Long sparePartId);
    
    /**
     * Search suppliers with filters using PostgreSQL-compatible native query with proper text casting.
     * This prevents both lazy loading issues and PostgreSQL bytea type errors.
     */
    @Query(value = "SELECT DISTINCT s.* FROM suppliers s " +
           "LEFT JOIN supplier_spare_parts ssp ON s.id = ssp.supplier_id " +
           "LEFT JOIN spare_parts sp ON sp.id = ssp.spare_part_id " +
           "WHERE (:keyword IS NULL OR " +
           "LOWER(s.company_name::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.contact_person::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email::text) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)",
           countQuery = "SELECT COUNT(DISTINCT s.id) FROM suppliers s WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(s.company_name::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.contact_person::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email::text) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)",
           nativeQuery = true)
    Page<Supplier> findSuppliersWithFilters(@Param("keyword") String keyword,
                                           @Param("status") String status,
                                           Pageable pageable);
    
    /**
     * Alternative method using JPQL without LOWER() function to avoid PostgreSQL type issues.
     * Uses case-insensitive search with UPPER() which works better with PostgreSQL.
     */
    @Query("SELECT DISTINCT s FROM Supplier s LEFT JOIN FETCH s.spareParts WHERE " +
           "(:keyword IS NULL OR " +
           "UPPER(s.companyName) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
           "UPPER(s.contactPerson) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
           "UPPER(s.email) LIKE UPPER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.id")
    List<Supplier> findSuppliersWithFiltersEager(@Param("keyword") String keyword,
                                                 @Param("status") String status);
    
    /**
     * Count query using UPPER() instead of LOWER() for PostgreSQL compatibility.
     */
    @Query("SELECT COUNT(DISTINCT s) FROM Supplier s WHERE " +
           "(:keyword IS NULL OR " +
           "UPPER(s.companyName) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
           "UPPER(s.contactPerson) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
           "UPPER(s.email) LIKE UPPER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)")
    Long countSuppliersWithFilters(@Param("keyword") String keyword,
                                  @Param("status") String status);
    
    /**
     * Load suppliers by IDs with eager fetching of spare parts to prevent lazy loading issues.
     * This is used after getting supplier IDs from native queries.
     */
    @Query("SELECT DISTINCT s FROM Supplier s LEFT JOIN FETCH s.spareParts WHERE s.id IN :ids ORDER BY s.id")
    List<Supplier> findByIdsWithSpareParts(@Param("ids") List<Long> ids);
}