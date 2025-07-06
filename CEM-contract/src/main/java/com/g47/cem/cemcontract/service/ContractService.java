package com.g47.cem.cemcontract.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.g47.cem.cemcontract.dto.request.ContractCreationRequestDto;
import com.g47.cem.cemcontract.dto.request.CreateContractRequest;
import com.g47.cem.cemcontract.dto.request.SignatureRequestDto;
import com.g47.cem.cemcontract.dto.request.UpdateContractRequest;
import com.g47.cem.cemcontract.dto.response.ContractResponseDto;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.ContractDeliverySchedule;
import com.g47.cem.cemcontract.entity.ContractDetail;
import com.g47.cem.cemcontract.entity.ContractHistory;
import com.g47.cem.cemcontract.enums.ContractAction;
import com.g47.cem.cemcontract.enums.ContractStatus;
import com.g47.cem.cemcontract.event.SellerSignedEvent;
import com.g47.cem.cemcontract.exception.BusinessException;
import com.g47.cem.cemcontract.exception.ResourceNotFoundException;
import com.g47.cem.cemcontract.repository.ContractDeliveryScheduleRepository;
import com.g47.cem.cemcontract.repository.ContractDetailRepository;
import com.g47.cem.cemcontract.repository.ContractHistoryRepository;
import com.g47.cem.cemcontract.repository.ContractRepository;
import com.g47.cem.cemcontract.repository.ContractSignatureRepository;
import com.g47.cem.cemcontract.util.JwtUtil;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractDetailRepository contractDetailRepository;
    private final ContractDeliveryScheduleRepository contractDeliveryScheduleRepository;
    private final ContractHistoryRepository contractHistoryRepository;
    private final ContractSignatureRepository contractSignatureRepository;
    private final ExternalService externalService;
    private final EmailService emailService;
    private final ContractNumberGenerator contractNumberGenerator;
    private final FileStorageService fileStorageService; // Injected via @RequiredArgsConstructor
    private final Path fileStorageLocation; // Assuming this is configured
    private final ApplicationEventPublisher eventPublisher;
    private final JwtUtil jwtUtil; // Add JwtUtil injection

    @Transactional
    public ContractResponseDto createContract(CreateContractRequest requestDto, Authentication authentication) {
        String username = authentication.getName();
        
        // Auto-set staffId from multiple sources if not provided
        Long staffId = requestDto.getStaffId();
        if (staffId == null) {
            staffId = extractStaffIdFromAuthentication(authentication);
        }

        // Generate contract number first
        String contractNumber = contractNumberGenerator.generate();

        Contract contract = Contract.builder()
                .contractNumber(contractNumber)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .customerId(requestDto.getCustomerId())
                .staffId(staffId)
                .totalValue(requestDto.getTotalValue())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                // Điều 2: Thanh toán
                .paymentMethod(requestDto.getPaymentMethod())
                .paymentTerm(requestDto.getPaymentTerm())
                .bankAccount(requestDto.getBankAccount())
                // Điều 3: Thời gian, địa điểm, phương thức giao hàng (managed by delivery schedules table)
                // Điều 5: Bảo hành và hướng dẫn sử dụng hàng hóa
                .warrantyProduct(requestDto.getWarrantyProduct())
                .warrantyPeriodMonths(requestDto.getWarrantyPeriodMonths())
                .status(ContractStatus.DRAFT)
                .isHidden(false)
                .createdBy(username)
                .build();

        // Tạo contract details nếu có
        if (requestDto.getContractDetails() != null && !requestDto.getContractDetails().isEmpty()) {
            List<ContractDetail> details = requestDto.getContractDetails().stream().map(detailDto -> {
                ContractDetail detail = ContractDetail.builder()
                        .contract(contract)
                        .workCode(detailDto.getWorkCode())
                        .deviceId(detailDto.getDeviceId())
                        .description(detailDto.getDescription())
                        .quantity(detailDto.getQuantity())
                        .unitPrice(detailDto.getUnitPrice())
                        .warrantyMonths(detailDto.getWarrantyMonths())
                        .notes(detailDto.getNotes())
                        .build();
                detail.calculateTotalPrice();
                return detail;
            }).collect(Collectors.toList());
            
            contract.setContractDetails(details);
            contract.updateTotalValue();
        }
        
        // Tạo delivery schedules nếu có (Điều 3)
        if (requestDto.getDeliverySchedules() != null && !requestDto.getDeliverySchedules().isEmpty()) {
            List<ContractDeliverySchedule> deliverySchedules = new ArrayList<>();
            for (int i = 0; i < requestDto.getDeliverySchedules().size(); i++) {
                CreateContractRequest.CreateDeliveryScheduleRequest deliveryDto = requestDto.getDeliverySchedules().get(i);
                ContractDeliverySchedule schedule = ContractDeliverySchedule.builder()
                        .contract(contract)
                        .sequenceNumber(i + 1) // STT starts from 1
                        .itemName(deliveryDto.getItemName())
                        .unit(deliveryDto.getUnit())
                        .quantity(deliveryDto.getQuantity())
                        .deliveryTime(deliveryDto.getDeliveryTime())
                        .deliveryLocation(deliveryDto.getDeliveryLocation())
                        .notes(deliveryDto.getNotes())
                        .build();
                deliverySchedules.add(schedule);
            }
            contract.setDeliverySchedules(deliverySchedules);
        }

        Contract savedContract = contractRepository.save(contract);
        
        // Generate PDF từ Word template
        try {
            generateContractPdf(savedContract, username);
            savedContract.setStatus(ContractStatus.PENDING_SELLER_SIGNATURE);
            savedContract = contractRepository.save(savedContract);
        } catch (Exception e) {
            log.error("Failed to generate contract PDF for contract ID: {}", savedContract.getId(), e);
            // Không throw exception để contract vẫn được tạo, chỉ log error
        }
        
        addHistory(savedContract, ContractAction.CREATED, "Contract created by " + username, username, null, null);
        log.info("Contract created with ID: {}", savedContract.getId());
        return mapToDto(savedContract);
    }

    @Transactional
    public Contract createAndGenerateContract(ContractCreationRequestDto requestDto) {
        String contractNumber = contractNumberGenerator.generate();
        Contract contract = new Contract();
        contract.setContractNumber(contractNumber);
        String title = String.format("Hợp đồng mua bán giữa %s và %s",
                requestDto.getSeller().getCompanyName(),
                requestDto.getBuyer().getCompanyName());
        contract.setTitle(title);
        contract.setDescription(requestDto.getNotes());
        contract.setCustomerId(requestDto.getBuyer().getEntityId());
        contract.setStaffId(requestDto.getSeller().getEntityId());
        contract.setCreatedBy(requestDto.getSeller().getLegalRepresentative());

        List<ContractDetail> details = requestDto.getItems().stream().map(itemDto -> {
            ContractDetail detail = new ContractDetail();
            detail.setDescription(itemDto.getName()); // Use description instead of serviceName
            detail.setQuantity(itemDto.getQuantity().intValue());
            detail.setUnitPrice(itemDto.getUnitPrice());
                        detail.calculateTotalPrice();
            detail.setContract(contract);
                        return detail;
        }).collect(Collectors.toList());
            contract.setContractDetails(details);
            contract.updateTotalValue();

        String filePath = "uploads/contracts/" + contractNumber + ".pdf";
        try {
            generateContractPdf(contract, requestDto.getSeller().getLegalRepresentative());
        } catch (Exception e) {
            log.error("Failed to generate PDF for contract {}", contractNumber, e);
            throw new BusinessException("Failed to generate PDF from template.");
        }
        contract.setFilePath(filePath);
        contract.setStatus(ContractStatus.PENDING_SELLER_SIGNATURE);

        Contract savedContract = contractRepository.save(contract);
        String reason = "Contract generated from template by " + savedContract.getCreatedBy();
        addHistory(savedContract, ContractAction.CREATED, reason, savedContract.getCreatedBy(), null, ContractStatus.PENDING_SELLER_SIGNATURE);
        return savedContract;
    }

    /**
     * Tạo PDF từ Word template với thông tin contract đầy đủ
     */
    private void generateContractPdf(Contract contract, String username) throws Exception {
        // Contract number should already be set
        String contractNumber = contract.getContractNumber();
        if (contractNumber == null) {
            throw new BusinessException("Contract number is required for PDF generation");
        }
        
        String filePath = "uploads/contracts/" + contractNumber + ".pdf";
        
        // Load Word template
        java.io.InputStream templateInputStream = this.getClass().getClassLoader()
                .getResourceAsStream("templates/HD-mua-ban-hang-hoa2025.docx");
        if (templateInputStream == null) {
            throw new ResourceNotFoundException("Contract template not found.");
        }

        org.docx4j.openpackaging.packages.WordprocessingMLPackage wordMLPackage = 
                org.docx4j.openpackaging.packages.WordprocessingMLPackage.load(templateInputStream);
        org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart mainDocumentPart = 
                wordMLPackage.getMainDocumentPart();

        HashMap<String, String> mappings = new HashMap<>();
        
        // Thông tin chung
        mappings.put("contractNumber", contractNumber);
        mappings.put("documentDate", LocalDate.now().toString());
        
        // Thông tin bên bán (cố định theo yêu cầu)
        mappings.put("seller.companyName", "CÔNG TY TNHH KINH DOANH XUẤT NHẬP KHẨU TM VÀ SX THÀNH ĐẠT");
        mappings.put("seller.businessCode", "0901108513");
        mappings.put("seller.address", "Thôn Giữa, Xã Lạc Đạo, Huyện Văn Lâm, Tỉnh Hưng Yên");
        mappings.put("seller.legalRepresentative", "NGUYỄN NGỌC LAN");
        mappings.put("seller.position", "Giám đốc");
        mappings.put("seller.idCardNumber", "001203004433");
        mappings.put("seller.idIssueDate", "29/06/2023");
        mappings.put("seller.idIssuePlace", "Hà Nội");
        mappings.put("seller.phone", "0948 566416");
        mappings.put("seller.fax", "");
        
        // Lấy thông tin bên mua từ Customer service
        try {
            ExternalService.CustomerDto customer = externalService.getCustomerInfo(contract.getCustomerId());
            mappings.put("buyer.companyName", customer.getCompanyName());
            mappings.put("buyer.address", customer.getAddress());
            mappings.put("buyer.legalRepresentative", customer.getContactName());
            mappings.put("buyer.phone", customer.getPhone() != null ? customer.getPhone() : "");
            mappings.put("buyer.email", customer.getEmail() != null ? customer.getEmail() : "");
            mappings.put("buyer.businessCode", customer.getBusinessCode() != null ? customer.getBusinessCode() : "");
            mappings.put("buyer.taxCode", customer.getTaxCode() != null ? customer.getTaxCode() : "");
        } catch (Exception e) {
            log.warn("Could not fetch customer info, using placeholders", e);
            mappings.put("buyer.companyName", "KHÁCH HÀNG");
            mappings.put("buyer.address", "");
            mappings.put("buyer.legalRepresentative", "");
            mappings.put("buyer.phone", "");
            mappings.put("buyer.email", "");
            mappings.put("buyer.businessCode", "");
            mappings.put("buyer.taxCode", "");
        }
        
        // Điều 2: Thanh toán
        mappings.put("paymentMethod", contract.getPaymentMethod() != null ? contract.getPaymentMethod() : "");
        mappings.put("paymentTerm", contract.getPaymentTerm() != null ? contract.getPaymentTerm() : "");
        mappings.put("bankAccount", contract.getBankAccount() != null ? contract.getBankAccount() : "");
        
        // Điều 3: Giao hàng - build table from delivery schedules
        StringBuilder deliveryTableBuilder = new StringBuilder();
        if (contract.getDeliverySchedules() != null && !contract.getDeliverySchedules().isEmpty()) {
            for (ContractDeliverySchedule schedule : contract.getDeliverySchedules()) {
                deliveryTableBuilder.append(String.format(
                    "%d\t%s\t%s\t%d\t%s\t%s\t%s\n",
                    schedule.getSequenceNumber(),
                    schedule.getItemName(),
                    schedule.getUnit(),
                    schedule.getQuantity(),
                    schedule.getDeliveryTime() != null ? schedule.getDeliveryTime() : "",
                    schedule.getDeliveryLocation() != null ? schedule.getDeliveryLocation() : "",
                    schedule.getNotes() != null ? schedule.getNotes() : ""
                ));
            }
        }
        mappings.put("deliveryScheduleTable", deliveryTableBuilder.toString());
        
        // Điều 1: Contract Details table
        StringBuilder contractDetailsTableBuilder = new StringBuilder();
        if (contract.getContractDetails() != null && !contract.getContractDetails().isEmpty()) {
            for (int i = 0; i < contract.getContractDetails().size(); i++) {
                ContractDetail detail = contract.getContractDetails().get(i);
                contractDetailsTableBuilder.append(String.format(
                    "%d\t%s\t%s\t%d\t%.2f\t%.2f\t%d\t%s\n",
                    (i + 1),
                    detail.getWorkCode(),
                    detail.getDescription() != null ? detail.getDescription() : "",
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    detail.getTotalPrice(),
                    detail.getWarrantyMonths() != null ? detail.getWarrantyMonths() : 0,
                    detail.getNotes() != null ? detail.getNotes() : ""
                ));
            }
        }
        mappings.put("contractDetailsTable", contractDetailsTableBuilder.toString());
        
        // Điều 5: Bảo hành
        mappings.put("warrantyProduct", contract.getWarrantyProduct() != null ? contract.getWarrantyProduct() : "");
        mappings.put("warrantyPeriodMonths", contract.getWarrantyPeriodMonths() != null ? 
                contract.getWarrantyPeriodMonths().toString() : "");

        // Replace variables in template
        try {
            mainDocumentPart.variableReplace(mappings);
        } catch (JAXBException e) {
            log.error("JAXBException during variable replacement", e);
            throw new BusinessException("Error processing contract template placeholders.");
        }

        // Save as PDF
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(outputFile);
        org.docx4j.Docx4J.toPDF(wordMLPackage, os);
        os.flush();
        os.close();
        
        // Update contract with file path
        contract.setFilePath(filePath);
        
        log.info("Generated contract PDF: {}", filePath);
    }

    private void generatePdfFromTemplate(String pdfPath, ContractCreationRequestDto dto, String contractNumber) throws Exception {
        java.io.InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream("templates/HD-mua-ban-hang-hoa2025.docx");
        if (templateInputStream == null) throw new ResourceNotFoundException("Contract template not found.");

        org.docx4j.openpackaging.packages.WordprocessingMLPackage wordMLPackage = org.docx4j.openpackaging.packages.WordprocessingMLPackage.load(templateInputStream);
        org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        HashMap<String, String> mappings = new HashMap<>();
        mappings.put("contractNumber", contractNumber);
        mappings.put("seller.name", dto.getSeller().getCompanyName());
        mappings.put("buyer.name", dto.getBuyer().getCompanyName());

        try {
            mainDocumentPart.variableReplace(mappings);
        } catch (JAXBException e) {
            log.error("JAXBException during variable replacement", e);
            throw new BusinessException("Error processing contract template placeholders.");
        }

        File outputFile = new File(pdfPath);
        outputFile.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(outputFile);
        org.docx4j.Docx4J.toPDF(wordMLPackage, os);
        os.flush();
        os.close();
    }

    @Transactional
    public ContractResponseDto updateContract(Long id, UpdateContractRequest requestDto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new BusinessException("Only contracts in DRAFT status can be updated.");
        }
        BeanUtils.copyProperties(requestDto, contract, "id", "contractNumber", "status");
        contract.setUpdatedAt(LocalDateTime.now());
        Contract updatedContract = contractRepository.save(contract);
        log.info("Contract updated with ID: {}", updatedContract.getId());
        addHistory(updatedContract, ContractAction.UPDATED, "Contract details updated.", "SYSTEM", null, null);
        return mapToDto(updatedContract);
    }

    @Transactional
    public ContractResponseDto updateContractStatus(Long id, ContractStatus status, String updatedBy) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        ContractStatus oldStatus = contract.getStatus();
        contract.setStatus(status);
        contract.setUpdatedAt(LocalDateTime.now());
        String reason = "Status changed to " + status.getVietnameseName();
        addHistory(contract, ContractAction.STATUS_CHANGED, reason, updatedBy, oldStatus, status);
        Contract updatedContract = contractRepository.save(contract);
        log.info("Contract status updated for ID: {}. New status: {}", updatedContract.getId(), status);
        return mapToDto(updatedContract);
    }

    @Transactional
    public void deleteContract(Long id) {
        if (!contractRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contract not found with id: " + id);
        }
        contractRepository.deleteById(id);
        log.info("Contract deleted with ID: {}", id);
    }

    public ContractResponseDto getContractById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        return mapToDto(contract);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDto> getUnsignedContracts(Pageable pageable) {
        List<ContractStatus> unsignedStatuses = Arrays.asList(
                ContractStatus.PENDING_SELLER_SIGNATURE,
                ContractStatus.PENDING_CUSTOMER_SIGNATURE
        );
        Page<Contract> contractPage = contractRepository.findByStatusInAndIsHiddenFalse(unsignedStatuses, pageable);
        return contractPage.map(this::mapToDto);
    }

    public List<ContractResponseDto> getAllContracts() {
        return contractRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDto> getContractsForUser(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isManagerOrStaff = authorities.stream()
                .anyMatch(a -> {
                    String role = a.getAuthority();
                    return role.equals("MANAGER") || role.equals("STAFF") || role.equals("SUPPORT_TEAM") || role.equals("TECH_LEAD") || role.equals("TECHNICIAN");
                });

        if (isManagerOrStaff) {
            return contractRepository.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } else {
            // For CUSTOMER role
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Long userId = jwt.getClaim("userId"); // Assuming userId is a claim in the JWT

            if (userId == null) {
                // This case should ideally not happen for an authenticated customer
                return List.of();
            }

            return contractRepository.findByCustomerId(userId).stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }
    }

    private void addHistory(Contract contract, ContractAction action, String reason, String changedBy, ContractStatus oldStatus, ContractStatus newStatus) {
        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(action);
        history.setChangeReason(reason);
        history.setChangedBy(changedBy);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        contractHistoryRepository.save(history);
    }

    private ContractResponseDto mapToDto(Contract contract) {
        ContractResponseDto dto = new ContractResponseDto();
        BeanUtils.copyProperties(contract, dto);
        if (contract.getContractDetails() != null) {
            dto.setContractDetails(contract.getContractDetails().stream()
                    .map(this::mapDetailToDto).collect(Collectors.toList()));
        } else {
            dto.setContractDetails(Collections.emptyList());
        }
        
        // Map delivery schedules
        if (contract.getDeliverySchedules() != null) {
            dto.setDeliverySchedules(contract.getDeliverySchedules().stream()
                    .map(this::mapDeliveryScheduleToDto).collect(Collectors.toList()));
        } else {
            dto.setDeliverySchedules(Collections.emptyList());
        }
        
        return dto;
    }

    private ContractResponseDto.ContractDetailDto mapDetailToDto(ContractDetail detail) {
        ContractResponseDto.ContractDetailDto dto = new ContractResponseDto.ContractDetailDto();
        BeanUtils.copyProperties(detail, dto);
        // Use description as device name since we removed serviceName
        dto.setDeviceName(detail.getDescription());
        return dto;
    }

    private ContractResponseDto.DeliveryScheduleDto mapDeliveryScheduleToDto(ContractDeliverySchedule schedule) {
        ContractResponseDto.DeliveryScheduleDto dto = new ContractResponseDto.DeliveryScheduleDto();
        BeanUtils.copyProperties(schedule, dto);
        return dto;
    }

    @Transactional
    public void addSignatureToContract(Long contractId, SignatureRequestDto signatureRequestDto) throws Exception {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));

        if (contract.getFilePath() == null || contract.getFilePath().isEmpty()) {
            throw new BusinessException("Contract file does not exist.");
        }

        String signerType = signatureRequestDto.getSignerType();
        ContractStatus currentStatus = contract.getStatus();
        ContractStatus nextStatus;
        float xPosition;
        float yPosition = 100;

        if ("SELLER".equalsIgnoreCase(signerType)) {
            if (currentStatus != ContractStatus.PENDING_SELLER_SIGNATURE) {
                throw new BusinessException("Contract is not awaiting seller's signature.");
            }
            nextStatus = ContractStatus.PENDING_CUSTOMER_SIGNATURE;
            xPosition = 50;
            // Publish event after seller signs
            this.eventPublisher.publishEvent(new SellerSignedEvent(this, contract));

        } else if ("CUSTOMER".equalsIgnoreCase(signerType)) {
            if (currentStatus != ContractStatus.PENDING_CUSTOMER_SIGNATURE) {
                throw new BusinessException("Contract is not awaiting customer's signature.");
            }
            nextStatus = ContractStatus.ACTIVE;
            xPosition = 350;
        } else {
            throw new BusinessException("Invalid signer type.");
        }

        Path sourcePath = fileStorageService.loadFileAsPath(contract.getFilePath());
        File sourceFile = sourcePath.toFile();
        File tempDestFile = Files.createTempFile("signed-", ".pdf").toFile();

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(sourceFile), new PdfWriter(tempDestFile));
        Document document = new Document(pdfDoc);

        byte[] imageBytes = Base64.getDecoder().decode(signatureRequestDto.getSignatureImage().split(",")[1]);
        Image signatureImage = new Image(ImageDataFactory.create(imageBytes));
        signatureImage.setFixedPosition(pdfDoc.getNumberOfPages(), xPosition, yPosition);
        signatureImage.scaleToFit(150, 75);

        document.add(signatureImage);
        document.close();

        Files.delete(sourcePath);
        Files.move(tempDestFile.toPath(), sourcePath);

        contract.setStatus(nextStatus);

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.SIGNED);
        history.setOldStatus(currentStatus);
        history.setNewStatus(nextStatus);
        history.setChangedBy(signerType); // Placeholder, replace with actual user from security context
        history.setChangeReason("Contract signed by " + signerType);
        contractHistoryRepository.save(history);

        contractRepository.save(contract);
    }

    /**
     * Extract staff ID from authentication using multiple fallback strategies
     */
    private Long extractStaffIdFromAuthentication(Authentication authentication) {
        Long staffId = null;
        
        // Strategy 1: Try to get from JWT if principal is JWT object
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            log.debug("JWT Claims: {}", jwt.getClaims());
            
            // Try different possible claim names for user ID
            staffId = jwt.getClaim("userId");
            if (staffId == null) {
                staffId = jwt.getClaim("user_id");
            }
            if (staffId == null) {
                staffId = jwt.getClaim("id");
            }
            if (staffId == null) {
                // Try to get from "sub" claim and parse as Long
                String subClaim = jwt.getClaim("sub");
                if (subClaim != null) {
                    try {
                        staffId = Long.parseLong(subClaim);
                    } catch (NumberFormatException e) {
                        log.warn("Cannot parse 'sub' claim as Long: {}", subClaim);
                    }
                }
            }
            
            log.debug("Extracted staffId from JWT: {}", staffId);
            if (staffId != null) {
                return staffId;
            }
        }
        
        // Strategy 2: Try to get from request attributes (set by JwtAuthenticationFilter)
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr instanceof Long) {
                staffId = (Long) userIdAttr;
                log.debug("Extracted staffId from request attributes: {}", staffId);
                if (staffId != null) {
                    return staffId;
                }
            }
        } catch (Exception e) {
            log.debug("Could not get request attributes: {}", e.getMessage());
        }
        
        // Strategy 3: Try to extract from Authorization header using JwtUtil
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                staffId = jwtUtil.extractUserId(jwt);
                log.debug("Extracted staffId using JwtUtil: {}", staffId);
                if (staffId != null) {
                    return staffId;
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract userId using JwtUtil: {}", e.getMessage());
        }
        
        log.warn("Authentication principal is not a JWT: {}. Could not extract staffId from any source.", 
                 authentication.getPrincipal().getClass());
        
        throw new BusinessException("Staff ID could not be determined from authentication. Please provide staffId in request or ensure JWT contains user ID claims.");
    }
} 