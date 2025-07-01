package com.g47.cem.cemcontract.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.ContractHistory;
import com.g47.cem.cemcontract.enums.ContractAction;

/**
 * Repository interface for ContractHistory entity
 */
@Repository
public interface ContractHistoryRepository extends JpaRepository<ContractHistory, Long> {
    
    /**
     * Find history by contract ID ordered by most recent first
     */
    List<ContractHistory> findByContractIdOrderByChangedAtDesc(Long contractId);
    
    /**
     * Find history by contract ID with pagination
     */
    Page<ContractHistory> findByContractIdOrderByChangedAtDesc(Long contractId, Pageable pageable);
    
    /**
     * Find history by action type
     */
    List<ContractHistory> findByActionOrderByChangedAtDesc(ContractAction action);
    
    /**
     * Find history by user who made changes
     */
    List<ContractHistory> findByChangedByOrderByChangedAtDesc(String changedBy);
    
    /**
     * Find history within date range
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.changedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ch.changedAt DESC")
    List<ContractHistory> findHistoryBetweenDates(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find history by contract and action
     */
    List<ContractHistory> findByContractIdAndActionOrderByChangedAtDesc(Long contractId, ContractAction action);
    
    /**
     * Find latest history entry for a contract
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.contract.id = :contractId " +
           "ORDER BY ch.changedAt DESC LIMIT 1")
    ContractHistory findLatestHistoryByContractId(@Param("contractId") Long contractId);
    
    /**
     * Count history entries by action
     */
    long countByAction(ContractAction action);
    
    /**
     * Count history entries by user
     */
    long countByChangedBy(String changedBy);
    
    /**
     * Find signing activities
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.action IN ('SIGNED', 'DIGITAL_SIGNATURE_ADDED') " +
           "ORDER BY ch.changedAt DESC")
    List<ContractHistory> findSigningActivities();
    
    /**
     * Get audit trail for a contract
     */
    @Query("SELECT ch FROM ContractHistory ch WHERE ch.contract.id = :contractId " +
           "ORDER BY ch.changedAt ASC")
    List<ContractHistory> getAuditTrailByContractId(@Param("contractId") Long contractId);
} 