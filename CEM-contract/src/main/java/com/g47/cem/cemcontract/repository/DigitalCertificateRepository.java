package com.g47.cem.cemcontract.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.enums.CertificateStatus;
import com.g47.cem.cemcontract.enums.CertificateType;

/**
 * Repository interface for DigitalCertificate entity
 */
@Repository
public interface DigitalCertificateRepository extends JpaRepository<DigitalCertificate, Long> {
    
    /**
     * Find certificates by status
     */
    List<DigitalCertificate> findByStatus(CertificateStatus status);
    
    /**
     * Find active certificates
     */
    @Query("SELECT c FROM DigitalCertificate c WHERE c.status = 'ACTIVE' AND c.validFrom <= :now AND c.validTo > :now")
    List<DigitalCertificate> findActiveCertificates(@Param("now") LocalDateTime now);
    
    /**
     * Find certificate by serial number and issuer DN
     */
    Optional<DigitalCertificate> findBySerialNumberAndIssuerDN(String serialNumber, String issuerDN);
    
    /**
     * Find certificates by subject DN
     */
    List<DigitalCertificate> findBySubjectDNContaining(String subjectDN);
    
    /**
     * Find certificates by fingerprint
     */
    Optional<DigitalCertificate> findByFingerprintSha256(String fingerprintSha256);
    
    /**
     * Find certificates by type and status
     */
    List<DigitalCertificate> findByCertificateTypeAndStatus(CertificateType type, CertificateStatus status);
    
    /**
     * Find certificates created by user
     */
    List<DigitalCertificate> findByCreatedBy(String createdBy);
    
    /**
     * Find expired certificates
     */
    @Query("SELECT c FROM DigitalCertificate c WHERE c.validTo < :now")
    List<DigitalCertificate> findExpiredCertificates(@Param("now") LocalDateTime now);
    
    /**
     * Find certificates expiring soon
     */
    @Query("SELECT c FROM DigitalCertificate c WHERE c.status = 'ACTIVE' AND c.validTo > :now AND c.validTo <= :expiryDate")
    List<DigitalCertificate> findCertificatesExpiringSoon(@Param("now") LocalDateTime now, @Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Count active certificates
     */
    @Query("SELECT COUNT(c) FROM DigitalCertificate c WHERE c.status = 'ACTIVE' AND c.validFrom <= :now AND c.validTo > :now")
    long countActiveCertificates(@Param("now") LocalDateTime now);
} 