package com.g47.cem.cemspareparts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemspareparts.dto.request.CreateSupplierDeviceTypeRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSupplierDeviceTypeRequest;
import com.g47.cem.cemspareparts.dto.response.ApiResponse;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SupplierDeviceTypeResponse;
import com.g47.cem.cemspareparts.service.SupplierDeviceTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/supplier-device-types")
@RequiredArgsConstructor
@Tag(name = "Supplier Device Type Management", description = "APIs for managing supplier device types")
public class SupplierDeviceTypeController {

    private final SupplierDeviceTypeService supplierDeviceTypeService;

    @PostMapping
    @Operation(summary = "Create a new supplier device type")
    public ResponseEntity<ApiResponse<SupplierDeviceTypeResponse>> createSupplierDeviceType(
            @Valid @RequestBody CreateSupplierDeviceTypeRequest request) {
        SupplierDeviceTypeResponse response = supplierDeviceTypeService.createSupplierDeviceType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Supplier device type created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier device type by ID")
    public ResponseEntity<ApiResponse<SupplierDeviceTypeResponse>> getSupplierDeviceTypeById(@PathVariable Long id) {
        SupplierDeviceTypeResponse response = supplierDeviceTypeService.getSupplierDeviceTypeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all supplier device types with filters")
    public ResponseEntity<ApiResponse<PagedResponse<SupplierDeviceTypeResponse>>> getAllSupplierDeviceTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword) {
        PagedResponse<SupplierDeviceTypeResponse> response = supplierDeviceTypeService
                .getAllSupplierDeviceTypes(page, size, sortBy, sortDir, supplierId, isActive, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing supplier device type")
    public ResponseEntity<ApiResponse<SupplierDeviceTypeResponse>> updateSupplierDeviceType(
            @PathVariable Long id, @Valid @RequestBody UpdateSupplierDeviceTypeRequest request) {
        SupplierDeviceTypeResponse response = supplierDeviceTypeService.updateSupplierDeviceType(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier device type updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a supplier device type")
    public ResponseEntity<ApiResponse<String>> deactivateSupplierDeviceType(@PathVariable Long id) {
        supplierDeviceTypeService.deactivateSupplierDeviceType(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier device type deactivated successfully"));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get active device types for a specific supplier")
    public ResponseEntity<ApiResponse<SupplierDeviceTypeResponse[]>> getActiveDeviceTypesBySupplier(
            @PathVariable Long supplierId) {
        var response = supplierDeviceTypeService.getActiveDeviceTypesBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success(response.toArray(new SupplierDeviceTypeResponse[0])));
    }

    @GetMapping("/device-type/{deviceType}/suppliers")
    @Operation(summary = "Find suppliers for a specific device type")
    public ResponseEntity<ApiResponse<SupplierDeviceTypeResponse[]>> getSuppliersForDeviceType(
            @PathVariable String deviceType,
            @RequestParam(defaultValue = "1") Integer requestedQuantity) {
        var response = supplierDeviceTypeService.getSuppliersForDeviceType(deviceType, requestedQuantity);
        return ResponseEntity.ok(ApiResponse.success(response.toArray(new SupplierDeviceTypeResponse[0])));
    }

    @GetMapping("/device-types")
    @Operation(summary = "Get distinct active device types")
    public ResponseEntity<ApiResponse<String[]>> getDistinctActiveDeviceTypes() {
        var response = supplierDeviceTypeService.getDistinctActiveDeviceTypes();
        return ResponseEntity.ok(ApiResponse.success(response.toArray(new String[0])));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get supplier device type statistics")
    public ResponseEntity<ApiResponse<Object[]>> getSupplierDeviceTypeStatistics() {
        var response = supplierDeviceTypeService.getSupplierDeviceTypeStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}


