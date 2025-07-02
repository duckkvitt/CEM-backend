package com.g47.cem.cemcontract.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.g47.cem.cemcontract.dto.request.CreateContractRequest;
import com.g47.cem.cemcontract.dto.request.SignContractRequest;
import com.g47.cem.cemcontract.dto.request.UpdateContractRequest;
import com.g47.cem.cemcontract.dto.response.ContractResponse;
import com.g47.cem.cemcontract.entity.Contract;
import com.g47.cem.cemcontract.entity.ContractDetail;
import com.g47.cem.cemcontract.entity.ContractSignature;
import com.g47.cem.cemcontract.enums.ContractStatus;
import com.g47.cem.cemcontract.enums.SignatureType;
import com.g47.cem.cemcontract.enums.SignerType;
import com.g47.cem.cemcontract.exception.BusinessException;
import com.g47.cem.cemcontract.exception.ResourceNotFoundException;
import com.g47.cem.cemcontract.repository.ContractRepository;
import com.g47.cem.cemcontract.repository.ContractSignatureRepository;
import com.g47.cem.cemcontract.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;
    private final ContractSignatureRepository contractSignatureRepository;
    private final ExternalService externalService;
    private final EmailService emailService;

    // -------------------- CREATE --------------------
    public ContractResponse createContract(CreateContractRequest request, String jwt) {
        // Remove "Bearer " prefix if present
        String token = jwt != null && jwt.startsWith("Bearer ") ? jwt.substring(7) : jwt;
        
        log.debug("Processing contract creation with JWT token");
        
        // Validate JWT token
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException("Authorization token is required");
        }
        
        // Validate JWT token format and extract user information
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("Invalid or expired authorization token");
        }
        
        Long staffId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.extractUsername(token);
        
        // Validate extracted user information
        if (staffId == null) {
            log.error("Failed to extract user ID from JWT token. Token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new BusinessException("Invalid token: user ID not found");
        }
        
        if (username == null || username.trim().isEmpty()) {
            log.error("Failed to extract username from JWT token. Token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            throw new BusinessException("Invalid token: username not found");
        }
        
        log.info("Creating contract for staffId: {}, username: {}", staffId, username);
        
        String contractNumber = generateContractNumber();

        Contract contract = Contract.builder()
                .contractNumber(contractNumber)
                .customerId(request.getCustomerId())
                .staffId(staffId)
                .title(request.getTitle())
                .description(request.getDescription())
                .filePath(request.getFilePath())
                .totalValue(request.getTotalValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ContractStatus.UNSIGNED)
                .isHidden(false)
                .createdBy(username)
                .build();

        // Map details if any
        if (request.getContractDetails() != null) {
            List<ContractDetail> details = request.getContractDetails().stream()
                    .map(d -> {
                        ContractDetail detail = ContractDetail.builder()
                                .contract(contract)
                                .workCode(d.getWorkCode())
                                .deviceId(d.getDeviceId())
                                .serviceName(d.getServiceName())
                                .description(d.getDescription())
                                .quantity(d.getQuantity())
                                .unitPrice(d.getUnitPrice())
                                .warrantyMonths(d.getWarrantyMonths())
                                .notes(d.getNotes())
                                .build();
                        detail.calculateTotalPrice();
                        return detail;
                    })
                    .collect(Collectors.toList());
            contract.setContractDetails(details);
            contract.updateTotalValue();
        }

        // Ensure total value is set even if no details provided
        if (contract.getTotalValue() == null) {
            contract.updateTotalValue();
        }

        contractRepository.save(contract);
        return map(contract);
    }

    // -------------------- UPDATE --------------------
    public ContractResponse updateContract(Long id, UpdateContractRequest request) {
        Contract contract = fetch(id);
        if (!contract.getStatus().equals(ContractStatus.UNSIGNED)) {
            throw new BusinessException("Chỉ hợp đồng chưa ký mới được sửa");
        }
        contract.setTitle(request.getTitle());
        contract.setDescription(request.getDescription());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        if (request.getFilePath() != null) {
            contract.setFilePath(request.getFilePath());
        }

        // If contract details are provided, replace existing ones
        if (request.getContractDetails() != null) {
            // Remove old details
            if (contract.getContractDetails() != null) {
                contract.getContractDetails().clear();
            }

            List<ContractDetail> newDetails = request.getContractDetails().stream()
                    .map(d -> {
                        ContractDetail detail = ContractDetail.builder()
                                .contract(contract)
                                .workCode(d.getWorkCode())
                                .deviceId(d.getDeviceId())
                                .serviceName(d.getServiceName())
                                .description(d.getDescription())
                                .quantity(d.getQuantity())
                                .unitPrice(d.getUnitPrice())
                                .warrantyMonths(d.getWarrantyMonths())
                                .notes(d.getNotes())
                                .build();
                        detail.calculateTotalPrice();
                        return detail;
                    })
                    .collect(Collectors.toList());
            contract.setContractDetails(newDetails);
        }

        // Recalculate total value
        contract.updateTotalValue();

        // Allow manual override of total value if provided and different
        if (request.getTotalValue() != null) {
            contract.setTotalValue(request.getTotalValue());
        }

        contractRepository.save(contract);
        return map(contract);
    }

    // -------------------- DETAIL --------------------
    @Transactional(readOnly = true)
    public ContractResponse getDetail(Long id) {
        return map(fetch(id));
    }

    // -------------------- LISTS --------------------
    @Transactional(readOnly = true)
    public Page<ContractResponse> listUnsigned(int page, int size) {
        return contractRepository.findByStatusAndIsHiddenFalse(ContractStatus.UNSIGNED, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::map);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> listSigned(int page, int size) {
        return contractRepository.findSignedContracts(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::map);
    }

    @Transactional(readOnly = true)
    public Page<ContractResponse> listHidden(int page, int size) {
        return contractRepository.findByIsHiddenTrue(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::map);
    }

    // -------------------- SIGN --------------------
    public ContractResponse signEContract(Long id, SignContractRequest req) {
        Contract contract = fetch(id);
        if (!contract.getStatus().equals(ContractStatus.UNSIGNED)) {
            throw new BusinessException("Hợp đồng đã ký hoặc bị hủy");
        }
        contract.setSignedAt(LocalDate.now().atStartOfDay());
        contract.setSignedBy(req.getSignerName());
        contract.setStatus(ContractStatus.DIGITALLY_SIGNED);
        contractRepository.save(contract);
        return map(contract);
    }

    public ContractResponse signDigital(Long id, String base64, String signerName) {
        Contract contract = fetch(id);
        if (!contract.getStatus().equals(ContractStatus.UNSIGNED)) {
            throw new BusinessException("Hợp đồng đã ký hoặc bị hủy");
        }
        // Lưu file chữ ký
        String fileName = fileStorageService.storeSignatureFile(base64, contract.getContractNumber(), signerName);
        contract.setSignedAt(LocalDate.now().atStartOfDay());
        contract.setSignedBy(signerName);
        contract.setStatus(ContractStatus.DIGITALLY_SIGNED);
        contractRepository.save(contract);
        return map(contract);
    }

    public ContractResponse signDigitalFile(Long id, MultipartFile file, String signerName, String signerEmail, String jwt) {
        Contract contract = fetch(id);
        if (!contract.getStatus().equals(ContractStatus.UNSIGNED)) {
            throw new BusinessException("Hợp đồng đã ký hoặc bị hủy");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File chữ ký không hợp lệ");
        }
        // Lưu file chữ ký hình ảnh
        String storedName = fileStorageService.storeFile(file, contract.getContractNumber());

        // Cập nhật trạng thái hợp đồng
        contract.setSignedAt(LocalDate.now().atStartOfDay());
        contract.setSignedBy(signerName);
        contract.setStatus(ContractStatus.DIGITALLY_SIGNED);
        contractRepository.save(contract);

        // Tạo bản ghi chữ ký
        ContractSignature signature = ContractSignature.builder()
                .contract(contract)
                .signerName(signerName)
                .signerEmail(signerEmail)
                .signerType(SignerType.CUSTOMER)
                .signatureType(SignatureType.DIGITAL)
                .signatureData(storedName)
                .build();
        contractSignatureRepository.save(signature);

        // Gửi email thông báo + tạo tài khoản nếu cần
        try {
            String tempPass = "";
            boolean exists = externalService.customerAccountExists(signerEmail, jwt);
            if (!exists) {
                tempPass = externalService.generateTempPassword();
                externalService.createCustomerAccount(signerEmail, signerName, tempPass, jwt);
            }
            emailService.sendContractSignedNotification(signerEmail, signerName, contract.getContractNumber(), tempPass);
        } catch (Exception ex) {
            log.warn("Không gửi được email hoặc tạo tài khoản", ex);
        }

        return map(contract);
    }

    // -------------------- HIDE / RESTORE --------------------
    public void hide(Long id) {
        Contract c = fetch(id);
        c.setIsHidden(true);
        contractRepository.save(c);
    }

    public void restore(Long id) {
        Contract c = fetch(id);
        c.setIsHidden(false);
        contractRepository.save(c);
    }

    // -------------------- HELPER --------------------
    private Contract fetch(Long id) {
        return contractRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract", id));
    }

    private ContractResponse map(Contract contract) {
        return modelMapper.map(contract, ContractResponse.class);
    }

    private String generateContractNumber() {
        LocalDate today = LocalDate.now();
        String prefix = "HD" + today.getYear() + String.format("%02d", today.getMonthValue());
        
        Long seq;
        try {
            seq = contractRepository.getNextContractNumber();
        } catch (Exception e) {
            // Fallback if sequence doesn't exist
            log.warn("Failed to get next contract number from sequence, using fallback method", e);
            Long maxId = contractRepository.findAll(PageRequest.of(0, 1, Sort.by("id").descending()))
                .stream()
                .findFirst()
                .map(Contract::getId)
                .orElse(1000L);
            seq = maxId + 1;
        }
        
        return prefix + String.format("%04d", seq);
    }
} 