package com.g47.cem.cemcontract.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemcontract.dto.request.DigitalSignatureRequest;
import com.g47.cem.cemcontract.dto.response.DigitalSignatureResponseDto;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.DigitalCertificate;
import com.g47.cem.cemcontract.entity.DigitalSignatureRecord;
import com.g47.cem.cemcontract.enums.ContractStatus;
import com.g47.cem.cemcontract.enums.SignatureAlgorithm;
import com.g47.cem.cemcontract.enums.SignatureStatus;
import com.g47.cem.cemcontract.enums.SignerType;
import com.g47.cem.cemcontract.event.SellerSignedEvent;
import com.g47.cem.cemcontract.exception.BusinessException;
import com.g47.cem.cemcontract.exception.ResourceNotFoundException;
import com.g47.cem.cemcontract.repository.ContractRepository;
import com.g47.cem.cemcontract.repository.DigitalCertificateRepository;
import com.g47.cem.cemcontract.repository.DigitalSignatureRecordRepository;
import com.g47.cem.cemcontract.util.CertificateTestUtil;
import com.itextpdf.forms.fields.properties.SignedAppearanceText;
import com.itextpdf.forms.form.element.SignatureFieldAppearance;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalSignatureService {

    private final ContractRepository contractRepository;
    private final DigitalCertificateRepository certificateRepository;
    private final DigitalSignatureRecordRepository signatureRecordRepository;
    private final CertificateTestUtil certificateTestUtil;
    private final GoogleDriveService googleDriveService;
    private final ApplicationEventPublisher eventPublisher;
    
    @Value("${app.file.upload-dir:./uploads/contracts}")
    private String uploadDir;
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Main method to sign contract with visible signature
     */
    @Transactional
    public DigitalSignatureResponseDto signContract(Long contractId, DigitalSignatureRequest request) {
        try {
            log.info("Starting PDF signing process for contract ID: {}", contractId);
            
            // Validate request
            validateSignatureRequest(contractId, request);
            
            // Get contract
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + contractId));

            // Get or create certificate
            DigitalCertificate certificate = getOrCreateCertificate(request);

            // Load and sign PDF
            byte[] originalPdf = loadContractPdf(contract);
            byte[] signedPdf = signPdfWithVisibleSignature(originalPdf, request, certificate);
            
            // Save signed PDF
            String signedPdfPath = saveSignedPdf(contract, signedPdf);
            
            // Create signature record
            DigitalSignatureRecord signatureRecord = createSignatureRecord(
                contract, certificate, request, signedPdfPath);
            
            // Update contract with new Google Drive file ID
            updateContractAfterSigning(contract, signatureRecord, signedPdfPath);
            
            log.info("Successfully signed contract {} with digital signature. Record ID: {}", 
                    contractId, signatureRecord.getId());
            
            // Convert to DTO to avoid lazy loading issues
            return mapToResponseDto(signatureRecord);
            
        } catch (Exception e) {
            log.error("Failed to sign contract: {}", e.getMessage(), e);
            throw new BusinessException("Failed to sign contract: " + e.getMessage());
        }
    }

    /**
     * Validate signature request
     */
    private void validateSignatureRequest(Long contractId, DigitalSignatureRequest request) {
        if (request == null) {
            throw new BusinessException("Signature request cannot be null");
        }
        if (request.getSignerName() == null || request.getSignerName().trim().isEmpty()) {
            throw new BusinessException("Signer name is required");
        }
        if (request.getSignerEmail() == null || request.getSignerEmail().trim().isEmpty()) {
            throw new BusinessException("Signer email is required");
        }
        log.debug("Signature request validation passed for contract ID: {}", contractId);
    }

    /**
     * Get or create certificate for signing
     */
    private DigitalCertificate getOrCreateCertificate(DigitalSignatureRequest request) {
        try {
            // Try to find existing certificate
            if (request.getCertificateId() != null) {
                return certificateRepository.findById(request.getCertificateId())
                        .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
            }
            
            // Create temporary certificate for testing
            log.info("Creating temporary certificate for {}", request.getSignerName());
            DigitalCertificate certificate = certificateTestUtil.generateTestCertificateForUser(
                request.getSignerName(),
                "CEM Contract System",
                request.getSignerEmail()
            );
            
            return certificateRepository.save(certificate);
            
        } catch (Exception e) {
            log.error("Failed to get or create certificate: {}", e.getMessage());
            throw new BusinessException("Failed to get certificate: " + e.getMessage());
        }
    }
    
    /**
     * Load contract PDF from local file or download from Google Drive
     */
    private byte[] loadContractPdf(Contract contract) {
        try {
            if (contract.getFilePath() == null || contract.getFilePath().isEmpty()) {
                throw new BusinessException("Contract does not have a PDF file");
            }
            
            String filePath = contract.getFilePath();
            
            // Check if filePath is a Google Drive ID (typically 25-44 chars, alphanumeric + hyphens/underscores, no extension)
            if (isGoogleDriveId(filePath)) {
                log.info("Detected Google Drive ID: {}, downloading file content...", filePath);
                return googleDriveService.downloadFileContent(filePath);
            }
            
            // Try to load from local filesystem
            log.info("Loading PDF from local path: {}", filePath);
            Path pdfPath = Paths.get(uploadDir, filePath);
            if (!Files.exists(pdfPath)) {
                throw new BusinessException("Contract PDF file not found: " + pdfPath);
            }
            
            return Files.readAllBytes(pdfPath);
        } catch (IOException e) {
            throw new BusinessException("Failed to load contract PDF: " + e.getMessage());
        }
    }
    
    /**
     * Check if the given string is likely a Google Drive file ID
     */
    private boolean isGoogleDriveId(String filePath) {
        // Google Drive IDs are typically 25-44 characters long
        // Contains only alphanumeric characters, hyphens, and underscores
        // Does not contain file extensions or path separators
        return filePath != null
                && filePath.length() >= 25 
                && filePath.length() <= 44
                && filePath.matches("[a-zA-Z0-9_-]+")
                && !filePath.contains(".")
                && !filePath.contains("/")
                && !filePath.contains("\\");
    }

    /**
     * Sign PDF with visible signature using iText 8 PdfSigner and SignatureFieldAppearance
     */
    private byte[] signPdfWithVisibleSignature(byte[] pdfBytes,
                                               DigitalSignatureRequest request,
                                               DigitalCertificate certificate) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            int pageNum = 4;

            float x, y, width = 200f, height = 80f;
            SignerType signerType = SignerType.valueOf(request.getSignerType());
            if (signerType == SignerType.MANAGER) {
                x = 100f;
                y = 150f;
            } else if (signerType == SignerType.CUSTOMER) {
                x = 400f;
                y = 150f;
            } else {
                x = 50f;
                y = 100f;
            }

            // Tên field phải duy nhất
            String fieldName = "Signature_" + System.currentTimeMillis() + "_" + request.getSignerType();

            PdfSigner signer = new PdfSigner(
                    new PdfReader(new ByteArrayInputStream(pdfBytes)),
                    outputStream,
                    new StampingProperties().useAppendMode()
            );

            signer.setFieldName(fieldName);
            signer.setPageRect(new Rectangle(x, y, width, height));
            signer.setPageNumber(pageNum);
            signer.setReason(request.getReason() != null ? request.getReason() : "Digital contract signature");
            signer.setLocation(request.getLocation() != null ? request.getLocation() : "CEM Digital Platform");
            signer.setContact(request.getContactInfo() != null ? request.getContactInfo() : request.getSignerEmail());

            SignatureFieldAppearance appearance = createSignatureAppearance(fieldName, request);
            signer.setSignatureAppearance(appearance);

            KeyStore keyStore = createKeyStoreFromCertificate(certificate);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("cert", "password".toCharArray());
            Certificate[] chain = keyStore.getCertificateChain("cert");

            IExternalDigest externalDigest = new BouncyCastleDigest();
            IExternalSignature externalSignature = new PrivateKeySignature(
                    privateKey, DigestAlgorithms.SHA256, BouncyCastleProvider.PROVIDER_NAME);

            signer.signDetached(externalDigest, externalSignature,
                    chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Failed to sign PDF with visible signature: {}", e.getMessage(), e);
            throw new BusinessException("Failed to sign PDF: " + e.getMessage());
        }
    }
    
    /**
     * Decode signature image from base64
     */
    private byte[] decodeSignatureImage(String signatureData) {
        try {
            if (signatureData.startsWith("data:image/")) {
                String base64Data = signatureData.substring(signatureData.indexOf(",") + 1);
                return Base64.getDecoder().decode(base64Data);
            }
            return Base64.getDecoder().decode(signatureData);
        } catch (Exception e) {
            log.warn("Failed to decode signature image: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create keystore from certificate
     */
    private KeyStore createKeyStoreFromCertificate(DigitalCertificate digitalCert) throws Exception {
        try {
            // Try to load from stored PKCS12 keystore data first
            if (digitalCert.getPrivateKeyData() != null) {
                try {
                    KeyStore keyStore = KeyStore.getInstance("PKCS12");
                    keyStore.load(new ByteArrayInputStream(digitalCert.getPrivateKeyData()), "password".toCharArray());
                    log.debug("Loaded keystore from stored PKCS12 data");
                    return keyStore;
                } catch (Exception e) {
                    log.warn("Failed to load PKCS12 keystore from stored data, trying alternative method: {}", e.getMessage());
                }
            }
            
            // Fallback: create keystore from certificate and private key data
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            
            // Load certificate from stored data
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(
                new ByteArrayInputStream(digitalCert.getCertificateData())
            );
            
            PrivateKey privateKey = null;
            
            // Try to load private key from stored data as PKCS8
            if (digitalCert.getPrivateKeyData() != null) {
                try {
                    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(digitalCert.getPrivateKeyData());
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    privateKey = keyFactory.generatePrivate(keySpec);
                } catch (Exception e) {
                    log.warn("Failed to load private key from stored data: {}", e.getMessage());
                }
            }
            
            // If no private key available, generate a temporary one for testing
            if (privateKey == null) {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair keyPair = keyGen.generateKeyPair();
                privateKey = keyPair.getPrivate();
                log.info("Generated temporary private key for certificate");
            }
            
            // Add certificate and private key to keystore
            Certificate[] chain = {certificate};
            keyStore.setKeyEntry("cert", privateKey, "password".toCharArray(), chain);
            
            log.debug("Created keystore with certificate subject: {}", certificate.getSubjectX500Principal().getName());
            return keyStore;
            
        } catch (Exception e) {
            log.error("Failed to create keystore from certificate: {}", e.getMessage(), e);
            throw new Exception("Failed to create keystore: " + e.getMessage(), e);
        }
    }

    /**
     * Create signature appearance for iText 8
     */
    private SignatureFieldAppearance createSignatureAppearance(String fieldName, DigitalSignatureRequest request) {
        try {
            SignatureFieldAppearance appearance = new SignatureFieldAppearance(fieldName);

            // Create custom appearance content
            String signerName = request.getSignerName() != null ? request.getSignerName() : "Digital Signer";
            String reason = request.getReason() != null ? request.getReason() : "Digital contract signature";
            String location = request.getLocation() != null ? request.getLocation() : "CEM Digital Platform";

            // Try to create signature appearance with image if available
            if (request.getSignatureData() != null && !request.getSignatureData().trim().isEmpty()) {
                // Create appearance with signature image + text
                appearance = createSignatureAppearanceWithImage(appearance, signerName, reason, location, request.getSignatureData());
            } else {
                // Create text-only appearance
                appearance = createTextOnlySignatureAppearance(appearance, signerName, reason, location);
            }

            log.debug("Created signature appearance for field: {}", fieldName);
            return appearance;
            
        } catch (Exception e) {
            log.error("Failed to create signature appearance: {}", e.getMessage(), e);
            throw new BusinessException("Failed to create signature appearance: " + e.getMessage());
        }
    }

    /**
     * Create signature appearance with signature image
     */
    private SignatureFieldAppearance createSignatureAppearanceWithImage(SignatureFieldAppearance appearance, 
                                                                        String signerName, String reason, String location, String signatureData) {
        try {
            // Decode signature image
            byte[] imageBytes = decodeSignatureImage(signatureData);
            if (imageBytes != null) {
                ImageData imageData = ImageDataFactory.create(imageBytes);
                
                // Create custom appearance text
                SignedAppearanceText appearanceText = new SignedAppearanceText()
                    .setSignedBy("Signed by: " + signerName)
                    .setReasonLine("Reason: " + reason)
                    .setLocationLine("Location: " + location);

                // Set content with both image and text
                appearance.setContent(appearanceText, imageData);
                
                log.debug("Created signature appearance with image");
            } else {
                // Fallback to text-only if image decode fails
                log.warn("Failed to decode signature image, falling back to text-only appearance");
                return createTextOnlySignatureAppearance(appearance, signerName, reason, location);
            }
            
            // Style the appearance
            appearance
                .setFontColor(new DeviceRgb(0, 0, 0))  // Black text
                .setBackgroundColor(new DeviceRgb(245, 245, 245));  // Light gray background
            
            return appearance;
            
        } catch (Exception e) {
            log.error("Failed to create image signature appearance: {}", e.getMessage(), e);
            // Fallback to text-only
            return createTextOnlySignatureAppearance(appearance, signerName, reason, location);
        }
    }

    /**
     * Create text-only signature appearance
     */
    private SignatureFieldAppearance createTextOnlySignatureAppearance(SignatureFieldAppearance appearance, 
                                                                       String signerName, String reason, String location) {
        try {
            // Create custom appearance text
            SignedAppearanceText appearanceText = new SignedAppearanceText()
                .setSignedBy("✓ Digitally signed by: " + signerName)
                .setReasonLine("Reason: " + reason)
                .setLocationLine("Location: " + location);

            // Set text content
            appearance.setContent(appearanceText);
            
            // Style the appearance  
            appearance
                .setFontColor(new DeviceRgb(0, 51, 102))  // Dark blue text
                .setBackgroundColor(new DeviceRgb(240, 248, 255))  // Light blue background
                .setBorder(new SolidBorder(new DeviceRgb(0, 51, 102), 1)); // Dark blue border
            
            log.debug("Created text-only signature appearance");
            
            return appearance;
            
        } catch (Exception e) {
            log.error("Failed to create text signature appearance: {}", e.getMessage(), e);
            throw new BusinessException("Failed to create signature appearance: " + e.getMessage());
        }
    }

    /**
     * Save signed PDF to Google Drive (replace old file)
     */
    private String saveSignedPdf(Contract contract, byte[] signedPdf) throws IOException {
        String fileName = "signed_" + contract.getContractNumber() + "_" + System.currentTimeMillis() + ".pdf";
        
        try {
            // Delete old file from Google Drive if exists
            String currentFilePath = contract.getFilePath();
            if (currentFilePath != null && isGoogleDriveId(currentFilePath)) {
                log.info("Deleting old file from Google Drive: {}", currentFilePath);
                googleDriveService.deleteFile(currentFilePath);
            }
            
            // Upload signed PDF to Google Drive
            log.info("Uploading signed PDF to Google Drive: {}", fileName);
            String googleDriveFileId = googleDriveService.uploadFileBytes(signedPdf, fileName);
            
            log.info("Signed PDF uploaded to Google Drive successfully. FileId: {}", googleDriveFileId);
            return googleDriveFileId;
            
        } catch (Exception e) {
            log.error("Failed to upload signed PDF to Google Drive: {}", e.getMessage(), e);
            throw new IOException("Failed to upload to Google Drive: " + e.getMessage(), e);
        }
    }

    /**
     * Create signature record
     */
    private DigitalSignatureRecord createSignatureRecord(Contract contract, DigitalCertificate certificate, 
                                                        DigitalSignatureRequest request, String signedPdfPath) {
        DigitalSignatureRecord record = DigitalSignatureRecord.builder()
                .contract(contract)  // Use contract object, not contractId
                .certificate(certificate)  // Use certificate object
                .signerType(SignerType.valueOf(request.getSignerType()))
                .signerName(request.getSignerName())
                .signerEmail(request.getSignerEmail())
                .signatureFieldName("signature_" + System.currentTimeMillis())
                .signatureAlgorithm(SignatureAlgorithm.SHA256_WITH_RSA)
                .status(SignatureStatus.VALID)
                .pageNumber(request.getPageNumber())
                .signatureX(request.getSignatureX())
                .signatureY(request.getSignatureY())
                .signatureWidth(request.getSignatureWidth())
                .signatureHeight(request.getSignatureHeight())
                .reason(request.getReason())
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .signedAt(LocalDateTime.now())
                .build();
        
        return signatureRecordRepository.save(record);
    }

    /**
     * Update contract after signing with proper workflow
     */
    private void updateContractAfterSigning(Contract contract, DigitalSignatureRecord signatureRecord, String signedPdfPath) {
        contract.setDigitalSigned(true);
        contract.setSignedAt(LocalDateTime.now());
        contract.setSignedBy(signatureRecord.getSignerName());
        contract.setUpdatedAt(LocalDateTime.now());
        
        // Update file path with new Google Drive file ID
        contract.setFilePath(signedPdfPath);
        
        // Update status based on signer type and current status
        SignerType signerType = signatureRecord.getSignerType();
        ContractStatus currentStatus = contract.getStatus();
        
        if (signerType == SignerType.MANAGER || signerType == SignerType.STAFF) {
            // Manager/Staff signs -> Wait for customer signature
            if (currentStatus == ContractStatus.PENDING_SELLER_SIGNATURE || currentStatus == ContractStatus.DRAFT) {
                contract.setStatus(ContractStatus.PENDING_CUSTOMER_SIGNATURE);
                log.info("Manager/Staff signed contract {}. Status changed to PENDING_CUSTOMER_SIGNATURE", contract.getId());
                
                // Fire event to create customer account and send email
                eventPublisher.publishEvent(new SellerSignedEvent(this, contract));
                log.info("SellerSignedEvent published for contract {}", contract.getId());
            }
        } else if (signerType == SignerType.CUSTOMER) {
            // Customer signs -> Contract becomes active
            if (currentStatus == ContractStatus.PENDING_CUSTOMER_SIGNATURE) {
                contract.setStatus(ContractStatus.ACTIVE);
                log.info("Customer signed contract {}. Status changed to ACTIVE (fully signed)", contract.getId());
            } else {
                log.warn("Customer signed contract {} but status was {} instead of PENDING_CUSTOMER_SIGNATURE", 
                        contract.getId(), currentStatus);
                // Still set to ACTIVE if customer signs
                contract.setStatus(ContractStatus.ACTIVE);
            }
        } else {
            // Default behavior for other signer types
            log.warn("Unknown signer type {} for contract {}. Using default status ACTIVE", signerType, contract.getId());
            contract.setStatus(ContractStatus.ACTIVE);
        }
        
        contractRepository.save(contract);
        log.info("Contract {} updated after signing. New status: {}, File path: {}", 
                contract.getId(), contract.getStatus(), signedPdfPath);
    }

    /**
     * Map DigitalSignatureRecord entity to DigitalSignatureResponseDto
     */
    private DigitalSignatureResponseDto mapToResponseDto(DigitalSignatureRecord record) {
        return DigitalSignatureResponseDto.builder()
                .id(record.getId())
                .contractId(record.getContract().getId())
                .contractNumber(record.getContract().getContractNumber())
                .signerName(record.getSignerName())
                .signerEmail(record.getSignerEmail())
                .signerType(record.getSignerType())
                .status(record.getStatus())
                .signatureAlgorithm(record.getSignatureAlgorithm().name())
                .pageNumber(record.getPageNumber())
                .signatureX(record.getSignatureX() != null ? record.getSignatureX().doubleValue() : null)
                .signatureY(record.getSignatureY() != null ? record.getSignatureY().doubleValue() : null)
                .signatureWidth(record.getSignatureWidth() != null ? record.getSignatureWidth().doubleValue() : null)
                .signatureHeight(record.getSignatureHeight() != null ? record.getSignatureHeight().doubleValue() : null)
                .reason(record.getReason())
                .location(record.getLocation())
                .ipAddress(record.getIpAddress())
                .signedAt(record.getSignedAt())
                // Certificate info
                .certificateSubjectDN(record.getCertificate().getSubjectDN())
                .certificateIssuerDN(record.getCertificate().getIssuerDN())
                .certificateSerialNumber(record.getCertificate().getSerialNumber())
                .build();
    }
} 