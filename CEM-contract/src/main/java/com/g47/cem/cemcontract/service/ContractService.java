package com.g47.cem.cemcontract.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemcontract.dto.request.ContractCreationRequestDto;
import com.g47.cem.cemcontract.dto.request.CreateContractRequest;
import com.g47.cem.cemcontract.dto.request.SignatureRequestDto;
import com.g47.cem.cemcontract.dto.request.UpdateContractRequest;
import com.g47.cem.cemcontract.dto.response.ContractResponseDto;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.ContractDeliverySchedule;
import com.g47.cem.cemcontract.entity.ContractDetail;
import com.g47.cem.cemcontract.entity.ContractHistory;
import com.g47.cem.cemcontract.entity.ContractSignature;
import com.g47.cem.cemcontract.enums.ContractAction;
import com.g47.cem.cemcontract.enums.ContractStatus;
import com.g47.cem.cemcontract.enums.SignatureType;
import com.g47.cem.cemcontract.enums.SignerType;
import com.g47.cem.cemcontract.event.SellerSignedEvent;
import com.g47.cem.cemcontract.exception.BusinessException;
import com.g47.cem.cemcontract.exception.ResourceNotFoundException;
import com.g47.cem.cemcontract.repository.ContractDeliveryScheduleRepository;
import com.g47.cem.cemcontract.repository.ContractDetailRepository;
import com.g47.cem.cemcontract.repository.ContractHistoryRepository;
import com.g47.cem.cemcontract.repository.ContractRepository;
import com.g47.cem.cemcontract.repository.ContractSignatureRepository;
import com.g47.cem.cemcontract.service.ExternalService.CustomerDto;
import com.g47.cem.cemcontract.util.MoneyToWords;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.JAXBElement;
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
    private final GoogleDriveService googleDriveService; // For cloud storage
    private final Path fileStorageLocation; // Assuming this is configured
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ContractResponseDto createContract(CreateContractRequest requestDto, String username, HttpServletRequest request, String authToken) {
        // Generate contract number
        String contractNumber = contractNumberGenerator.generate();
        
        // Determine staffId - use from request if provided, otherwise get from JWT token
        Long staffId = requestDto.getStaffId();
        if (staffId == null) {
            // Get userId from request attributes (set by JwtAuthenticationFilter)
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj instanceof Long) {
                staffId = (Long) userIdObj;
            } else if (userIdObj instanceof Integer) {
                staffId = ((Integer) userIdObj).longValue();
            }
        }
        
        // Validate that we have a staffId
        if (staffId == null) {
            throw new BusinessException("Unable to determine staff ID for contract creation");
        }
        
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
            log.info("Starting PDF generation for contract ID: {}", savedContract.getId());
            String googleDriveFileId = generateContractPdf(savedContract, username, authToken);
            savedContract.setFilePath(googleDriveFileId); // Store Google Drive file ID
            savedContract.setStatus(ContractStatus.PENDING_SELLER_SIGNATURE);
            savedContract = contractRepository.save(savedContract);
            log.info("Contract PDF generated successfully. Google Drive ID: {}", googleDriveFileId);
        } catch (Exception e) {
            log.error("Failed to generate contract PDF for contract ID: {} - Error: {}", savedContract.getId(), e.getMessage(), e);
            log.error("Full stack trace:", e);
            // Để contract trong status DRAFT và không có filePath, nhưng log chi tiết để debug
            // Trong tương lai có thể retry hoặc fallback
            throw new BusinessException("Failed to generate contract PDF: " + e.getMessage());
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

        try {
            // authToken is missing here, we might need to adjust how this method is called or retrieve it.
            // For now, let's assume it's not available in this flow and it might fail or use placeholders.
            // A proper fix would involve getting the token for the system/user creating the contract.
            String googleDriveFileId = generateContractPdf(contract, requestDto.getSeller().getLegalRepresentative(), null); // Passing null for authToken
            contract.setFilePath(googleDriveFileId); // Store Google Drive file ID
        } catch (Exception e) {
            log.error("Failed to generate PDF for contract {}", contractNumber, e);
            throw new BusinessException("Failed to generate PDF from template.");
        }
        contract.setStatus(ContractStatus.PENDING_SELLER_SIGNATURE);

        Contract savedContract = contractRepository.save(contract);
        String reason = "Contract generated from template by " + savedContract.getCreatedBy();
        addHistory(savedContract, ContractAction.CREATED, reason, savedContract.getCreatedBy(), null, ContractStatus.PENDING_SELLER_SIGNATURE);
        return savedContract;
    }

    /**
     * Tạo PDF từ Word template với thông tin contract đầy đủ
     * @return Google Drive file ID
     */
    private String generateContractPdf(Contract contract, String username, String authToken) throws Exception {
        log.info("generateContractPdf called for contract ID: {}", contract.getId());

        if (contract.getContractNumber() == null) {
            contract.setContractNumber(contractNumberGenerator.generate());
        }

        String contractNumber = contract.getContractNumber();
        String filePath = "uploads/contracts/" + contractNumber + ".pdf";
        log.info("Contract number: {}, Local file path: {}", contractNumber, filePath);

        java.io.InputStream templateInputStream = this.getClass().getClassLoader()
                .getResourceAsStream("templates/HD-mua-ban-hang-hoa2025.docx");
        if (templateInputStream == null) {
            log.error("Template file not found at templates/HD-mua-ban-hang-hoa2025.docx");
            throw new ResourceNotFoundException("Contract template not found.");
        }

        org.docx4j.openpackaging.packages.WordprocessingMLPackage wordMLPackage = org.docx4j.openpackaging.packages.WordprocessingMLPackage.load(templateInputStream);
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        
        CustomerDto buyer = externalService.getCustomerInfo(contract.getCustomerId(), authToken);
        // Remove seller info retrieval and mapping
        // UserResponse seller = externalService.getUserById(contract.getStaffId(), authToken);

        HashMap<String, String> mappings = prepareMappings(contract, buyer);
        
        // Log all mappings for debugging
        log.info("Applying {} mappings to contract template:", mappings.size());
        mappings.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                log.info("  {} -> {}", key, value);
            }
        });

        // Apply variable replacements - sometimes need multiple passes for nested variables
        try {
            log.info("First pass of variable replacement...");
            mainDocumentPart.variableReplace(mappings);
            
            // Second pass to catch any variables that might have been revealed after first replacement
            log.info("Second pass of variable replacement...");
            mainDocumentPart.variableReplace(mappings);
            
            log.info("Variable replacement completed successfully");
        } catch (Exception e) {
            log.error("Error during variable replacement: {}", e.getMessage(), e);
            throw e;
        }

        populateItemsTable(wordMLPackage, contract.getContractDetails());
        populateDeliveryScheduleTable(wordMLPackage, contract.getDeliverySchedules());

        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs();
        
        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            org.docx4j.Docx4J.toPDF(wordMLPackage, os);
            os.flush();
        }

        log.info("Uploading PDF to Google Drive...");
        String googleDriveFileId = googleDriveService.uploadFile(outputFile, contractNumber + ".pdf");
        
        Files.deleteIfExists(outputFile.toPath());

        return googleDriveFileId;
    }

    private HashMap<String, String> prepareMappings(Contract contract, CustomerDto buyer) {
        HashMap<String, String> mappings = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        // Contract header information
        mappings.put("contract_number", contract.getContractNumber() != null ? contract.getContractNumber() : "");
        mappings.put("contract_date", String.valueOf(today.getDayOfMonth()));
        mappings.put("contract_month", String.valueOf(today.getMonthValue()));
        mappings.put("contract_year", String.valueOf(today.getYear()));

        // Alternative naming patterns for contract date
        mappings.put("day", String.valueOf(today.getDayOfMonth()));
        mappings.put("month", String.valueOf(today.getMonthValue()));
        mappings.put("year", String.valueOf(today.getYear()));

        // Buyer (Bên B) information - mapping to customer details
        if (buyer != null) {
            // Tên doanh nghiệp
            String companyName = buyer.getCompanyName() != null ? buyer.getCompanyName() : "";
            mappings.put("buyer_company_name", companyName);
            mappings.put("company_name", companyName); // Alternative naming
            
            // Mã số doanh nghiệp (use taxCode or businessCode)
            String taxCode = "";
            if (buyer.getTaxCode() != null && !buyer.getTaxCode().isBlank()) {
                taxCode = buyer.getTaxCode();
            } else if (buyer.getBusinessCode() != null && !buyer.getBusinessCode().isBlank()) {
                taxCode = buyer.getBusinessCode();
            }
            mappings.put("buyer_tax_code", taxCode);
            mappings.put("tax_code", taxCode); // Alternative naming
            mappings.put("business_code", taxCode); // Alternative naming
            
            // Địa chỉ trụ sở chính
            String address = buyer.getAddress() != null ? buyer.getAddress() : "";
            mappings.put("buyer_address", address);
            mappings.put("address", address); // Alternative naming
            mappings.put("company_address", address); // Alternative naming
            
            // Người đại diện theo pháp luật
            String representative = buyer.getContactName() != null ? buyer.getContactName() : "";
            mappings.put("buyer_representative", representative);
            mappings.put("representative", representative); // Alternative naming
            mappings.put("legal_representative", representative); // Alternative naming
            
            // Chức danh (placeholder - not in customer data)
            mappings.put("buyer_position", "");
            mappings.put("position", "");
            mappings.put("title", "");
            
            // CMND/CCCD/Hộ chiếu số (placeholder - not in customer data)
            mappings.put("buyer_id_number", "");
            mappings.put("id_number", "");
            mappings.put("cmnd", "");
            mappings.put("cccd", "");
            mappings.put("buyer_id_issue_date", "");
            mappings.put("id_issue_date", "");
            mappings.put("buyer_id_issue_place", "");
            mappings.put("id_issue_place", "");
            
            // Số điện thoại
            String phone = buyer.getPhone() != null ? buyer.getPhone() : "";
            mappings.put("buyer_phone", phone);
            mappings.put("phone", phone); // Alternative naming
            mappings.put("telephone", phone); // Alternative naming
            
            // Fax (use empty as not available in customer data)
            mappings.put("buyer_fax", "");
            mappings.put("fax", "");
            
            // Email
            String email = buyer.getEmail() != null ? buyer.getEmail() : "";
            mappings.put("buyer_email", email);
            mappings.put("email", email);
        } else {
            // If buyer is null, set all buyer fields to empty
            String[] buyerFields = {
                "buyer_company_name", "company_name",
                "buyer_tax_code", "tax_code", "business_code", 
                "buyer_address", "address", "company_address",
                "buyer_representative", "representative", "legal_representative",
                "buyer_position", "position", "title",
                "buyer_id_number", "id_number", "cmnd", "cccd",
                "buyer_id_issue_date", "id_issue_date",
                "buyer_id_issue_place", "id_issue_place",
                "buyer_phone", "phone", "telephone",
                "buyer_fax", "fax",
                "buyer_email", "email"
            };
            for (String field : buyerFields) {
                mappings.put(field, "");
            }
        }

        // Payment information (Điều 2)
        if (contract.getPaymentTerm() != null && !contract.getPaymentTerm().isBlank()) {
            mappings.put("payment_due_date", contract.getPaymentTerm());
            mappings.put("payment_term", contract.getPaymentTerm());
            mappings.put("payment_deadline", contract.getPaymentTerm());
        } else {
            mappings.put("payment_due_date", "");
            mappings.put("payment_term", "");
            mappings.put("payment_deadline", "");
        }
        
        // Payment method
        if (contract.getPaymentMethod() != null) {
            String paymentMethod = contract.getPaymentMethod().toString();
            mappings.put("payment_method", paymentMethod);
            mappings.put("payment_type", paymentMethod);
        } else {
            mappings.put("payment_method", "");
            mappings.put("payment_type", "");
        }
        
        // Bank account information
        if (contract.getBankAccount() != null && !contract.getBankAccount().isBlank()) {
            mappings.put("bank_account", contract.getBankAccount());
            mappings.put("account_number", contract.getBankAccount());
        } else {
            mappings.put("bank_account", "");
            mappings.put("account_number", "");
        }
        
        // Transportation and delivery costs (placeholders)
        mappings.put("transport_responsibility", "");
        mappings.put("transport_cost", "");
        mappings.put("loading_cost_responsibility", "");
        mappings.put("storage_cost_per_day", "");
        mappings.put("storage_fee", "");
        mappings.put("quality_check_deadline", "");
        mappings.put("complaint_deadline", "");
        mappings.put("inspection_deadline", "");

        // Warranty information (Điều 5)
        if (contract.getWarrantyPeriodMonths() != null) {
            String warrantyPeriod = contract.getWarrantyPeriodMonths().toString();
            mappings.put("warranty_period", warrantyPeriod);
            mappings.put("warranty_months", warrantyPeriod);
            mappings.put("warranty_time", warrantyPeriod);
        } else {
            mappings.put("warranty_period", "");
            mappings.put("warranty_months", "");
            mappings.put("warranty_time", "");
        }
        
        if (contract.getWarrantyProduct() != null && !contract.getWarrantyProduct().isBlank()) {
            mappings.put("warranty_product", contract.getWarrantyProduct());
            mappings.put("warranty_goods", contract.getWarrantyProduct());
        } else {
            mappings.put("warranty_product", "");
            mappings.put("warranty_goods", "");
        }

        // Contract penalty (Điều 7) - default values
        mappings.put("penalty_percentage", "100");
        mappings.put("violation_penalty", "100");
        
        // Contract dates and timing
        if (contract.getStartDate() != null) {
            mappings.put("start_date", contract.getStartDate().toString());
            mappings.put("contract_start_date", contract.getStartDate().toString());
        } else {
            mappings.put("start_date", "");
            mappings.put("contract_start_date", "");
        }
        
        if (contract.getEndDate() != null) {
            mappings.put("end_date", contract.getEndDate().toString());
            mappings.put("contract_end_date", contract.getEndDate().toString());
        } else {
            mappings.put("end_date", "");
            mappings.put("contract_end_date", "");
        }
        
        // Contract value
        if (contract.getTotalValue() != null) {
            mappings.put("total_value", String.format("%,.0f", contract.getTotalValue()));
            mappings.put("contract_value", String.format("%,.0f", contract.getTotalValue()));
        } else {
            mappings.put("total_value", "");
            mappings.put("contract_value", "");
        }
        
        // Additional placeholders for any other fields in the template
        mappings.put("contract_location", "");
        mappings.put("signing_location", "");
        mappings.put("intermediate_inspection_agency", "");
        mappings.put("inspection_agency", "");
        mappings.put("dispute_resolution", "Tòa án có thẩm quyền");
        
        // Contract description
        if (contract.getDescription() != null && !contract.getDescription().isBlank()) {
            mappings.put("contract_description", contract.getDescription());
            mappings.put("description", contract.getDescription());
        } else {
            mappings.put("contract_description", "");
            mappings.put("description", "");
        }
        
        // Contract title
        if (contract.getTitle() != null && !contract.getTitle().isBlank()) {
            mappings.put("contract_title", contract.getTitle());
        } else {
            mappings.put("contract_title", "");
        }
        
        // Additional common Vietnamese contract placeholders
        // Based on the PDF content you provided, these are likely placeholders:
        
        // For "ngày...tháng...năm..." pattern
        mappings.put("ngay", String.valueOf(today.getDayOfMonth()));
        mappings.put("thang", String.valueOf(today.getMonthValue()));
        mappings.put("nam", String.valueOf(today.getYear()));
        
        // Contract number variations
        mappings.put("so_hop_dong", contract.getContractNumber() != null ? contract.getContractNumber() : "");
        mappings.put("contract_no", contract.getContractNumber() != null ? contract.getContractNumber() : "");
        
        // For seller information (Bên A) - keep empty as requested
        mappings.put("seller_company_name", "");
        mappings.put("seller_tax_code", ""); 
        mappings.put("seller_address", "");
        mappings.put("seller_representative", "");
        mappings.put("seller_position", "");
        mappings.put("seller_id_number", "");
        mappings.put("seller_phone", "");
        mappings.put("seller_fax", "");
        
        // Vietnamese specific terms for buyer
        mappings.put("ten_doanh_nghiep", buyer != null && buyer.getCompanyName() != null ? buyer.getCompanyName() : "");
        
        // Fix complex ternary operator for ma_so_doanh_nghiep
        String maSoDoanh = "";
        if (buyer != null) {
            if (buyer.getTaxCode() != null && !buyer.getTaxCode().isBlank()) {
                maSoDoanh = buyer.getTaxCode();
            } else if (buyer.getBusinessCode() != null && !buyer.getBusinessCode().isBlank()) {
                maSoDoanh = buyer.getBusinessCode();
            }
        }
        mappings.put("ma_so_doanh_nghiep", maSoDoanh);
        
        mappings.put("dia_chi_tru_so", buyer != null && buyer.getAddress() != null ? buyer.getAddress() : "");
        mappings.put("nguoi_dai_dien", buyer != null && buyer.getContactName() != null ? buyer.getContactName() : "");
        mappings.put("so_dien_thoai", buyer != null && buyer.getPhone() != null ? buyer.getPhone() : "");
        
        // Payment terms in Vietnamese
        mappings.put("hinh_thuc_thanh_toan", contract.getPaymentMethod() != null ? contract.getPaymentMethod().toString() : "");
        mappings.put("thoi_han_thanh_toan", contract.getPaymentTerm() != null ? contract.getPaymentTerm() : "");
        
        // Warranty in Vietnamese
        mappings.put("thoi_gian_bao_hanh", contract.getWarrantyPeriodMonths() != null ? contract.getWarrantyPeriodMonths().toString() : "");
        mappings.put("san_pham_bao_hanh", contract.getWarrantyProduct() != null ? contract.getWarrantyProduct() : "");
        
        // Common contract fields that might appear
        mappings.put("gia_tri_hop_dong", contract.getTotalValue() != null ? String.format("%,.0f", contract.getTotalValue()) : "");
        mappings.put("tong_gia_tri", contract.getTotalValue() != null ? String.format("%,.0f", contract.getTotalValue()) : "");
        
        log.debug("Prepared {} total mappings for contract template", mappings.size());
        
        return mappings;
    }

    private void populateItemsTable(WordprocessingMLPackage wordMLPackage, List<ContractDetail> items) throws Exception {
        if (items == null || items.isEmpty()) return;
        
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        ObjectFactory factory = new ObjectFactory();
        
        // The items table is the first table in the document
        Object tableObj = mainDocumentPart.getJAXBNodesViaXPath("//w:tbl", false).get(0);
        Tbl table;
        if (tableObj instanceof Tbl) {
            table = (Tbl) tableObj;
        } else if (tableObj instanceof JAXBElement) {
            table = (Tbl) ((JAXBElement<?>) tableObj).getValue();
        } else {
            throw new IllegalStateException("Unable to locate items table in the template");
        }

        Object rowObj = table.getContent().get(1);
        Tr templateRow;
        if (rowObj instanceof Tr) {
            templateRow = (Tr) rowObj;
        } else if (rowObj instanceof JAXBElement) {
            templateRow = (Tr) ((JAXBElement<?>) rowObj).getValue();
        } else {
            throw new IllegalStateException("Unable to locate template row in the items table");
        }
        
        for (int i = 0; i < items.size(); i++) {
            ContractDetail item = items.get(i);
            Tr workingRow = (i == 0) ? templateRow : (Tr) org.docx4j.XmlUtils.deepCopy(templateRow);

            replaceTableCellText(workingRow, 0, String.valueOf(i + 1));
            replaceTableCellText(workingRow, 1, item.getDescription());
            replaceTableCellText(workingRow, 2, "Cái"); // Assuming unit
            replaceTableCellText(workingRow, 3, String.valueOf(item.getQuantity()));
            replaceTableCellText(workingRow, 4, String.format("%,.0f", item.getUnitPrice()));
            replaceTableCellText(workingRow, 5, String.format("%,.0f", item.getTotalPrice()));
            replaceTableCellText(workingRow, 6, item.getNotes());
            
            if (i > 0) {
                table.getContent().add(workingRow);
            }
        }
        
        double total = items.stream().mapToDouble(d -> d.getTotalPrice().doubleValue()).sum();
        String totalInWords = MoneyToWords.convert(total);

        HashMap<String, String> totalMappings = new HashMap<>();
        totalMappings.put("total_amount", String.format("%,.0f", total));
        totalMappings.put("total_in_words", totalInWords);
        mainDocumentPart.variableReplace(totalMappings);
    }

    private void populateDeliveryScheduleTable(WordprocessingMLPackage wordMLPackage, List<ContractDeliverySchedule> schedules) throws Exception {
        if (schedules == null || schedules.isEmpty()) return;

        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        
        // The delivery schedule table is expected to be the second table (index 1). Try XPath first, then fall back
        Tbl table;
        try {
            Object tableObj = mainDocumentPart.getJAXBNodesViaXPath("//w:tbl", false).get(1);
            table = (Tbl) (tableObj instanceof JAXBElement ? ((JAXBElement<?>) tableObj).getValue() : tableObj);
        } catch (Exception ex) {
            // Fallback: traverse document content manually
            log.warn("XPath retrieval failed for delivery schedule table, falling back to traversal: {}", ex.getMessage());
            table = findTableByIndex(mainDocumentPart, 1);
        }

        Object rowObj = table.getContent().get(1);
        Tr templateRow;
        if (rowObj instanceof Tr) {
            templateRow = (Tr) rowObj;
        } else if (rowObj instanceof JAXBElement) {
            templateRow = (Tr) ((JAXBElement<?>) rowObj).getValue();
        } else {
            throw new IllegalStateException("Unable to locate template row in the delivery schedule table");
        }
        
        for (int i = 0; i < schedules.size(); i++) {
            ContractDeliverySchedule schedule = schedules.get(i);
            Tr workingRow = (i == 0) ? templateRow : (Tr) org.docx4j.XmlUtils.deepCopy(templateRow);

            replaceTableCellText(workingRow, 0, String.valueOf(i + 1));
            replaceTableCellText(workingRow, 1, schedule.getItemName());
            replaceTableCellText(workingRow, 2, schedule.getUnit());
            replaceTableCellText(workingRow, 3, String.valueOf(schedule.getQuantity()));
            replaceTableCellText(workingRow, 4, schedule.getDeliveryTime().toString());
            replaceTableCellText(workingRow, 5, schedule.getDeliveryLocation());
            replaceTableCellText(workingRow, 6, schedule.getNotes());

            if (i > 0) {
                table.getContent().add(workingRow);
            }
        }
    }
    
    private void replaceTableCellText(Tr tableRow, int cellIndex, String text) {
        List<Object> cells = tableRow.getContent();
        if (cells.size() <= cellIndex) {
            return; // Nothing to update
        }

        Object cellObj = cells.get(cellIndex);
        Tc tc;
        if (cellObj instanceof Tc) {
            tc = (Tc) cellObj;
        } else if (cellObj instanceof JAXBElement) {
            tc = (Tc) ((JAXBElement<?>) cellObj).getValue();
        } else {
            return; // Unexpected structure, skip
        }

        // Ensure paragraph/ run / text hierarchy exists
        if (tc.getContent().isEmpty()) {
            ObjectFactory factory = new ObjectFactory();
            P newP = factory.createP();
            R newR = factory.createR();
            Text newText = factory.createText();
            newText.setValue(text != null ? text : "");
            newR.getContent().add(newText);
            newP.getContent().add(newR);
            tc.getContent().add(newP);
            return;
        }

        // Existing paragraph
        Object pObj = tc.getContent().get(0);
        P p;
        if (pObj instanceof P) {
            p = (P) pObj;
        } else if (pObj instanceof JAXBElement) {
            p = (P) ((JAXBElement<?>) pObj).getValue();
        } else {
            return;
        }

        // Ensure run exists
        if (p.getContent().isEmpty()) {
            ObjectFactory factory = new ObjectFactory();
            R newR = factory.createR();
            Text newText = factory.createText();
            newText.setValue(text != null ? text : "");
            newR.getContent().add(newText);
            p.getContent().add(newR);
            return;
        }

        Object rObj = p.getContent().get(0);
        R r;
        if (rObj instanceof R) {
            r = (R) rObj;
        } else if (rObj instanceof JAXBElement) {
            r = (R) ((JAXBElement<?>) rObj).getValue();
        } else {
            return;
        }

        // Ensure text element exists
        if (r.getContent().isEmpty()) {
            ObjectFactory factory = new ObjectFactory();
            Text newText = factory.createText();
            newText.setValue(text != null ? text : "");
            r.getContent().add(newText);
            return;
        }

        Object textObj = r.getContent().get(0);
        Text t;
        if (textObj instanceof Text) {
            t = (Text) textObj;
        } else if (textObj instanceof JAXBElement) {
            t = (Text) ((JAXBElement<?>) textObj).getValue();
        } else {
            return;
        }

        t.setValue(text != null ? text : "");
    }

    /**
     * Traverse the document content and return the Tbl element at the given logical index.
     */
    private Tbl findTableByIndex(MainDocumentPart mainDocumentPart, int index) {
        int tblCount = 0;
        for (Object obj : mainDocumentPart.getContent()) {
            Object unwrapped = org.docx4j.XmlUtils.unwrap(obj);
            if (unwrapped instanceof Tbl) {
                if (tblCount == index) {
                    return (Tbl) unwrapped;
                }
                tblCount++;
            }
        }
        throw new IllegalStateException("Unable to locate table with index " + index);
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

        mainDocumentPart.variableReplace(mappings);

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

    @Transactional
    public ContractResponseDto hideContract(Long id, String changedBy) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        
        contract.hide();
        Contract savedContract = contractRepository.save(contract);
        
        addHistory(savedContract, ContractAction.HIDDEN, "Contract hidden", changedBy, null, null);
        log.info("Contract hidden with ID: {} by user: {}", id, changedBy);
        
        return mapToDto(savedContract);
    }

    @Transactional
    public ContractResponseDto restoreContract(Long id, String changedBy) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        
        contract.show();
        Contract savedContract = contractRepository.save(contract);
        
        addHistory(savedContract, ContractAction.RESTORED, "Contract restored", changedBy, null, null);
        log.info("Contract restored with ID: {} by user: {}", id, changedBy);
        
        return mapToDto(savedContract);
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

    @Transactional(readOnly = true)
    public Page<ContractResponseDto> getHiddenContracts(Pageable pageable) {
        Page<Contract> contractPage = contractRepository.findByIsHiddenTrue(pageable);
        return contractPage.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDto> getSignedContracts(Pageable pageable) {
        Page<Contract> contractPage = contractRepository.findByStatusAndIsHiddenFalse(ContractStatus.ACTIVE, pageable);
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

            // For CUSTOMER role, we need to find contracts where the customer email matches the user email
            // Since the customer user was created from customer info, emails should match
            String userEmail = authentication.getName();
            log.debug("Looking for contracts for customer user: {} (ID: {})", userEmail, userId);

            // Get all contracts and filter by customer email through external service
            List<Contract> allContracts = contractRepository.findAll();
            List<Contract> customerContracts = allContracts.stream()
                    .filter(contract -> {
                        try {
                            // Get customer info for this contract
                            CustomerDto customer = externalService.getCustomerInfo(contract.getCustomerId(), null);
                            return customer != null && userEmail.equals(customer.getEmail());
                        } catch (Exception e) {
                            log.warn("Error checking customer email for contract {}: {}", contract.getId(), e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            log.info("Found {} contracts for customer user: {}", customerContracts.size(), userEmail);
            return customerContracts.stream()
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
    
    // Legacy signature methods removed - use DigitalSignatureService for new PAdES-compliant signing
    // Keeping legacy endpoint for backward compatibility in ContractController
    
    @Transactional
    public void signContract(Long contractId, SignatureRequestDto signatureRequestDto, Authentication authentication) throws Exception {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + contractId));

        // Check if contract is already digitally signed
        if (Boolean.TRUE.equals(contract.getDigitalSigned())) {
            log.warn("Contract {} is already digitally signed. Skipping legacy signature.", contractId);
            return; // Silently return to avoid breaking frontend
        }

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isManager = authorities.stream().anyMatch(a -> a.getAuthority().equals("MANAGER"));
        boolean isCustomer = authorities.stream().anyMatch(a -> a.getAuthority().equals("CUSTOMER"));

        // Create legacy signature record for backward compatibility
        ContractSignature signature = ContractSignature.builder()
                .contract(contract)
                .signerType(isManager ? SignerType.MANAGER : SignerType.CUSTOMER)
                .signerName(username)
                .signerEmail(username)
                .signatureType(SignatureType.DIGITAL_IMAGE)
                .signatureData(signatureRequestDto.getSignature())
                .signedAt(LocalDateTime.now())
                .build();
        contractSignatureRepository.save(signature);

        // Update contract status based on signer
        if (isManager && contract.getStatus() == ContractStatus.PENDING_SELLER_SIGNATURE) {
            contract.setStatus(ContractStatus.PENDING_CUSTOMER_SIGNATURE);
            addHistory(contract, ContractAction.SIGNED, "Seller (manager) signed contract (legacy)", username, null, null);
            eventPublisher.publishEvent(new SellerSignedEvent(this, contract));
        } else if (isCustomer && contract.getStatus() == ContractStatus.PENDING_CUSTOMER_SIGNATURE) {
            contract.setStatus(ContractStatus.ACTIVE);
            addHistory(contract, ContractAction.SIGNED, "Customer signed contract (legacy)", username, null, null);
        } else {
            throw new BusinessException("Contract is not in a valid state for signing.");
        }
        
        contractRepository.save(contract);
        
        log.warn("Legacy signature method used for contract {}. Consider migrating to DigitalSignatureService.", contractId);
    }
} 