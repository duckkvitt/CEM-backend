package com.g47.cem.cemcontract.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemcontract.dto.response.ApiResponse;
import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.repository.DigitalCertificateRepository;
import com.g47.cem.cemcontract.util.CertificateTestUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Test controller for certificate management and testing digital signatures
 * WARNING: This is for testing purposes only - do not use in production
 */
@RestController
@RequestMapping("/test/certificates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Certificate Management", description = "APIs for testing digital signature functionality")
public class TestCertificateController {

    private final CertificateTestUtil certificateTestUtil;
    private final DigitalCertificateRepository certificateRepository;

    @PostMapping("/generate-manager")
    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Generate test manager certificate", 
               description = "Creates a test self-signed certificate for manager role testing")
    public ResponseEntity<ApiResponse<DigitalCertificate>> generateManagerCertificate() {
        try {
            log.info("Generating test manager certificate");
            
            DigitalCertificate certificate = certificateTestUtil.generateManagerCertificate();
            DigitalCertificate savedCertificate = certificateRepository.save(certificate);
            
            log.info("Generated test manager certificate with ID: {}", savedCertificate.getId());
            
            return ResponseEntity.ok(ApiResponse.success(savedCertificate));
            
        } catch (Exception e) {
            log.error("Failed to generate manager certificate", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/generate-staff")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    @Operation(summary = "Generate test staff certificate", 
               description = "Creates a test self-signed certificate for staff role testing")
    public ResponseEntity<ApiResponse<DigitalCertificate>> generateStaffCertificate() {
        try {
            log.info("Generating test staff certificate");
            
            DigitalCertificate certificate = certificateTestUtil.generateStaffCertificate();
            DigitalCertificate savedCertificate = certificateRepository.save(certificate);
            
            log.info("Generated test staff certificate with ID: {}", savedCertificate.getId());
            
            return ResponseEntity.ok(ApiResponse.success(savedCertificate));
            
        } catch (Exception e) {
            log.error("Failed to generate staff certificate", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/generate-customer")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    @Operation(summary = "Generate test customer certificate", 
               description = "Creates a test self-signed certificate for customer role testing")
    public ResponseEntity<ApiResponse<DigitalCertificate>> generateCustomerCertificate() {
        try {
            log.info("Generating test customer certificate");
            
            DigitalCertificate certificate = certificateTestUtil.generateCustomerCertificate();
            DigitalCertificate savedCertificate = certificateRepository.save(certificate);
            
            log.info("Generated test customer certificate with ID: {}", savedCertificate.getId());
            
            return ResponseEntity.ok(ApiResponse.success(savedCertificate));
            
        } catch (Exception e) {
            log.error("Failed to generate customer certificate", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/setup-all")
    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Setup all test certificates", 
               description = "Generates all test certificates for comprehensive testing")
    public ResponseEntity<ApiResponse<String>> setupAllTestCertificates() {
        try {
            log.info("Setting up all test certificates");
            
            // Check if certificates already exist
            long existingCount = certificateRepository.count();
            if (existingCount > 0) {
                return ResponseEntity.ok(ApiResponse.success("Test certificates already exist. Count: " + existingCount));
            }
            
            // Generate all test certificates
            DigitalCertificate managerCert = certificateTestUtil.generateManagerCertificate();
            DigitalCertificate staffCert = certificateTestUtil.generateStaffCertificate();
            DigitalCertificate customerCert = certificateTestUtil.generateCustomerCertificate();
            
            // Save certificates
            certificateRepository.save(managerCert);
            certificateRepository.save(staffCert);
            certificateRepository.save(customerCert);
            
            log.info("Successfully setup all test certificates");
            
            return ResponseEntity.ok(ApiResponse.success("Successfully generated 3 test certificates"));
            
        } catch (Exception e) {
            log.error("Failed to setup test certificates", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    @Operation(summary = "List all certificates", 
               description = "Returns list of all available certificates for testing")
    public ResponseEntity<ApiResponse<Object>> listCertificates() {
        try {
            var certificates = certificateRepository.findAll();
            
            var result = certificates.stream()
                .map(cert -> {
                    var info = new java.util.HashMap<String, Object>();
                    info.put("id", cert.getId());
                    info.put("subject", cert.getSubjectDN());
                    info.put("issuer", cert.getIssuerDN());
                    info.put("serialNumber", cert.getSerialNumber());
                    info.put("validFrom", cert.getValidFrom().toString());
                    info.put("validTo", cert.getValidTo());
                    info.put("isValid", cert.isCurrentlyValid());
                    info.put("commonName", cert.getCommonName());
                    return info;
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Failed to list certificates", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasAuthority('MANAGER')")
    @Operation(summary = "Cleanup test certificates", 
               description = "Removes all test certificates from the database")
    public ResponseEntity<ApiResponse<String>> cleanupTestCertificates() {
        try {
            log.info("Cleaning up test certificates");
            
            long deletedCount = certificateRepository.count();
            certificateRepository.deleteAll();
            
            log.info("Deleted {} test certificates", deletedCount);
            
            return ResponseEntity.ok(ApiResponse.success("Deleted " + deletedCount + " certificates"));
            
        } catch (Exception e) {
            log.error("Failed to cleanup certificates", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }
} 