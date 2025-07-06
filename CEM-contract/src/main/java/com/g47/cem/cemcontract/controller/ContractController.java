package com.g47.cem.cemcontract.controller;

import com.g47.cem.cemcontract.dto.request.CreateContractRequest;
import com.g47.cem.cemcontract.dto.request.UpdateContractRequest;
import com.g47.cem.cemcontract.dto.request.UpdateContractStatusRequest;
import com.g47.cem.cemcontract.dto.response.ContractResponseDto;
import com.g47.cem.cemcontract.service.ContractService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.g47.cem.cemcontract.dto.response.ApiResponse;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "Contract Management", description = "APIs for managing contracts")
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);
    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ApiResponse<ContractResponseDto>> createContract(@Valid @RequestBody CreateContractRequest requestDto, Authentication authentication) {
        ContractResponseDto createdContract = contractService.createContract(requestDto, authentication);
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<ApiResponse<Object>> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.ok(ApiResponse.success("Contract deleted"));
    }
} 