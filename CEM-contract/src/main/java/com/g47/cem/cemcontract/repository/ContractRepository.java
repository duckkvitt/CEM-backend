package com.g47.cem.cemcontract.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.enums.ContractStatus;

/**
 * Repository interface for Contract entity
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    
    /**
     * Find contract by contract number
     */
    Optional<Contract> findByContractNumber(String contractNumber);
    
    /**
     * Find contracts by customer ID
     */
    Page<Contract> findByCustomerIdAndIsHiddenFalse(Long customerId, Pageable pageable);
    
    /**
     * Find contracts by staff ID
     */
    Page<Contract> findByStaffIdAndIsHiddenFalse(Long staffId, Pageable pageable);
    
    /**
     * Find contracts by status
     */
    Page<Contract> findByStatusAndIsHiddenFalse(ContractStatus status, Pageable pageable);
    
    /**
     * Find contracts by a list of statuses
     */
    Page<Contract> findByStatusInAndIsHiddenFalse(List<ContractStatus> statuses, Pageable pageable);
    
    /**
     * Find signed contracts (both paper and digital)
     */
    @Query("SELECT c FROM Contract c WHERE c.status IN ('PAPER_SIGNED', 'DIGITALLY_SIGNED') AND c.isHidden = false")
    Page<Contract> findSignedContracts(Pageable pageable);
    
    /**
     * Find visible contracts (not hidden)
     */
    Page<Contract> findByIsHiddenFalse(Pageable pageable);
    
    /**
     * Find hidden contracts
     */
    Page<Contract> findByIsHiddenTrue(Pageable pageable);
    
    /**
     * Search contracts by title, description, or contract number
     */
    @Query("SELECT c FROM Contract c WHERE c.isHidden = false AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Contract> searchContracts(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find contracts created between dates
     */
    @Query("SELECT c FROM Contract c WHERE c.isHidden = false AND " +
           "c.createdAt BETWEEN :startDate AND :endDate")
    Page<Contract> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
    
    /**
     * Find contracts signed between dates
     */
    @Query("SELECT c FROM Contract c WHERE c.isHidden = false AND " +
           "c.signedAt BETWEEN :startDate AND :endDate")
    Page<Contract> findBySignedAtBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
    
    /**
     * Find contracts expiring soon
     */
    @Query("SELECT c FROM Contract c WHERE c.isHidden = false AND " +
           "c.endDate BETWEEN :today AND :expiryDate AND " +
           "c.status IN ('PAPER_SIGNED', 'DIGITALLY_SIGNED')")
    List<Contract> findContractsExpiringSoon(
            @Param("today") LocalDate today, 
            @Param("expiryDate") LocalDate expiryDate);
    
    /**
     * Count contracts by status
     */
    long countByStatusAndIsHiddenFalse(ContractStatus status);
    
    /**
     * Count contracts by customer
     */
    long countByCustomerIdAndIsHiddenFalse(Long customerId);
    
    /**
     * Count contracts by staff
     */
    long countByStaffIdAndIsHiddenFalse(Long staffId);
    
    /**
     * Check if contract number exists
     */
    boolean existsByContractNumber(String contractNumber);
    
    /**
     * Find next contract number sequence
     */
    @Query(value = "SELECT nextval('contract_number_seq')", nativeQuery = true)
    Long getNextContractNumber();
    
    // Additional methods for statistics
    Long countByStatus(ContractStatus status);
    Long countByIsHidden(Boolean isHidden);
    
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.endDate <= :cutoffDate AND c.endDate >= CURRENT_DATE")
    Long countExpiringContracts(@Param("cutoffDate") LocalDate cutoffDate);
    
    @Query("SELECT COALESCE(SUM(c.totalValue), 0) FROM Contract c")
    BigDecimal sumTotalValue();
    
    @Query("SELECT COALESCE(SUM(c.totalValue), 0) FROM Contract c WHERE c.status = :status")
    BigDecimal sumTotalValueByStatus(@Param("status") ContractStatus status);
    
    @Query("SELECT COALESCE(SUM(c.totalValue), 0) FROM Contract c WHERE c.status IN ('PAPER_SIGNED', 'DIGITALLY_SIGNED')")
    BigDecimal sumSignedContractValue();
    
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.createdAt >= :startOfMonth")
    Long countContractsInMonth(@Param("startOfMonth") LocalDateTime startOfMonth);
    
    @Query("SELECT COALESCE(SUM(c.totalValue), 0) FROM Contract c WHERE c.createdAt >= :startOfMonth AND c.status IN ('PAPER_SIGNED', 'DIGITALLY_SIGNED')")
    BigDecimal sumRevenueInMonth(@Param("startOfMonth") LocalDateTime startOfMonth);
    
    @Query("SELECT c FROM Contract c WHERE c.endDate <= :cutoffDate AND c.endDate >= CURRENT_DATE AND c.isHidden = false ORDER BY c.endDate ASC")
    List<Contract> findExpiringContracts(@Param("cutoffDate") LocalDate cutoffDate);
    
    /**
     * Count all visible contracts
     */
    long countByIsHiddenFalse();

    List<Contract> findByCustomerId(Long customerId);
    
    /**
     * Find contracts by customer ID with pagination
     */
    Page<Contract> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Find contracts by title or contract number containing search term
     */
    Page<Contract> findByTitleContainingIgnoreCaseOrContractNumberContainingIgnoreCase(
            String title, String contractNumber, Pageable pageable);
    
    /**
     * Find contracts by status list
     */
    Page<Contract> findByStatusIn(List<ContractStatus> statuses, Pageable pageable);
    
    /**
     * Find contracts by customer ID and title or contract number containing search term
     */
    Page<Contract> findByCustomerIdAndTitleContainingIgnoreCaseOrCustomerIdAndContractNumberContainingIgnoreCase(
            Long customerId1, String title, Long customerId2, String contractNumber, Pageable pageable);
    
    /**
     * Find contracts by customer ID and title or contract number containing search term, excluding hidden contracts
     */
    @Query("SELECT c FROM Contract c WHERE c.customerId = :customerId AND c.isHidden = false AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.contractNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Contract> findByCustomerIdAndTitleContainingIgnoreCaseOrContractNumberContainingIgnoreCaseAndIsHiddenFalse(
            @Param("customerId") Long customerId, 
            @Param("searchTerm") String searchTerm, 
            Pageable pageable);
    
    /**
     * Find contracts by customer ID and status list
     */
    Page<Contract> findByCustomerIdAndStatusIn(Long customerId, List<ContractStatus> statuses, Pageable pageable);
    
    /**
     * Find contracts by customer ID and status list, excluding hidden contracts
     */
    Page<Contract> findByCustomerIdAndStatusInAndIsHiddenFalse(Long customerId, List<ContractStatus> statuses, Pageable pageable);
} 