package com.g47.cem.cemdevice.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.request.CreateDeviceRequest;
import com.g47.cem.cemdevice.dto.request.UpdateDeviceRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.DeviceResponse;
import com.g47.cem.cemdevice.enums.DeviceStatus;
import com.g47.cem.cemdevice.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Device operations
 */
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Management", description = "APIs for managing devices")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    /**
     * Create a new device (Staff only)
     */
    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Create a new device", description = "Creates a new device entry (Staff access required)")
    public ResponseEntity<ApiResponse<DeviceResponse>> createDevice(
            @Valid @RequestBody CreateDeviceRequest request,
            Principal principal) {
        
        log.info("Creating device: {} by user: {}", request.getName(), principal.getName());
        
        DeviceResponse response = deviceService.createDevice(request, principal.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Device created successfully"));
    }
    
    /**
     * Get device by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER') or hasRole('SUPPORT_TEAM') or hasRole('TECH_LEAD') or hasRole('TECHNICIAN')")
    @Operation(summary = "Get device by ID", description = "Retrieves device details by ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        
        log.debug("Fetching device with ID: {}", id);
        
        DeviceResponse response = deviceService.getDeviceById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all devices with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasRole('STAFF') or hasRole('MANAGER') or hasRole('SUPPORT_TEAM') or hasRole('TECH_LEAD') or hasRole('TECHNICIAN')")
    @Operation(summary = "Get all devices", description = "Retrieves paginated list of devices with optional filtering")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getAllDevices(
            @Parameter(description = "Device name filter") @RequestParam(required = false) String name,
            @Parameter(description = "Device model filter") @RequestParam(required = false) String model,
            @Parameter(description = "Serial number filter") @RequestParam(required = false) String serialNumber,
            @Parameter(description = "Customer ID filter") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Device status filter") @RequestParam(required = false) DeviceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Fetching devices with filters - name: {}, model: {}, serialNumber: {}, customerId: {}, status: {}", 
                name, model, serialNumber, customerId, status);
        
        Page<DeviceResponse> devices;
        
        // If any filter is provided, use search; otherwise get all
        if (name != null || model != null || serialNumber != null || customerId != null || status != null) {
            devices = deviceService.searchDevices(name, model, serialNumber, customerId, status, pageable);
        } else {
            devices = deviceService.getAllDevices(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(devices, "Devices retrieved successfully"));
    }
    
    /**
     * Update device (Staff only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Update device", description = "Updates device information (Staff access required)")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @Parameter(description = "Device ID") @PathVariable Long id,
            @Valid @RequestBody UpdateDeviceRequest request,
            Principal principal) {
        
        log.info("Updating device ID: {} by user: {}", id, principal.getName());
        
        DeviceResponse response = deviceService.updateDevice(id, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Device updated successfully"));
    }
    
    /**
     * Delete device (Staff only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Delete device", description = "Deletes a device (Staff access required)")
    public ResponseEntity<ApiResponse<String>> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable Long id,
            Principal principal) {
        
        log.info("Deleting device ID: {} by user: {}", id, principal.getName());
        
        deviceService.deleteDevice(id);
        
        return ResponseEntity.ok(ApiResponse.success("Device deleted successfully"));
    }
} 