package com.g47.cem.cemdevice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g47.cem.cemdevice.dto.request.CreateServiceRequestRequest;
import com.g47.cem.cemdevice.dto.request.UpdateServiceRequestRequest;
import com.g47.cem.cemdevice.dto.response.ServiceRequestHistoryResponse;
import com.g47.cem.cemdevice.dto.response.ServiceRequestResponse;
import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.entity.ServiceRequest;
import com.g47.cem.cemdevice.entity.ServiceRequestHistory;
import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.enums.ServiceRequestType;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.repository.CustomerDeviceRepository;
import com.g47.cem.cemdevice.repository.ServiceRequestHistoryRepository;
import com.g47.cem.cemdevice.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for ServiceRequest operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceRequestService {
    
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestHistoryRepository serviceRequestHistoryRepository;
    private final CustomerDeviceRepository customerDeviceRepository;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * Create a new service request
     */
    @Transactional
    public ServiceRequestResponse createServiceRequest(CreateServiceRequestRequest request, Long customerId, String createdBy) {
        log.debug("Creating service request for customer: {} with device: {}", customerId, request.getDeviceId());
        
        // Verify the device belongs to the customer
        CustomerDevice customerDevice = customerDeviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("CustomerDevice", "id", request.getDeviceId()));
        
        if (!customerDevice.getCustomerId().equals(customerId)) {
            throw new BusinessException("Device does not belong to the customer");
        }
        
        // Generate unique request ID
        String requestId = generateRequestId(request.getType());
        
        // Convert attachments list to JSON string
        String attachmentsJson = null;
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            try {
                attachmentsJson = objectMapper.writeValueAsString(request.getAttachments());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize attachments", e);
                throw new BusinessException("Failed to process attachments");
            }
        }
        
        ServiceRequest serviceRequest = ServiceRequest.builder()
                .requestId(requestId)
                .customerId(customerId)
                .device(customerDevice)
                .type(request.getType())
                .status(ServiceRequestStatus.PENDING)
                .description(request.getDescription())
                .preferredDateTime(request.getPreferredDateTime())
                .attachments(attachmentsJson)
                .customerComments(request.getCustomerComments())
                .createdBy(createdBy)
                .build();
        
        serviceRequest = serviceRequestRepository.save(serviceRequest);
        
        // Create initial history entry
        ServiceRequestHistory history = ServiceRequestHistory.builder()
                .serviceRequest(serviceRequest)
                .status(ServiceRequestStatus.PENDING)
                .comment("Service request created")
                .updatedBy(createdBy)
                .build();
        
        serviceRequestHistoryRepository.save(history);
        
        log.info("Service request created successfully with ID: {}", serviceRequest.getId());
        return mapToServiceRequestResponse(serviceRequest);
    }
    
    /**
     * Get service request by ID
     */
    @Transactional(readOnly = true)
    public ServiceRequestResponse getServiceRequestById(Long id, Long customerId) {
        log.debug("Fetching service request with ID: {} for customer: {}", id, customerId);
        
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", id));
        
        // Verify the service request belongs to the customer
        if (!serviceRequest.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("ServiceRequest", "id", id);
        }
        
        return mapToServiceRequestResponse(serviceRequest);
    }
    
    /**
     * Get service request by request ID
     */
    @Transactional(readOnly = true)
    public ServiceRequestResponse getServiceRequestByRequestId(String requestId, Long customerId) {
        log.debug("Fetching service request with request ID: {} for customer: {}", requestId, customerId);
        
        ServiceRequest serviceRequest = serviceRequestRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "requestId", requestId));
        
        // Verify the service request belongs to the customer
        if (!serviceRequest.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("ServiceRequest", "requestId", requestId);
        }
        
        return mapToServiceRequestResponse(serviceRequest);
    }
    
    /**
     * Get customer's service requests with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<ServiceRequestResponse> getCustomerServiceRequests(
            Long customerId,
            String keyword,
            ServiceRequestStatus status,
            ServiceRequestType type,
            Long deviceId,
            Pageable pageable) {
        log.debug("Fetching service requests for customer: {} with filters - keyword: {}, status: {}, type: {}, deviceId: {}", 
                customerId, keyword, status, type, deviceId);
        
        Page<ServiceRequest> serviceRequests;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            serviceRequests = serviceRequestRepository.findByCustomerIdAndKeyword(customerId, keyword.trim(), pageable);
        } else if (status != null && type != null && deviceId != null) {
            // For complex filtering, we need to fetch and filter manually
            Page<ServiceRequest> statusFiltered = serviceRequestRepository.findByCustomerIdAndStatus(customerId, status, pageable);
            List<ServiceRequest> filteredList = statusFiltered.getContent().stream()
                    .filter(sr -> sr.getType() == type && sr.getDevice().getId().equals(deviceId))
                    .collect(Collectors.toList());
            
            // Create a new Page with filtered content
            serviceRequests = new PageImpl<>(filteredList, pageable, filteredList.size());
        } else if (status != null && type != null) {
            // For complex filtering, we need to fetch and filter manually
            Page<ServiceRequest> statusFiltered = serviceRequestRepository.findByCustomerIdAndStatus(customerId, status, pageable);
            List<ServiceRequest> filteredList = statusFiltered.getContent().stream()
                    .filter(sr -> sr.getType() == type)
                    .collect(Collectors.toList());
            
            // Create a new Page with filtered content
            serviceRequests = new PageImpl<>(filteredList, pageable, filteredList.size());
        } else if (status != null && deviceId != null) {
            // For complex filtering, we need to fetch and filter manually
            Page<ServiceRequest> statusFiltered = serviceRequestRepository.findByCustomerIdAndStatus(customerId, status, pageable);
            List<ServiceRequest> filteredList = statusFiltered.getContent().stream()
                    .filter(sr -> sr.getDevice().getId().equals(deviceId))
                    .collect(Collectors.toList());
            
            // Create a new Page with filtered content
            serviceRequests = new PageImpl<>(filteredList, pageable, filteredList.size());
        } else if (type != null && deviceId != null) {
            // For complex filtering, we need to fetch and filter manually
            Page<ServiceRequest> typeFiltered = serviceRequestRepository.findByCustomerIdAndType(customerId, type, pageable);
            List<ServiceRequest> filteredList = typeFiltered.getContent().stream()
                    .filter(sr -> sr.getDevice().getId().equals(deviceId))
                    .collect(Collectors.toList());
            
            // Create a new Page with filtered content
            serviceRequests = new PageImpl<>(filteredList, pageable, filteredList.size());
        } else if (status != null) {
            serviceRequests = serviceRequestRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else if (type != null) {
            serviceRequests = serviceRequestRepository.findByCustomerIdAndType(customerId, type, pageable);
        } else if (deviceId != null) {
            serviceRequests = serviceRequestRepository.findByCustomerIdAndDeviceId(customerId, deviceId, pageable);
        } else {
            serviceRequests = serviceRequestRepository.findByCustomerId(customerId, pageable);
        }
        
        return serviceRequests.map(this::mapToServiceRequestResponse);
    }
    
    /**
     * Update service request
     */
    @Transactional
    public ServiceRequestResponse updateServiceRequest(Long id, UpdateServiceRequestRequest request, Long customerId, String updatedBy) {
        log.debug("Updating service request with ID: {} for customer: {}", id, customerId);
        
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", id));
        
        // Verify the service request belongs to the customer
        if (!serviceRequest.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("ServiceRequest", "id", id);
        }
        
        // Track status change
        ServiceRequestStatus oldStatus = serviceRequest.getStatus();
        
        // Update fields
        if (request.getDescription() != null) {
            serviceRequest.setDescription(request.getDescription());
        }
        if (request.getPreferredDateTime() != null) {
            serviceRequest.setPreferredDateTime(request.getPreferredDateTime());
        }
        if (request.getCustomerComments() != null) {
            serviceRequest.setCustomerComments(request.getCustomerComments());
        }
        if (request.getEstimatedCost() != null) {
            serviceRequest.setEstimatedCost(request.getEstimatedCost());
        }
        if (request.getActualCost() != null) {
            serviceRequest.setActualCost(request.getActualCost());
        }
        if (request.getCompletedAt() != null) {
            serviceRequest.setCompletedAt(request.getCompletedAt());
        }
        
        // Update attachments if provided
        if (request.getAttachments() != null) {
            try {
                String attachmentsJson = objectMapper.writeValueAsString(request.getAttachments());
                serviceRequest.setAttachments(attachmentsJson);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize attachments", e);
                throw new BusinessException("Failed to process attachments");
            }
        }
        
        // Update status if provided (staff only)
        if (request.getStatus() != null && !request.getStatus().equals(oldStatus)) {
            serviceRequest.setStatus(request.getStatus());
            
            // Create history entry for status change
            ServiceRequestHistory history = ServiceRequestHistory.builder()
                    .serviceRequest(serviceRequest)
                    .status(request.getStatus())
                    .comment(request.getStaffNotes() != null ? request.getStaffNotes() : "Status updated to " + request.getStatus())
                    .updatedBy(updatedBy)
                    .build();
            
            serviceRequestHistoryRepository.save(history);
        }
        
        // Update staff notes if provided
        if (request.getStaffNotes() != null) {
            serviceRequest.setStaffNotes(request.getStaffNotes());
        }
        
        serviceRequest = serviceRequestRepository.save(serviceRequest);
        
        log.info("Service request updated successfully with ID: {}", serviceRequest.getId());
        return mapToServiceRequestResponse(serviceRequest);
    }
    
    /**
     * Add comment to service request
     */
    @Transactional
    public ServiceRequestResponse addComment(Long id, String comment, Long customerId, String updatedBy) {
        log.debug("Adding comment to service request with ID: {} for customer: {}", id, customerId);
        
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", id));
        
        // Verify the service request belongs to the customer
        if (!serviceRequest.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("ServiceRequest", "id", id);
        }
        
        // Create history entry for comment
        ServiceRequestHistory history = ServiceRequestHistory.builder()
                .serviceRequest(serviceRequest)
                .status(serviceRequest.getStatus())
                .comment(comment)
                .updatedBy(updatedBy)
                .build();
        
        serviceRequestHistoryRepository.save(history);
        
        log.info("Comment added to service request with ID: {}", serviceRequest.getId());
        return mapToServiceRequestResponse(serviceRequest);
    }
    
    /**
     * Get service request statistics for customer
     */
    @Transactional(readOnly = true)
    public ServiceRequestStatistics getCustomerServiceRequestStatistics(Long customerId) {
        log.debug("Fetching service request statistics for customer: {}", customerId);
        
        long totalRequests = serviceRequestRepository.countByCustomerId(customerId);
        long pendingRequests = serviceRequestRepository.countByCustomerIdAndStatus(customerId, ServiceRequestStatus.PENDING);
        long approvedRequests = serviceRequestRepository.countByCustomerIdAndStatus(customerId, ServiceRequestStatus.APPROVED);
        long inProgressRequests = serviceRequestRepository.countByCustomerIdAndStatus(customerId, ServiceRequestStatus.IN_PROGRESS);
        long completedRequests = serviceRequestRepository.countByCustomerIdAndStatus(customerId, ServiceRequestStatus.COMPLETED);
        long maintenanceRequests = serviceRequestRepository.countByCustomerIdAndType(customerId, ServiceRequestType.MAINTENANCE);
        long warrantyRequests = serviceRequestRepository.countByCustomerIdAndType(customerId, ServiceRequestType.WARRANTY);
        
        return ServiceRequestStatistics.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .approvedRequests(approvedRequests)
                .inProgressRequests(inProgressRequests)
                .completedRequests(completedRequests)
                .maintenanceRequests(maintenanceRequests)
                .warrantyRequests(warrantyRequests)
                .build();
    }
    
    /**
     * Generate unique request ID
     */
    private String generateRequestId(ServiceRequestType type) {
        String prefix = type == ServiceRequestType.MAINTENANCE ? "MNT" : "WAR";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + timestamp + random;
    }
    
    /**
     * Map ServiceRequest entity to ServiceRequestResponse DTO
     */
    private ServiceRequestResponse mapToServiceRequestResponse(ServiceRequest serviceRequest) {
        ServiceRequestResponse response = new ServiceRequestResponse();
        
        // Map basic fields
        response.setId(serviceRequest.getId());
        response.setRequestId(serviceRequest.getRequestId());
        response.setCustomerId(serviceRequest.getCustomerId());
        response.setType(serviceRequest.getType());
        response.setStatus(serviceRequest.getStatus());
        response.setDescription(serviceRequest.getDescription());
        response.setPreferredDateTime(serviceRequest.getPreferredDateTime());
        response.setStaffNotes(serviceRequest.getStaffNotes());
        response.setCustomerComments(serviceRequest.getCustomerComments());
        response.setEstimatedCost(serviceRequest.getEstimatedCost());
        response.setActualCost(serviceRequest.getActualCost());
        response.setCompletedAt(serviceRequest.getCompletedAt());
        response.setCreatedBy(serviceRequest.getCreatedBy());
        response.setCreatedAt(serviceRequest.getCreatedAt());
        response.setUpdatedAt(serviceRequest.getUpdatedAt());
        
        // Map device information
        if (serviceRequest.getDevice() != null && serviceRequest.getDevice().getDevice() != null) {
            response.setDeviceId(serviceRequest.getDevice().getId());
            response.setDeviceName(serviceRequest.getDevice().getDevice().getName());
            response.setDeviceModel(serviceRequest.getDevice().getDevice().getModel());
            response.setSerialNumber(serviceRequest.getDevice().getDevice().getSerialNumber());
        }
        
        // Map attachments
        if (serviceRequest.getAttachments() != null) {
            try {
                List<String> attachments = objectMapper.readValue(serviceRequest.getAttachments(), new TypeReference<List<String>>() {});
                response.setAttachments(attachments);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize attachments", e);
                response.setAttachments(List.of());
            }
        }
        
        // Map history
        if (serviceRequest.getHistory() != null) {
            List<ServiceRequestHistoryResponse> historyResponses = serviceRequest.getHistory().stream()
                    .map(this::mapToServiceRequestHistoryResponse)
                    .collect(Collectors.toList());
            response.setHistory(historyResponses);
        }
        
        return response;
    }
    
    /**
     * Map ServiceRequestHistory entity to ServiceRequestHistoryResponse DTO
     */
    private ServiceRequestHistoryResponse mapToServiceRequestHistoryResponse(ServiceRequestHistory history) {
        return ServiceRequestHistoryResponse.builder()
                .id(history.getId())
                .status(history.getStatus())
                .comment(history.getComment())
                .updatedBy(history.getUpdatedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
    
    /**
     * Statistics class for service requests
     */
    public static class ServiceRequestStatistics {
        private long totalRequests;
        private long pendingRequests;
        private long approvedRequests;
        private long inProgressRequests;
        private long completedRequests;
        private long maintenanceRequests;
        private long warrantyRequests;
        
        // Builder pattern
        public static ServiceRequestStatisticsBuilder builder() {
            return new ServiceRequestStatisticsBuilder();
        }
        
        public static class ServiceRequestStatisticsBuilder {
            private ServiceRequestStatistics statistics = new ServiceRequestStatistics();
            
            public ServiceRequestStatisticsBuilder totalRequests(long totalRequests) {
                statistics.totalRequests = totalRequests;
                return this;
            }
            
            public ServiceRequestStatisticsBuilder pendingRequests(long pendingRequests) {
                statistics.pendingRequests = pendingRequests;
                return this;
            }
            
            public ServiceRequestStatisticsBuilder approvedRequests(long approvedRequests) {
                statistics.approvedRequests = approvedRequests;
                return this;
            }
            
            public ServiceRequestStatisticsBuilder inProgressRequests(long inProgressRequests) {
                statistics.inProgressRequests = inProgressRequests;
                return this;
            }
            
            public ServiceRequestStatisticsBuilder completedRequests(long completedRequests) {
                statistics.completedRequests = completedRequests;
                return this;
            }
            
            public ServiceRequestStatisticsBuilder maintenanceRequests(long maintenanceRequests) {
                statistics.maintenanceRequests = maintenanceRequests;
                return this;
            }
            
            public ServiceRequestStatisticsBuilder warrantyRequests(long warrantyRequests) {
                statistics.warrantyRequests = warrantyRequests;
                return this;
            }
            
            public ServiceRequestStatistics build() {
                return statistics;
            }
        }
        
        // Getters
        public long getTotalRequests() { return totalRequests; }
        public long getPendingRequests() { return pendingRequests; }
        public long getApprovedRequests() { return approvedRequests; }
        public long getInProgressRequests() { return inProgressRequests; }
        public long getCompletedRequests() { return completedRequests; }
        public long getMaintenanceRequests() { return maintenanceRequests; }
        public long getWarrantyRequests() { return warrantyRequests; }
    }
} 