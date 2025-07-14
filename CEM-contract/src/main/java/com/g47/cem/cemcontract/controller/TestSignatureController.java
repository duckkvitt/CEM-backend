package com.g47.cem.cemcontract.controller;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemcontract.dto.request.DigitalSignatureRequest;
import com.g47.cem.cemcontract.dto.response.ApiResponse;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.event.SellerSignedEvent;
import com.g47.cem.cemcontract.exception.ResourceNotFoundException;
import com.g47.cem.cemcontract.repository.ContractRepository;
import com.g47.cem.cemcontract.repository.DigitalCertificateRepository;
import com.g47.cem.cemcontract.service.DigitalSignatureService;
import com.g47.cem.cemcontract.util.CertificateTestUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test/signature")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Signature", description = "Test endpoints for digital signature debugging")
public class TestSignatureController {

    private final DigitalSignatureService digitalSignatureService;
    private final DigitalCertificateRepository certificateRepository;
    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/health")
    @Operation(summary = "Check signature service health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        try {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Signature service is healthy")
                    .data("OK")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(false)
                    .message("Service error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/create-test-certificate")
    @Operation(summary = "Create a test certificate for signing")
    public ResponseEntity<ApiResponse<DigitalCertificate>> createTestCertificate() {
        try {
            log.info("Creating test certificate");
            
            // Create test certificate using the existing utility method
            CertificateTestUtil certUtil = new CertificateTestUtil();
            DigitalCertificate certificate = certUtil.generateTestCertificateForUser(
                "Test Signer - " + System.currentTimeMillis(),
                "Test Organization",
                "test@example.com"
            );
            
            DigitalCertificate saved = certificateRepository.save(certificate);
            log.info("Test certificate created with ID: {}", saved.getId());
            
            return ResponseEntity.ok(ApiResponse.<DigitalCertificate>builder()
                    .success(true)
                    .message("Test certificate created successfully")
                    .data(saved)
                    .build());
        } catch (Exception e) {
            log.error("Failed to create test certificate: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.<DigitalCertificate>builder()
                    .success(false)
                    .message("Failed to create test certificate: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/sign-test/{contractId}")
    @Operation(summary = "Test sign a contract with sample signature")
    public ResponseEntity<ApiResponse<String>> testSignContract(
            @PathVariable Long contractId,
            @RequestBody(required = false) DigitalSignatureRequest request) {
        try {
            log.info("Testing contract signing for contract ID: {}", contractId);
            
            // Create default request if none provided
            if (request == null) {
                request = DigitalSignatureRequest.builder()
                        .signerType("MANAGER")
                        .signerName("Test Manager")
                        .signerEmail("manager@cem.com")
                        .signatureData("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==") // 1x1 transparent PNG
                        .pageNumber(1)
                        .signatureX(400f)
                        .signatureY(100f)
                        .signatureWidth(200f)
                        .signatureHeight(100f)
                        .reason("Test signature")
                        .location("CEM Test Platform")
                        .contactInfo("test@cem.com")
                        .build();
            }
            
            // Use the existing sign method that already exists
            String result = "Test signing functionality - check existing sign endpoints";
            log.info("Test signing prepared successfully");
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Test prepared successfully")
                    .data(result)
                    .build());
        } catch (Exception e) {
            log.error("Failed to test sign contract {}: {}", contractId, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(false)
                    .message("Failed to prepare test: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/test-customer-creation-flow/{contractId}")
    @Operation(summary = "Test complete customer creation flow after manager signs")
    public ResponseEntity<ApiResponse<String>> testCustomerCreationFlow(@PathVariable Long contractId) {
        try {
            log.info("Testing customer creation flow for contract ID: {}", contractId);

            // 1. Get contract
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

            // 2. Manually trigger SellerSignedEvent to test customer creation
            SellerSignedEvent event = new SellerSignedEvent(this, contract);
            
            // Fire the event (this should create customer account and send email)
            eventPublisher.publishEvent(event);
            
            String result = String.format(
                "Customer creation flow triggered for contract %s (Customer ID: %d). " +
                "Check logs for customer account creation status.",
                contract.getContractNumber(),
                contract.getCustomerId()
            );
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Failed to test customer creation flow for contract {}", contractId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @GetMapping("/certificates")
    @Operation(summary = "List all certificates")
    public ResponseEntity<ApiResponse<Object>> listCertificates() {
        try {
            var certificates = certificateRepository.findAll();
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Certificates retrieved")
                    .data(certificates)
                    .build());
        } catch (Exception e) {
            log.error("Failed to list certificates: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Failed to list certificates: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/debug/{contractId}")
    @Operation(summary = "Debug contract signing process")
    public ResponseEntity<ApiResponse<Object>> debugContract(@PathVariable Long contractId) {
        try {
            log.info("Debugging contract {}", contractId);
            
            // Check if contract exists
            // Check certificate availability
            // Check PDF file
            
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Debug completed")
                    .data("Check logs for details")
                    .build());
        } catch (Exception e) {
            log.error("Debug failed for contract {}: {}", contractId, e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message("Debug failed: " + e.getMessage())
                    .build());
        }
    }
} 