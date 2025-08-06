package com.g47.cem.cemdevice.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

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
import com.g47.cem.cemdevice.dto.request.LinkDevicesRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.DeviceResponse;
import com.g47.cem.cemdevice.enums.DeviceStatus;
import com.g47.cem.cemdevice.service.DeviceService;
import com.g47.cem.cemdevice.service.ContractDeviceLinkService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.annotation.Secured;

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
    private final ContractDeviceLinkService contractDeviceLinkService;
    
    /**
     * Create a new device (Staff only)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('STAFF')")
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
    @Secured({"STAFF", "MANAGER", "SUPPORT_TEAM", "TECH_LEAD", "TECHNICIAN", "CUSTOMER"})
    @Operation(summary = "Get device by ID", description = "Retrieves device details by ID")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        
        log.debug("Fetching device with ID: {}", id);
        log.debug("Current user authorities: {}", 
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        
        DeviceResponse response = deviceService.getDeviceById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get device by ID (Customer access - limited information)
     */
    @GetMapping("/{id}/info")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get device info by ID", description = "Retrieves basic device information for customers")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceInfoById(
            @Parameter(description = "Device ID") @PathVariable Long id) {
        
        log.debug("Customer fetching device info with ID: {}", id);
        
        DeviceResponse response = deviceService.getDeviceById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all devices with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('STAFF', 'MANAGER', 'SUPPORT_TEAM', 'TECH_LEAD', 'TECHNICIAN')")
    @Operation(summary = "Get all devices", description = "Retrieves paginated list of devices with optional filtering")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getAllDevices(
            @Parameter(description = "Search keyword (matches name, model, serial number)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Set to true to only get devices in stock (not assigned to any customer)") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Device status filter") @RequestParam(required = false) DeviceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Fetching devices with filters - keyword: {}, inStock: {}, status: {}", keyword, inStock, status);
        
        Page<DeviceResponse> devices;
        
        // Luôn sử dụng hàm search để có thể kết hợp các bộ lọc
        devices = deviceService.searchDevices(keyword, inStock, status, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(devices, "Devices retrieved successfully"));
    }
    
    /**
     * Update device (Staff only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STAFF')")
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
    @PreAuthorize("hasAuthority('STAFF')")
    @Operation(summary = "Delete device", description = "Deletes a device (Staff access required)")
    public ResponseEntity<ApiResponse<String>> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable Long id,
            Principal principal) {
        
        log.info("Deleting device ID: {} by user: {}", id, principal.getName());
        
        deviceService.deleteDevice(id);
        
        return ResponseEntity.ok(ApiResponse.success("Device deleted successfully"));
    }
    
    /**
     * Link devices to customer (called when contract is completed)
     */
    @PostMapping("/link-to-customer")
    @PreAuthorize("hasAnyAuthority('STAFF', 'MANAGER')")
    @Operation(summary = "Link devices to customer", description = "Links devices to customer when contract is completed")
    public ResponseEntity<ApiResponse<String>> linkDevicesToCustomer(
            @Valid @RequestBody LinkDevicesRequest request,
            Principal principal) {
        
        log.info("Linking {} devices to customer {} by user: {}", 
                request.getDevices().size(), request.getCustomerId(), principal.getName());
        
        // Convert request to service format
        List<ContractDeviceLinkService.ContractDeviceInfo> deviceInfos = request.getDevices().stream()
                .map(device -> new ContractDeviceLinkService.ContractDeviceInfo(
                        device.getDeviceId(),
                        device.getQuantity(),
                        device.getWarrantyMonths()))
                .collect(Collectors.toList());
        
        contractDeviceLinkService.linkDevicesFromContract(request.getCustomerId(), deviceInfos);
        
        return ResponseEntity.ok(ApiResponse.success("Devices linked to customer successfully"));
    }
    
    /**
     * Unlink devices from customer (called when contract is cancelled or rejected)
     */
    @PostMapping("/unlink-from-customer")
    @PreAuthorize("hasAnyAuthority('STAFF', 'MANAGER')")
    @Operation(summary = "Unlink devices from customer", description = "Unlinks devices from customer when contract is cancelled or rejected")
    public ResponseEntity<ApiResponse<String>> unlinkDevicesFromCustomer(
            @Valid @RequestBody LinkDevicesRequest request,
            Principal principal) {
        
        log.info("Unlinking {} devices from customer {} by user: {}", 
                request.getDevices().size(), request.getCustomerId(), principal.getName());
        
        // Convert request to service format and unlink each device type
        for (LinkDevicesRequest.DeviceInfo deviceInfo : request.getDevices()) {
            contractDeviceLinkService.unlinkAllDevicesOfTypeFromCustomer(
                    request.getCustomerId(), 
                    deviceInfo.getDeviceId());
        }
        
        return ResponseEntity.ok(ApiResponse.success("Devices unlinked from customer successfully"));
    }
} 