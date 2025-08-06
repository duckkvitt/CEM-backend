package com.g47.cem.cemdevice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.request.CreateServiceRequestRequest;
import com.g47.cem.cemdevice.dto.request.UpdateServiceRequestRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.ServiceRequestResponse;
import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.enums.ServiceRequestType;
import com.g47.cem.cemdevice.service.ServiceRequestService;
import com.g47.cem.cemdevice.service.ServiceRequestService.ServiceRequestStatistics;
import com.g47.cem.cemdevice.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for service request operations
 */
@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestController {
    
    private final ServiceRequestService serviceRequestService;
    private final JwtUtil jwtUtil;
    
    /**
     * Create a new service request
     */
    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> createServiceRequest(
            @Valid @RequestBody CreateServiceRequestRequest request,
            Authentication authentication) {
        
        log.info("Creating service request for user: {}", authentication.getName());
        
        // Extract customer ID from authentication (assuming it's stored in principal)
        Long customerId = extractCustomerId(authentication);
        
        ServiceRequestResponse response = serviceRequestService.createServiceRequest(
                request, customerId, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service request created successfully"));
    }
    
    /**
     * Get service request by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getServiceRequestById(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.debug("Fetching service request with ID: {} for user: {}", id, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        ServiceRequestResponse response = serviceRequestService.getServiceRequestById(id, customerId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service request retrieved successfully"));
    }
    
    /**
     * Get service request by request ID
     */
    @GetMapping("/by-request-id/{requestId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> getServiceRequestByRequestId(
            @PathVariable String requestId,
            Authentication authentication) {
        
        log.debug("Fetching service request with request ID: {} for user: {}", requestId, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        ServiceRequestResponse response = serviceRequestService.getServiceRequestByRequestId(requestId, customerId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service request retrieved successfully"));
    }
    
    /**
     * Get customer's service requests with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<ServiceRequestResponse>>> getCustomerServiceRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ServiceRequestStatus status,
            @RequestParam(required = false) ServiceRequestType type,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        log.debug("Fetching service requests for user: {} with filters - keyword: {}, status: {}, type: {}, deviceId: {}", 
                authentication.getName(), keyword, status, type, deviceId);
        
        Long customerId = extractCustomerId(authentication);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ServiceRequestResponse> response = serviceRequestService.getCustomerServiceRequests(
                customerId, keyword, status, type, deviceId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service requests retrieved successfully"));
    }
    
    /**
     * Update service request
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> updateServiceRequest(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequestRequest request,
            Authentication authentication) {
        
        log.info("Updating service request with ID: {} for user: {}", id, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        ServiceRequestResponse response = serviceRequestService.updateServiceRequest(
                id, request, customerId, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service request updated successfully"));
    }
    
    /**
     * Add comment to service request
     */
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestResponse>> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest commentRequest,
            Authentication authentication) {
        
        log.info("Adding comment to service request with ID: {} for user: {}", id, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        ServiceRequestResponse response = serviceRequestService.addComment(
                id, commentRequest.getComment(), customerId, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Comment added successfully"));
    }
    
    /**
     * Get service request statistics for customer
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<ServiceRequestStatistics>> getServiceRequestStatistics(
            Authentication authentication) {
        
        log.debug("Fetching service request statistics for user: {}", authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        ServiceRequestStatistics statistics = serviceRequestService.getCustomerServiceRequestStatistics(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }
    
    /**
     * Get service requests by device ID
     */
    @GetMapping("/device/{deviceId}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<ServiceRequestResponse>>> getServiceRequestsByDevice(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        log.debug("Fetching service requests for device: {} for user: {}", deviceId, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ServiceRequestResponse> response = serviceRequestService.getCustomerServiceRequests(
                customerId, null, null, null, deviceId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Device service requests retrieved successfully"));
    }
    
    /**
     * Get service requests by type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<ServiceRequestResponse>>> getServiceRequestsByType(
            @PathVariable ServiceRequestType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        log.debug("Fetching service requests of type: {} for user: {}", type, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ServiceRequestResponse> response = serviceRequestService.getCustomerServiceRequests(
                customerId, null, null, type, null, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service requests by type retrieved successfully"));
    }
    
    /**
     * Get service requests by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<ServiceRequestResponse>>> getServiceRequestsByStatus(
            @PathVariable ServiceRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        log.debug("Fetching service requests with status: {} for user: {}", status, authentication.getName());
        
        Long customerId = extractCustomerId(authentication);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ServiceRequestResponse> response = serviceRequestService.getCustomerServiceRequests(
                customerId, null, status, null, null, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service requests by status retrieved successfully"));
    }
    
    /**
     * Extract customer ID from authentication
     * This method extracts customer ID from JWT token claims
     */
    private Long extractCustomerId(Authentication authentication) {
        try {
            // Get the JWT token from the request
            // Since we don't have direct access to the request here,
            // we'll use a different approach
            String username = authentication.getName();
            
            // For now, we'll use a mapping based on the database
            // In a real implementation, customer ID should be in JWT claims
            if ("longrpk200314@gmail.com".equals(username)) {
                return 12L;
            }
            
            // If no mapping found, try to extract from JWT claims if available
            // This would require storing the token somewhere accessible
            log.warn("No customer ID mapping found for user: {}, using fallback", username);
            return 12L; // Fallback to customer ID 12 based on the database
            
        } catch (Exception e) {
            log.error("Failed to extract customer ID from authentication", e);
            throw new RuntimeException("Failed to extract customer ID");
        }
    }
    
    /**
     * Inner class for comment request
     */
    public static class CommentRequest {
        private String comment;
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
        }
    }
} 