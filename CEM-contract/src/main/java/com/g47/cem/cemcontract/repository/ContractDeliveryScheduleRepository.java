package com.g47.cem.cemcontract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.ContractDeliverySchedule;

/**
 * Repository for ContractDeliverySchedule entity
 */
@Repository
public interface ContractDeliveryScheduleRepository extends JpaRepository<ContractDeliverySchedule, Long> {
    
    /**
     * Find all delivery schedules for a specific contract, ordered by sequence number
     */
    @Query("SELECT cds FROM ContractDeliverySchedule cds WHERE cds.contract.id = :contractId ORDER BY cds.sequenceNumber ASC")
    List<ContractDeliverySchedule> findByContractIdOrderBySequenceNumber(@Param("contractId") Long contractId);
    
    /**
     * Delete all delivery schedules for a specific contract
     */
    void deleteByContractId(Long contractId);
    
    /**
     * Count delivery schedules for a specific contract
     */
    long countByContractId(Long contractId);
} 