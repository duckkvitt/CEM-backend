package com.g47.cem.cemcontract.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.ContractDetail;

/**
 * Repository interface for ContractDetail entity
 */
@Repository
public interface ContractDetailRepository extends JpaRepository<ContractDetail, Long> {
    
    /**
     * Find contract details by contract ID
     */
    List<ContractDetail> findByContractId(Long contractId);
    
    /**
     * Find contract details by device ID
     */
    List<ContractDetail> findByDeviceId(Long deviceId);
    
    /**
     * Find contract details by work code
     */
    List<ContractDetail> findByWorkCodeContaining(String workCode);
    
    /**
     * Find contract details by service name
     */
    List<ContractDetail> findByServiceNameContaining(String serviceName);
    
    /**
     * Calculate total value for a contract
     */
    @Query("SELECT COALESCE(SUM(cd.totalPrice), 0) FROM ContractDetail cd WHERE cd.contract.id = :contractId")
    java.math.BigDecimal calculateTotalValueByContractId(@Param("contractId") Long contractId);
    
    /**
     * Find contract details with warranty
     */
    @Query("SELECT cd FROM ContractDetail cd WHERE cd.warrantyMonths > 0")
    List<ContractDetail> findDetailsWithWarranty();
    
    /**
     * Find contract details by contract and device
     */
    List<ContractDetail> findByContractIdAndDeviceId(Long contractId, Long deviceId);
    
    /**
     * Count contract details by contract ID
     */
    Integer countByContractId(Long contractId);
    
    /**
     * Delete contract details by contract ID
     */
    void deleteByContractId(Long contractId);
} 