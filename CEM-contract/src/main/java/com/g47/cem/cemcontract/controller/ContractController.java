package com.g47.cem.cemcontract.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemcontract.dto.request.CreateContractRequest;
import com.g47.cem.cemcontract.dto.request.UpdateContractRequest;
import com.g47.cem.cemcontract.dto.request.UpdateContractStatusRequest;
import com.g47.cem.cemcontract.dto.response.ApiResponse;
import com.g47.cem.cemcontract.dto.response.ContractResponseDto;
import com.g47.cem.cemcontract.service.ContractService;
import com.g47.cem.cemcontract.service.GoogleDriveService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Contract Management", description = "APIs for managing contracts")
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);
    private final ContractService contractService;
    private final GoogleDriveService googleDriveService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> createContract(
            @Valid @RequestBody CreateContractRequest requestDto, 
            Authentication authentication,
            HttpServletRequest request) {
        ContractResponseDto createdContract = contractService.createContract(requestDto, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdContract));
    }

    @GetMapping
    // Allow additional internal roles such as SUPPORT_TEAM, TECH_LEAD and TECHNICIAN to access the contract list as read-only
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF', 'CUSTOMER', 'SUPPORT_TEAM', 'TECH_LEAD', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<ContractResponseDto>>> getContractsForUser(Authentication authentication) {
        List<ContractResponseDto> contracts = contractService.getContractsForUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(contracts));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> getContractById(@PathVariable Long id) {
        ContractResponseDto dto = contractService.getContractById(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> updateContract(@PathVariable Long id, @Valid @RequestBody UpdateContractRequest requestDto) {
        ContractResponseDto dto = contractService.updateContract(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> updateContractStatus(@PathVariable Long id, @RequestBody UpdateContractStatusRequest requestDto, Principal principal) {
         ContractResponseDto dto = contractService.updateContractStatus(id, requestDto.getStatus(), principal.getName());
         return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/unsigned")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<ContractResponseDto>>> getUnsignedContracts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContractResponseDto> page = contractService.getUnsignedContracts(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/hidden")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<ContractResponseDto>>> getHiddenContracts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContractResponseDto> page = contractService.getHiddenContracts(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @GetMapping("/signed")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<Page<ContractResponseDto>>> getSignedContracts(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContractResponseDto> page = contractService.getSignedContracts(pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success("Contract deleted"));
    }

    @PostMapping("/{id}/hide")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> hideContract(@PathVariable Long id, Authentication authentication) {
        ContractResponseDto dto = contractService.hideContract(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> restoreContract(@PathVariable Long id, Authentication authentication) {
        ContractResponseDto dto = contractService.restoreContract(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/{id}/signatures")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Object>> addSignature(
            @PathVariable Long id, 
            @Valid @RequestBody com.g47.cem.cemcontract.dto.request.SignatureRequestDto requestDto) {
        try {
            contractService.addSignatureToContract(id, requestDto);
            return ResponseEntity.ok(ApiResponse.success("Signature added successfully"));
        } catch (Exception e) {
            logger.error("Failed to add signature to contract {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<byte[]> getContractFile(@PathVariable Long id) {
        try {
            ContractResponseDto contract = contractService.getContractById(id);
            
            if (contract.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            // Download file content from Google Drive
            byte[] fileContent = googleDriveService.downloadFileContent(contract.getFilePath());
            
            // Get file metadata for proper filename
            com.google.api.services.drive.model.File fileMetadata = googleDriveService.getFileMetadata(contract.getFilePath());
            String filename = fileMetadata.getName() != null ? fileMetadata.getName() : "contract_" + id + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", filename);
            headers.setCacheControl("max-age=3600"); // Cache for 1 hour

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
                    
        } catch (Exception e) {
            logger.error("Failed to serve contract file for contract {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 