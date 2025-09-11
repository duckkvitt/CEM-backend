package com.g47.cem.cemdevice.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.CustomerDeviceResponse;
import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;
import com.g47.cem.cemdevice.service.CustomerDeviceService;
import com.g47.cem.cemdevice.service.CustomerDeviceService.CustomerDeviceStatistics;
import com.g47.cem.cemdevice.service.ExternalCustomerService;
import com.g47.cem.cemdevice.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Customer Device operations
 */
@RestController
    @RequestMapping("/customer-devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Device Management", description = "APIs for customers to view their purchased devices")
public class CustomerDeviceController {
    
    private final CustomerDeviceService customerDeviceService;
    private final JwtUtil jwtUtil;
    private final ExternalCustomerService externalCustomerService;
    
    /**
     * Staff: List devices of a specific customer by ID
     */
    @GetMapping("/staff")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM','LEAD_TECH','MANAGER','ADMIN')")
    @Operation(summary = "List customer devices by customerId (staff)")
    public ResponseEntity<ApiResponse<Page<CustomerDeviceResponse>>> listCustomerDevicesForStaff(
            @RequestParam Long customerId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<CustomerDeviceResponse> page = customerDeviceService.getCustomerDevicesForStaff(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    /**
     * Get customer's purchased devices with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get customer's purchased devices", description = "Retrieves paginated list of customer's purchased devices with optional filtering")
    public ResponseEntity<ApiResponse<Page<CustomerDeviceResponse>>> getCustomerPurchasedDevices(
            @Parameter(description = "Search keyword (matches device name, model, serial number)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Device status filter") @RequestParam(required = false) CustomerDeviceStatus status,
            @Parameter(description = "Filter by expired warranty") @RequestParam(required = false) Boolean warrantyExpired,
            @Parameter(description = "Filter by contract ID") @RequestParam(required = false) Long contractId,
            @PageableDefault(size = 20) Pageable pageable,
            Principal principal,
            HttpServletRequest request) {
        
        log.debug("Customer {} fetching purchased devices with filters - keyword: {}, status: {}, warrantyExpired: {}", 
                principal.getName(), keyword, status, warrantyExpired);
        
        // Extract customer ID from user email
        Long customerId = extractCustomerIdFromUserEmail(principal.getName());
        
        Page<CustomerDeviceResponse> devices = customerDeviceService.getCustomerPurchasedDevices(
                customerId, keyword, status, warrantyExpired, contractId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(devices, "Purchased devices retrieved successfully"));
    }
    
    /**
     * Get customer device by ID
     */
    @GetMapping("/{deviceId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get customer device by ID", description = "Retrieves specific customer device details by ID")
    public ResponseEntity<ApiResponse<CustomerDeviceResponse>> getCustomerDeviceById(
            @Parameter(description = "Customer Device ID") @PathVariable Long deviceId,
            Principal principal,
            HttpServletRequest request) {
        
        log.debug("Customer {} fetching device with ID: {}", principal.getName(), deviceId);
        
        Long customerId = extractCustomerIdFromUserEmail(principal.getName());
        
        CustomerDeviceResponse device = customerDeviceService.getCustomerDeviceById(customerId, deviceId);
        
        return ResponseEntity.ok(ApiResponse.success(device, "Device details retrieved successfully"));
    }
    
    /**
     * Get customer device statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get customer device statistics", description = "Retrieves statistics about customer's devices")
    public ResponseEntity<ApiResponse<CustomerDeviceStatistics>> getCustomerDeviceStatistics(
            Principal principal,
            HttpServletRequest request) {
        
        log.debug("Customer {} fetching device statistics", principal.getName());
        
        Long customerId = extractCustomerIdFromUserEmail(principal.getName());
        
        CustomerDeviceStatistics statistics = customerDeviceService.getCustomerDeviceStatistics(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(statistics, "Device statistics retrieved successfully"));
    }
    
    /**
     * Get devices with expiring warranty
     */
    @GetMapping("/expiring-warranty")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Get devices with expiring warranty", description = "Retrieves devices with warranty expiring within 30 days")
    public ResponseEntity<ApiResponse<List<CustomerDeviceResponse>>> getDevicesWithExpiringWarranty(
            Principal principal,
            HttpServletRequest request) {
        
        log.debug("Customer {} fetching devices with expiring warranty", principal.getName());
        
        Long customerId = extractCustomerIdFromUserEmail(principal.getName());
        
        List<CustomerDeviceResponse> devices = customerDeviceService.getDevicesWithExpiringWarranty(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(devices, "Devices with expiring warranty retrieved successfully"));
    }
    
    /**
     * Extract customer ID from user email by calling customer service
     */
    private Long extractCustomerIdFromUserEmail(String userEmail) {
        try {
            // Call customer service to get customer info by email
            var customerInfo = externalCustomerService.getCustomerByEmail(userEmail);
            if (customerInfo != null && customerInfo.getId() != null) {
                log.debug("Found customer ID {} for user email {}", customerInfo.getId(), userEmail);
                return customerInfo.getId();
            } else {
                log.error("Could not find customer info for user email: {}", userEmail);
                throw new RuntimeException("Customer not found for user email: " + userEmail);
            }
        } catch (Exception e) {
            log.error("Error extracting customer ID from user email {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Failed to get customer info for user email: " + userEmail);
        }
    }
} 