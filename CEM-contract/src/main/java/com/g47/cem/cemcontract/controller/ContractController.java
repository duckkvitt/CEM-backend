package com.g47.cem.cemcontract.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemcontract.dto.request.CreateContractRequest;
import com.g47.cem.cemcontract.dto.request.SignContractRequest;
import com.g47.cem.cemcontract.dto.request.UpdateContractRequest;
import com.g47.cem.cemcontract.dto.response.ApiResponse;
import com.g47.cem.cemcontract.dto.response.ContractResponse;
import com.g47.cem.cemcontract.service.ContractService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

/**
 * REST controller for contract management operations
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contract Management", description = "APIs for managing contracts")
public class ContractController {
    
    private final ContractService contractService;
    
    // Create contract
    @PostMapping
    @Operation(summary = "Create contract")
    public ResponseEntity<ApiResponse<ContractResponse>> create(
            @Valid @RequestBody CreateContractRequest req,
            @RequestHeader("Authorization") String jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contractService.createContract(req, jwt)));
    }

    // Update contract
    @PutMapping("/{id}")
    @Operation(summary = "Update contract (only unsigned)")
    public ResponseEntity<ApiResponse<ContractResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateContractRequest req) {
        return ResponseEntity.ok(ApiResponse.success(contractService.updateContract(id, req)));
    }

    // Detail
    @GetMapping("/{id}")
    @Operation(summary = "Contract detail")
    public ResponseEntity<ApiResponse<ContractResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contractService.getDetail(id)));
    }

    // List unsigned
    @GetMapping("/unsigned")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> unsignedList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.listUnsigned(page, size)));
    }

    // List signed
    @GetMapping("/signed")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> signedList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.listSigned(page, size)));
    }

    // List hidden
    @GetMapping("/hidden")
    public ResponseEntity<ApiResponse<Page<ContractResponse>>> hiddenList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(contractService.listHidden(page, size)));
    }

    // Sign with e-signature
    @PostMapping("/{id}/sign")
    public ResponseEntity<ApiResponse<ContractResponse>> signE(@PathVariable Long id,
            @RequestBody SignContractRequest req) {
        return ResponseEntity.ok(ApiResponse.success(contractService.signEContract(id, req)));
    }

    // Hide contract
    @PostMapping("/{id}/hide")
    public ResponseEntity<ApiResponse<String>> hide(@PathVariable Long id) {
        contractService.hide(id);
        return ResponseEntity.ok(ApiResponse.success("Hidden"));
    }

    // Restore contract
    @PostMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<String>> restore(@PathVariable Long id) {
        contractService.restore(id);
        return ResponseEntity.ok(ApiResponse.success("Restored"));
    }
} 