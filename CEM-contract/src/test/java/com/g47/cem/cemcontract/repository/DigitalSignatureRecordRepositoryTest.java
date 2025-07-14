package com.g47.cem.cemcontract.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.entity.DigitalSignatureRecord;
import com.g47.cem.cemcontract.enums.CertificateStatus;
import com.g47.cem.cemcontract.enums.CertificateType;
import com.g47.cem.cemcontract.enums.ContractStatus;
import com.g47.cem.cemcontract.enums.SignatureAlgorithm;
import com.g47.cem.cemcontract.enums.SignatureStatus;
import com.g47.cem.cemcontract.enums.SignerType;

@DataJpaTest
@ActiveProfiles("test")
class DigitalSignatureRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DigitalSignatureRecordRepository signatureRecordRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private DigitalCertificateRepository certificateRepository;

    private Contract testContract1;
    private Contract testContract2;
    private DigitalCertificate testCertificate;
    private DigitalSignatureRecord managerSignature;
    private DigitalSignatureRecord customerSignature;

    @BeforeEach
    void setUp() {
        // Create test certificate
        testCertificate = DigitalCertificate.builder()
                .alias("repo_test_alias")
                .subjectDN("CN=Repository Test Certificate")
                .issuerDN("CN=Test CA")
                .serialNumber("987654321")
                .certificateType(CertificateType.SELF_SIGNED)
                .status(CertificateStatus.ACTIVE)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusYears(1))
                .certificateData("test-certificate-data".getBytes())
                .privateKeyData("test-private-key-data".getBytes())
                .publicKeyData("test-public-key-data".getBytes())
                .keyAlgorithm("RSA")
                .keySize(2048)
                .signatureAlgorithm("SHA256withRSA")
                .fingerprintSha1("test-sha1-fingerprint")
                .fingerprintSha256("test-sha256-fingerprint")
                .createdBy("repository-test")
                .build();
        testCertificate = certificateRepository.save(testCertificate);

        // Create test contracts
        testContract1 = Contract.builder()
                .contractNumber("REPO-CONTRACT-001")
                .title("Repository Test Contract 1")
                .customerId(1L)
                .staffId(1L)
                .status(ContractStatus.ACTIVE)
                .filePath("repo-test-file-1")
                .isHidden(false)
                .createdBy("repository-test")
                .build();
        testContract1 = contractRepository.save(testContract1);

        testContract2 = Contract.builder()
                .contractNumber("REPO-CONTRACT-002")
                .title("Repository Test Contract 2")
                .customerId(2L)
                .staffId(1L)
                .status(ContractStatus.PENDING_CUSTOMER_SIGNATURE)
                .filePath("repo-test-file-2")
                .isHidden(false)
                .createdBy("repository-test")
                .build();
        testContract2 = contractRepository.save(testContract2);

        // Create test signature records
        managerSignature = DigitalSignatureRecord.builder()
                .contract(testContract1)
                .certificate(testCertificate)
                .signerType(SignerType.MANAGER)
                .signerId(1L)
                .signerName("Repository Test Manager")
                .signerEmail("repo.manager@test.com")
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                .status(SignatureStatus.VALID)
                .signatureImageData("manager-signature-data".getBytes())
                .signatureHash("manager-signature-hash")
                .hashAlgorithm("SHA-256")
                .pageNumber(1)
                .signatureX(100f)
                .signatureY(100f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .reason("Manager approval")
                .location("Manager Portal")
                .ipAddress("192.168.1.100")
                .userAgent("Manager Browser")
                .signatureVerified(true)
                .certificateVerified(true)
                .timestampVerified(false)
                .signedAt(LocalDateTime.now().minusHours(2))
                .build();
        managerSignature = signatureRecordRepository.save(managerSignature);

        customerSignature = DigitalSignatureRecord.builder()
                .contract(testContract1)
                .certificate(testCertificate)
                .signerType(SignerType.CUSTOMER)
                .signerId(2L)
                .signerName("Repository Test Customer")
                .signerEmail("repo.customer@test.com")
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                .status(SignatureStatus.VALID)
                .signatureImageData("customer-signature-data".getBytes())
                .signatureHash("customer-signature-hash")
                .hashAlgorithm("SHA-256")
                .pageNumber(1)
                .signatureX(400f)
                .signatureY(100f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .reason("Customer acceptance")
                .location("Customer Portal")
                .ipAddress("192.168.1.200")
                .userAgent("Customer Browser")
                .signatureVerified(true)
                .certificateVerified(true)
                .timestampVerified(false)
                .signedAt(LocalDateTime.now().minusHours(1))
                .build();
        customerSignature = signatureRecordRepository.save(customerSignature);

        // Flush to ensure data is persisted
        entityManager.flush();
    }

    @Test
    void testFindByContractIdOrderBySignedAtAsc() {
        // Act
        List<DigitalSignatureRecord> signatures = signatureRecordRepository
                .findByContractIdOrderBySignedAtAsc(testContract1.getId());

        // Assert
        assertNotNull(signatures);
        assertEquals(2, signatures.size());
        
        // Verify order (manager signed first, customer signed second)
        assertEquals(managerSignature.getId(), signatures.get(0).getId());
        assertEquals(customerSignature.getId(), signatures.get(1).getId());
        
        // Verify signatures belong to correct contract
        signatures.forEach(signature -> 
            assertEquals(testContract1.getId(), signature.getContract().getId())
        );
    }

    @Test
    void testFindByContractIdOrderBySignedAtAsc_EmptyResult() {
        // Act
        List<DigitalSignatureRecord> signatures = signatureRecordRepository
                .findByContractIdOrderBySignedAtAsc(testContract2.getId());

        // Assert
        assertNotNull(signatures);
        assertTrue(signatures.isEmpty());
    }

    @Test
    void testFindByContractIdOrderBySignedAtAsc_NonExistentContract() {
        // Act
        List<DigitalSignatureRecord> signatures = signatureRecordRepository
                .findByContractIdOrderBySignedAtAsc(99999L);

        // Assert
        assertNotNull(signatures);
        assertTrue(signatures.isEmpty());
    }

    @Test
    void testHasValidSignatureByType_Manager_True() {
        // Act
        boolean hasValidSignature = signatureRecordRepository
                .hasValidSignatureByType(testContract1.getId(), SignerType.MANAGER);

        // Assert
        assertTrue(hasValidSignature);
    }

    @Test
    void testHasValidSignatureByType_Customer_True() {
        // Act
        boolean hasValidSignature = signatureRecordRepository
                .hasValidSignatureByType(testContract1.getId(), SignerType.CUSTOMER);

        // Assert
        assertTrue(hasValidSignature);
    }

    @Test
    void testHasValidSignatureByType_NonExistentType_False() {
        // Act
        boolean hasValidSignature = signatureRecordRepository
                .hasValidSignatureByType(testContract2.getId(), SignerType.MANAGER);

        // Assert
        assertFalse(hasValidSignature);
    }

    @Test
    void testHasValidSignatureByType_InvalidStatus_False() {
        // Arrange - Create signature with invalid status
        DigitalSignatureRecord invalidSignature = DigitalSignatureRecord.builder()
                .contract(testContract2)
                .certificate(testCertificate)
                .signerType(SignerType.MANAGER)
                .signerId(3L)
                .signerName("Invalid Signature Manager")
                .signerEmail("invalid@test.com")
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                .status(SignatureStatus.INVALID) // Invalid status
                .signatureImageData("invalid-signature".getBytes())
                .signatureHash("invalid-hash")
                .signatureVerified(false)
                .certificateVerified(false)
                .build();
        signatureRecordRepository.save(invalidSignature);
        entityManager.flush();

        // Act
        boolean hasValidSignature = signatureRecordRepository
                .hasValidSignatureByType(testContract2.getId(), SignerType.MANAGER);

        // Assert
        assertFalse(hasValidSignature);
    }

    @Test
    void testSaveDigitalSignatureRecord() {
        // Arrange
        DigitalSignatureRecord newSignature = DigitalSignatureRecord.builder()
                .contract(testContract2)
                .certificate(testCertificate)
                .signerType(SignerType.MANAGER)
                .signerId(4L)
                .signerName("New Test Signer")
                .signerEmail("new.signer@test.com")
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_ECDSA)
                .status(SignatureStatus.VALID)
                .signatureImageData("new-signature-data".getBytes())
                .signatureHash("new-signature-hash")
                .hashAlgorithm("SHA-256")
                .pageNumber(1)
                .signatureX(200f)
                .signatureY(200f)
                .signatureWidth(140f)
                .signatureHeight(70f)
                .reason("New signature reason")
                .location("New Location")
                .ipAddress("192.168.1.50")
                .userAgent("New Browser")
                .signatureVerified(true)
                .certificateVerified(true)
                .timestampVerified(true)
                .timestampToken("new-timestamp-token".getBytes())
                .build();

        // Act
        DigitalSignatureRecord savedSignature = signatureRecordRepository.save(newSignature);

        // Assert
        assertNotNull(savedSignature);
        assertNotNull(savedSignature.getId());
        assertEquals("New Test Signer", savedSignature.getSignerName());
        assertEquals("new.signer@test.com", savedSignature.getSignerEmail());
        assertEquals(SignatureAlgorithm.SHA256_WITH_ECDSA, savedSignature.getSignatureAlgorithm());
        assertEquals(SignatureStatus.VALID, savedSignature.getStatus());
        assertEquals(testContract2.getId(), savedSignature.getContract().getId());
        assertEquals(testCertificate.getId(), savedSignature.getCertificate().getId());
        assertNotNull(savedSignature.getCreatedAt());
        assertNotNull(savedSignature.getSignedAt());
    }

    @Test
    void testFindById() {
        // Act
        Optional<DigitalSignatureRecord> found = signatureRecordRepository.findById(managerSignature.getId());

        // Assert
        assertTrue(found.isPresent());
        DigitalSignatureRecord signature = found.get();
        assertEquals(managerSignature.getId(), signature.getId());
        assertEquals("Repository Test Manager", signature.getSignerName());
        assertEquals("repo.manager@test.com", signature.getSignerEmail());
        assertEquals(SignerType.MANAGER, signature.getSignerType());
    }

    @Test
    void testUpdateSignatureRecord() {
        // Arrange
        managerSignature.setSignatureVerified(false);
        managerSignature.setStatus(SignatureStatus.INVALID);
        managerSignature.setLastVerifiedAt(LocalDateTime.now());

        // Act
        DigitalSignatureRecord updated = signatureRecordRepository.save(managerSignature);

        // Assert
        assertNotNull(updated);
        assertEquals(managerSignature.getId(), updated.getId());
        assertFalse(updated.getSignatureVerified());
        assertEquals(SignatureStatus.INVALID, updated.getStatus());
        assertNotNull(updated.getLastVerifiedAt());
    }

    @Test
    void testDeleteSignatureRecord() {
        // Arrange
        Long signatureId = customerSignature.getId();

        // Act
        signatureRecordRepository.delete(customerSignature);
        entityManager.flush();

        // Assert
        Optional<DigitalSignatureRecord> deleted = signatureRecordRepository.findById(signatureId);
        assertFalse(deleted.isPresent());

        // Verify only manager signature remains for contract1
        List<DigitalSignatureRecord> remaining = signatureRecordRepository
                .findByContractIdOrderBySignedAtAsc(testContract1.getId());
        assertEquals(1, remaining.size());
        assertEquals(managerSignature.getId(), remaining.get(0).getId());
    }

    @Test
    void testDigitalSignatureRecord_EntityMethods() {
        // Test entity helper methods
        assertTrue(managerSignature.isValid());
        assertTrue(managerSignature.isCompletelyVerified());
        assertFalse(managerSignature.hasTimestamp());
        assertFalse(managerSignature.isTimestampVerified());

        // Test with timestamp
        customerSignature.setTimestampToken("test-timestamp".getBytes());
        customerSignature.setTimestampVerified(true);
        assertTrue(customerSignature.hasTimestamp());
        assertTrue(customerSignature.isTimestampVerified());
        assertTrue(customerSignature.isCompletelyVerified());
    }

    @Test
    void testSignatureRecord_WithTimestamp() {
        // Arrange
        DigitalSignatureRecord timestampedSignature = DigitalSignatureRecord.builder()
                .contract(testContract2)
                .certificate(testCertificate)
                .signerType(SignerType.CUSTOMER)
                .signerId(5L)
                .signerName("Timestamped Signer")
                .signerEmail("timestamped@test.com")
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                .status(SignatureStatus.VALID)
                .signatureImageData("timestamped-signature".getBytes())
                .signatureHash("timestamped-hash")
                .hashAlgorithm("SHA-256")
                .timestampToken("rfc3161-timestamp-token".getBytes())
                .timestampUrl("http://timestamp.example.com")
                .timestampVerified(true)
                .signatureVerified(true)
                .certificateVerified(true)
                .pageNumber(1)
                .signatureX(300f)
                .signatureY(300f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .build();

        // Act
        DigitalSignatureRecord saved = signatureRecordRepository.save(timestampedSignature);

        // Assert
        assertNotNull(saved.getId());
        assertTrue(saved.hasTimestamp());
        assertTrue(saved.isTimestampVerified());
        assertTrue(saved.isCompletelyVerified());
        assertEquals("http://timestamp.example.com", saved.getTimestampUrl());
        assertArrayEquals("rfc3161-timestamp-token".getBytes(), saved.getTimestampToken());
    }

    @Test
    void testSignatureRecord_CascadeRelations() {
        // Test that deleting a contract doesn't cascade to signatures (should be handled by business logic)
        List<DigitalSignatureRecord> beforeDelete = signatureRecordRepository
                .findByContractIdOrderBySignedAtAsc(testContract1.getId());
        assertEquals(2, beforeDelete.size());

        // Contract deletion should be handled by business logic, not cascade
        // This test verifies the relationship integrity
        Optional<DigitalSignatureRecord> signature = signatureRecordRepository.findById(managerSignature.getId());
        assertTrue(signature.isPresent());
        assertEquals(testContract1.getId(), signature.get().getContract().getId());
    }

    @Test
    void testSignatureRecord_QueryPerformance() {
        // Create multiple signatures for performance testing
        Contract performanceContract = Contract.builder()
                .contractNumber("PERF-CONTRACT-001")
                .title("Performance Test Contract")
                .customerId(1L)
                .staffId(1L)
                .status(ContractStatus.ACTIVE)
                .filePath("perf-test-file")
                .isHidden(false)
                .createdBy("performance-test")
                .build();
        performanceContract = contractRepository.save(performanceContract);

        // Create 10 signatures
        for (int i = 0; i < 10; i++) {
            DigitalSignatureRecord signature = DigitalSignatureRecord.builder()
                    .contract(performanceContract)
                    .certificate(testCertificate)
                    .signerType(i % 2 == 0 ? SignerType.MANAGER : SignerType.CUSTOMER)
                    .signerId((long) i)
                    .signerName("Performance Signer " + i)
                    .signerEmail("perf" + i + "@test.com")
                    .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                    .status(SignatureStatus.VALID)
                    .signatureImageData(("perf-signature-" + i).getBytes())
                    .signatureHash("perf-hash-" + i)
                    .signatureVerified(true)
                    .certificateVerified(true)
                    .signedAt(LocalDateTime.now().minusMinutes(10 - i))
                    .build();
            signatureRecordRepository.save(signature);
        }
        entityManager.flush();

        // Test query performance
        long startTime = System.currentTimeMillis();
        List<DigitalSignatureRecord> signatures = signatureRecordRepository
                .findByContractIdOrderBySignedAtAsc(performanceContract.getId());
        long endTime = System.currentTimeMillis();

        // Assert
        assertEquals(10, signatures.size());
        assertTrue(endTime - startTime < 1000); // Should complete within 1 second

        // Verify ordering
        for (int i = 0; i < signatures.size() - 1; i++) {
            assertTrue(signatures.get(i).getSignedAt().isBefore(signatures.get(i + 1).getSignedAt()) ||
                      signatures.get(i).getSignedAt().equals(signatures.get(i + 1).getSignedAt()));
        }
    }
} 