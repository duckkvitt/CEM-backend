package com.g47.cem.cemcontract.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.ContractSignature;
import com.g47.cem.cemcontract.enums.SignatureType;
import com.g47.cem.cemcontract.enums.SignerType;

/**
 * Repository interface for ContractSignature entity
 */
@Repository
public interface ContractSignatureRepository extends JpaRepository<ContractSignature, Long> {
    
    /**
     * Find signatures by contract ID
     */
    List<ContractSignature> findByContractId(Long contractId);
    
    /**
     * Find signatures by contract ID ordered by signed date
     */
    List<ContractSignature> findByContractIdOrderBySignedAtDesc(Long contractId);
    
    /**
     * Find signatures by signer ID and type
     */
    List<ContractSignature> findBySignerIdAndSignerType(Long signerId, SignerType signerType);
    
    /**
     * Find signatures by signature type
     */
    List<ContractSignature> findBySignatureType(SignatureType signatureType);
    
    /**
     * Find signatures by contract and signer type
     */
    List<ContractSignature> findByContractIdAndSignerType(Long contractId, SignerType signerType);
    
    /**
     * Find latest signature for a contract
     */
    @Query("SELECT cs FROM ContractSignature cs WHERE cs.contract.id = :contractId " +
           "ORDER BY cs.signedAt DESC LIMIT 1")
    ContractSignature findLatestSignatureByContractId(@Param("contractId") Long contractId);
    
    /**
     * Check if contract has customer signature
     */
    boolean existsByContractIdAndSignerType(Long contractId, SignerType signerType);
    
    /**
     * Find signatures within date range
     */
    @Query("SELECT cs FROM ContractSignature cs WHERE cs.signedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.signedAt DESC")
    List<ContractSignature> findSignaturesBetweenDates(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count signatures by type
     */
    long countBySignatureType(SignatureType signatureType);
    
    /**
     * Count signatures by signer type
     */
    long countBySignerType(SignerType signerType);
    
    /**
     * Find digital signatures
     */
    @Query("SELECT cs FROM ContractSignature cs WHERE cs.signatureType IN ('DIGITAL', 'DIGITAL_IMAGE')")
    List<ContractSignature> findDigitalSignatures();
} 