package com.g47.cem.cemcontract.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.g47.cem.cemcontract.dto.request.DigitalSignatureRequest;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.entity.DigitalSignatureRecord;
import com.g47.cem.cemcontract.enums.CertificateStatus;
import com.g47.cem.cemcontract.enums.CertificateType;
import com.g47.cem.cemcontract.enums.ContractStatus;
import com.g47.cem.cemcontract.enums.SignatureAlgorithm;
import com.g47.cem.cemcontract.enums.SignatureStatus;
import com.g47.cem.cemcontract.enums.SignerType;
import com.g47.cem.cemcontract.repository.ContractRepository;
import com.g47.cem.cemcontract.repository.DigitalCertificateRepository;
import com.g47.cem.cemcontract.repository.DigitalSignatureRecordRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContractControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private DigitalCertificateRepository certificateRepository;

    @Autowired
    private DigitalSignatureRecordRepository signatureRecordRepository;

    private Contract testContract;
    private DigitalCertificate testCertificate;
    private DigitalSignatureRecord testSignature;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        signatureRecordRepository.deleteAll();
        contractRepository.deleteAll();
        certificateRepository.deleteAll();

        // Create test certificate
        testCertificate = DigitalCertificate.builder()
                .alias("integration_test_alias")
                .subjectDN("CN=Integration Test Certificate,O=Test Org")
                .issuerDN("CN=Test CA,O=Test CA Org")
                .serialNumber("INT123456789")
                .certificateType(CertificateType.SELF_SIGNED)
                .status(CertificateStatus.ACTIVE)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusYears(1))
                .certificateData("test-certificate-data".getBytes())
                .publicKeyData("test-public-key-data".getBytes())
                .keyAlgorithm("RSA")
                .keySize(2048)
                .signatureAlgorithm("SHA256withRSA")
                .fingerprintSha1("test-sha1-fingerprint")
                .fingerprintSha256("test-sha256-fingerprint")
                .createdBy("integration-test")
                .build();
        testCertificate = certificateRepository.save(testCertificate);

        // Create test contract
        testContract = Contract.builder()
                .contractNumber("INT-CONTRACT-001")
                .title("Integration Test Contract")
                .description("Test contract for integration testing")
                .customerId(1L)
                .staffId(1L)
                .totalValue(BigDecimal.valueOf(10000.0))
                .status(ContractStatus.PENDING_SELLER_SIGNATURE)
                .filePath("test-integration-file-id")
                .isHidden(false)
                .createdBy("integration-test")
                .build();
        testContract = contractRepository.save(testContract);

        // Create test signature record
        testSignature = DigitalSignatureRecord.builder()
                .contract(testContract)
                .certificate(testCertificate)
                .signerType(SignerType.MANAGER)
                .signerId(1L)
                .signerName("Integration Test Signer")
                .signerEmail("integration@test.com")
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                .status(SignatureStatus.VALID)
                .signatureImageData("integration-test-signature".getBytes())
                .signatureHash("integration-test-hash")
                .hashAlgorithm("SHA-256")
                .pageNumber(1)
                .signatureX(100f)
                .signatureY(100f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .reason("Integration test signature")
                .location("Test Environment")
                .ipAddress("127.0.0.1")
                .userAgent("Integration Test Agent")
                .signatureVerified(true)
                .certificateVerified(true)
                .timestampVerified(false)
                .build();
        testSignature = signatureRecordRepository.save(testSignature);
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testSignContractDigitally_Success() throws Exception {
        // Arrange
        DigitalSignatureRequest request = DigitalSignatureRequest.builder()
                .signatureData(Base64.getEncoder().encodeToString("test-canvas-signature".getBytes()))
                .signerName("Test Manager")
                .signerEmail("manager@test.com")
                .reason("Contract approval")
                .location("Digital Platform")
                .signatureX(150f)
                .signatureY(150f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .pageNumber(1)
                .build();

        // Act & Assert
        mockMvc.perform(post("/contracts/{id}/digital-signature", testContract.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.signerName").value("Test Manager"))
                .andExpect(jsonPath("$.data.signerEmail").value("manager@test.com"))
                .andExpect(jsonPath("$.data.signatureReason").value("Contract approval"))
                .andExpect(jsonPath("$.data.contractId").value(testContract.getId()));
    }

    @Test
    @WithMockUser(authorities = {"CUSTOMER"})
    void testSignContractDigitally_AsCustomer() throws Exception {
        // Arrange
        // Update contract status to allow customer signing
        testContract.setStatus(ContractStatus.PENDING_CUSTOMER_SIGNATURE);
        contractRepository.save(testContract);

        DigitalSignatureRequest request = DigitalSignatureRequest.builder()
                .signatureData(Base64.getEncoder().encodeToString("customer-canvas-signature".getBytes()))
                .signerName("Test Customer")
                .signerEmail("customer@test.com")
                .reason("Contract acceptance")
                .location("Customer Portal")
                .signatureX(400f)
                .signatureY(150f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .pageNumber(1)
                .build();

        // Act & Assert
        mockMvc.perform(post("/contracts/{id}/digital-signature", testContract.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.signerName").value("Test Customer"))
                .andExpect(jsonPath("$.data.signerEmail").value("customer@test.com"));
    }

    @Test
    @WithMockUser(authorities = {"STAFF"})
    void testSignContractDigitally_Unauthorized() throws Exception {
        // Arrange
        DigitalSignatureRequest request = DigitalSignatureRequest.builder()
                .signatureData(Base64.getEncoder().encodeToString("staff-signature".getBytes()))
                .signerName("Test Staff")
                .signerEmail("staff@test.com")
                .build();

        // Act & Assert
        mockMvc.perform(post("/contracts/{id}/digital-signature", testContract.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testSignContractDigitally_InvalidRequest() throws Exception {
        // Arrange - Request missing required fields
        DigitalSignatureRequest request = DigitalSignatureRequest.builder()
                .signatureData(null) // Missing signature
                .signerName(null) // Missing name
                .build();

        // Act & Assert
        mockMvc.perform(post("/contracts/{id}/digital-signature", testContract.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testSignContractDigitally_ContractNotFound() throws Exception {
        // Arrange
        Long nonExistentContractId = 99999L;
        DigitalSignatureRequest request = DigitalSignatureRequest.builder()
                .signatureData(Base64.getEncoder().encodeToString("test-signature".getBytes()))
                .signerName("Test Manager")
                .signerEmail("manager@test.com")
                .build();

        // Act & Assert
        mockMvc.perform(post("/contracts/{id}/digital-signature", nonExistentContractId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testGetContractSignatures_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/contracts/{id}/signatures", testContract.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testSignature.getId()))
                .andExpect(jsonPath("$.data[0].signerName").value("Integration Test Signer"))
                .andExpect(jsonPath("$.data[0].signerEmail").value("integration@test.com"))
                .andExpect(jsonPath("$.data[0].contractId").value(testContract.getId()));
    }

    @Test
    @WithMockUser(authorities = {"CUSTOMER"})
    void testGetContractSignatures_AsCustomer() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/contracts/{id}/signatures", testContract.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testGetContractSignatures_EmptyList() throws Exception {
        // Arrange - Create contract without signatures
        Contract emptyContract = Contract.builder()
                .contractNumber("EMPTY-CONTRACT-001")
                .title("Empty Contract")
                .customerId(1L)
                .staffId(1L)
                .status(ContractStatus.DRAFT)
                .isHidden(false)
                .createdBy("test")
                .build();
        emptyContract = contractRepository.save(emptyContract);

        // Act & Assert
        mockMvc.perform(get("/contracts/{id}/signatures", emptyContract.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testVerifySignature_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/contracts/signatures/{signatureId}/verify", testSignature.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.signatureValid").value(true))
                .andExpect(jsonPath("$.data.certificateValid").value(true))
                .andExpect(jsonPath("$.data.documentIntegrityValid").value(true))
                .andExpect(jsonPath("$.data.signerName").value("Integration Test Signer"))
                .andExpect(jsonPath("$.data.verificationTime").exists());
    }

    @Test
    @WithMockUser(authorities = {"STAFF"})
    void testVerifySignature_AsStaff() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/contracts/signatures/{signatureId}/verify", testSignature.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signatureValid").value(true));
    }

    @Test
    @WithMockUser(authorities = {"CUSTOMER"})
    void testVerifySignature_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/contracts/signatures/{signatureId}/verify", testSignature.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testVerifySignature_NotFound() throws Exception {
        // Arrange
        Long nonExistentSignatureId = 99999L;

        // Act & Assert
        mockMvc.perform(post("/contracts/signatures/{signatureId}/verify", nonExistentSignatureId))
                .andExpect(status().isOk()) // Service returns 200 with error in response
                .andExpect(jsonPath("$.data.signatureValid").value(false))
                .andExpect(jsonPath("$.data.errors").isArray())
                .andExpect(jsonPath("$.data.errors[0]").value(containsString("not found")));
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testDigitalSignatureWorkflow_FullFlow() throws Exception {
        // Step 1: Create a new contract for testing full workflow
        Contract workflowContract = Contract.builder()
                .contractNumber("WORKFLOW-CONTRACT-001")
                .title("Workflow Test Contract")
                .customerId(1L)
                .staffId(1L)
                .status(ContractStatus.PENDING_SELLER_SIGNATURE)
                .filePath("workflow-test-file-id")
                .isHidden(false)
                .createdBy("workflow-test")
                .build();
        workflowContract = contractRepository.save(workflowContract);

        // Step 2: Manager signs the contract
        DigitalSignatureRequest managerRequest = DigitalSignatureRequest.builder()
                .signatureData(Base64.getEncoder().encodeToString("manager-workflow-signature".getBytes()))
                .signerName("Workflow Manager")
                .signerEmail("workflow.manager@test.com")
                .reason("Manager approval")
                .location("Manager Portal")
                .signatureX(100f)
                .signatureY(100f)
                .signatureWidth(130f)
                .signatureHeight(65f)
                .pageNumber(1)
                .build();

        String managerSignResponse = mockMvc.perform(post("/contracts/{id}/digital-signature", workflowContract.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(managerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signerName").value("Workflow Manager"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Step 3: Get contract signatures
        mockMvc.perform(get("/contracts/{id}/signatures", workflowContract.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].signerName").value("Workflow Manager"));

        // Step 4: Verify the manager's signature
        // Extract signature ID from response (simplified for test)
        // In real implementation, you'd parse the JSON response
        Long signatureId = signatureRecordRepository.findByContractIdOrderBySignedAtAsc(workflowContract.getId())
                .get(0).getId();

        mockMvc.perform(post("/contracts/signatures/{signatureId}/verify", signatureId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signatureValid").value(true))
                .andExpect(jsonPath("$.data.signerName").value("Workflow Manager"));
    }

    @Test
    @WithMockUser(authorities = {"MANAGER"})
    void testLegacySignatureEndpoint_BackwardCompatibility() throws Exception {
        // Arrange
        String legacySignatureRequest = """
            {
                "signature": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/contracts/{id}/signatures", testContract.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(legacySignatureRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
} 
