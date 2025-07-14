package com.g47.cem.cemcontract.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.DigitalSignatureRecord;
import com.g47.cem.cemcontract.enums.SignatureStatus;
import com.g47.cem.cemcontract.enums.SignerType;

/**
 * Repository interface for DigitalSignatureRecord entity
 */
@Repository
public interface DigitalSignatureRecordRepository extends JpaRepository<DigitalSignatureRecord, Long> {
    
    /**
     * Find signatures by contract ID
     */
    List<DigitalSignatureRecord> findByContractIdOrderBySignedAtAsc(Long contractId);
    
    /**
     * Find signatures by contract ID and signer type
     */
    List<DigitalSignatureRecord> findByContractIdAndSignerType(Long contractId, SignerType signerType);
    
    /**
     * Find signatures by signer ID and type
     */
    List<DigitalSignatureRecord> findBySignerIdAndSignerType(Long signerId, SignerType signerType);
    
    /**
     * Find signatures by certificate ID
     */
    List<DigitalSignatureRecord> findByCertificateId(Long certificateId);
    
    /**
     * Find signatures by status
     */
    List<DigitalSignatureRecord> findByStatus(SignatureStatus status);
    
    /**
     * Find valid signatures for contract
     */
    @Query("SELECT s FROM DigitalSignatureRecord s WHERE s.contract.id = :contractId AND s.status = 'VALID' ORDER BY s.signedAt ASC")
    List<DigitalSignatureRecord> findValidSignaturesByContract(@Param("contractId") Long contractId);
    
    /**
     * Find signatures by contract and signature field name
     */
    Optional<DigitalSignatureRecord> findByContractIdAndSignatureFieldName(Long contractId, String signatureFieldName);
    
    /**
     * Check if contract has valid signature by specific signer type
     */
    @Query("SELECT COUNT(s) > 0 FROM DigitalSignatureRecord s WHERE s.contract.id = :contractId AND s.signerType = :signerType AND s.status = 'VALID'")
    boolean hasValidSignatureByType(@Param("contractId") Long contractId, @Param("signerType") SignerType signerType);
    
    /**
     * Find signatures signed within date range
     */
    @Query("SELECT s FROM DigitalSignatureRecord s WHERE s.signedAt >= :startDate AND s.signedAt <= :endDate ORDER BY s.signedAt DESC")
    List<DigitalSignatureRecord> findSignaturesInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find signatures that need verification
     */
    @Query("SELECT s FROM DigitalSignatureRecord s WHERE s.status = 'PENDING_VERIFICATION' OR s.lastVerifiedAt IS NULL OR s.lastVerifiedAt < :cutoffDate")
    List<DigitalSignatureRecord> findSignaturesNeedingVerification(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count signatures by status
     */
    @Query("SELECT s.status, COUNT(s) FROM DigitalSignatureRecord s GROUP BY s.status")
    List<Object[]> countSignaturesByStatus();
    
    /**
     * Find signatures with timestamp
     */
    @Query("SELECT s FROM DigitalSignatureRecord s WHERE s.timestampToken IS NOT NULL AND s.timestampVerified = true")
    List<DigitalSignatureRecord> findTimestampedSignatures();
    
    /**
     * Find signatures by IP address (for audit)
     */
    List<DigitalSignatureRecord> findByIpAddress(String ipAddress);
    
    /**
     * Find recent signatures by signer
     */
    @Query("SELECT s FROM DigitalSignatureRecord s WHERE s.signerEmail = :signerEmail AND s.signedAt >= :sinceDate ORDER BY s.signedAt DESC")
    List<DigitalSignatureRecord> findRecentSignaturesBySigner(@Param("signerEmail") String signerEmail, @Param("sinceDate") LocalDateTime sinceDate);
} 