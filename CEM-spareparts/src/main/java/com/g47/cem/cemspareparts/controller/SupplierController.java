package com.g47.cem.cemspareparts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemspareparts.dto.request.CreateSupplierRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSupplierRequest;
import com.g47.cem.cemspareparts.dto.response.ApiResponse;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SupplierResponse;
import com.g47.cem.cemspareparts.enums.SupplierStatus;
import com.g47.cem.cemspareparts.service.SupplierService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier Management", description = "APIs for managing suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Supplier created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplierById(@PathVariable Long id) {
        SupplierResponse response = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all suppliers with filtering and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<SupplierResponse>>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SupplierStatus status) {
        PagedResponse<SupplierResponse> response = supplierService.getAllSuppliers(page, size, sortBy, sortDir, keyword, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(@PathVariable Long id, @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier updated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a supplier")
    public ResponseEntity<ApiResponse<String>> deactivateSupplier(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deactivated successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a supplier")
    public ResponseEntity<ApiResponse<String>> activateSupplier(@PathVariable Long id) {
        supplierService.activateSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier activated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a supplier")
    public ResponseEntity<ApiResponse<String>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully"));
    }

    @GetMapping("/by-spare-part/{sparePartId}")
    @Operation(summary = "Get suppliers by spare part ID")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getSuppliersBySparePartId(
            @PathVariable Long sparePartId) {
        List<SupplierResponse> response = supplierService.getSuppliersBySparePartId(sparePartId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}