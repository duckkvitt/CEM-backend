package com.g47.cem.cemdevice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.DeviceImportRequest;
import com.g47.cem.cemdevice.enums.ImportRequestStatus;
import com.g47.cem.cemdevice.repository.DeviceImportRequestRepository;
import com.g47.cem.cemdevice.repository.DeviceRepository;
import com.g47.cem.cemdevice.enums.InventoryReferenceType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing device import requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceImportRequestService {

    private final DeviceImportRequestRepository importRequestRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceInventoryService inventoryService;

    /**
     * Create a new import request
     */
    public DeviceImportRequest createImportRequest(CreateImportRequestDto dto, String requestedBy) {
        Device device = deviceRepository.findById(dto.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + dto.getDeviceId()));

        String requestNumber = generateRequestNumber();
        BigDecimal totalAmount = dto.getUnitPrice() != null && dto.getRequestedQuantity() != null
                ? dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getRequestedQuantity()))
                : null;

        if (dto.getUnitPrice() != null) {
            // Clamp to 2 decimals and validate precision (<= 13 digits before decimal)
            dto.setUnitPrice(dto.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP));
            if (dto.getUnitPrice().abs().compareTo(new BigDecimal("9999999999999.99")) > 0) {
                throw new RuntimeException("Unit price is too large for storage precision");
            }
        }
        if (totalAmount != null) {
            totalAmount = totalAmount.setScale(2, java.math.RoundingMode.HALF_UP);
            if (totalAmount.abs().compareTo(new BigDecimal("9999999999999.99")) > 0) {
                throw new RuntimeException("Total amount is too large for storage precision");
            }
        }

        DeviceImportRequest request = DeviceImportRequest.builder()
                .requestNumber(requestNumber)
                .device(device)
                .supplierId(dto.getSupplierId())
                .requestedQuantity(dto.getRequestedQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalAmount(totalAmount)
                .requestReason(dto.getRequestReason())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .requestedBy(requestedBy)
                .build();

        DeviceImportRequest saved = importRequestRepository.save(request);
        log.info("Created device import request {} for device {} by {}", 
                requestNumber, device.getId(), requestedBy);
        return saved;
    }

    /**
     * Get import request by ID
     */
    @Transactional(readOnly = true)
    public Optional<DeviceImportRequest> getImportRequestById(Long id) {
        return importRequestRepository.findByIdWithDevice(id);
    }

    /**
     * Get import request by request number
     */
    @Transactional(readOnly = true)
    public Optional<DeviceImportRequest> getImportRequestByNumber(String requestNumber) {
        return importRequestRepository.findByRequestNumber(requestNumber);
    }

    /**
     * Approve import request
     */
    public DeviceImportRequest approveImportRequest(Long requestId, String approvalReason, String reviewedBy) {
        DeviceImportRequest request = importRequestRepository.findByIdWithDevice(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new RuntimeException("Request is not in pending status");
        }

        request.approve(reviewedBy, approvalReason);
        DeviceImportRequest saved = importRequestRepository.save(request);

        // Business rule: On approval, immediately increase inventory for devices
        inventoryService.addStock(
                request.getDevice().getId(),
                request.getRequestedQuantity(),
                InventoryReferenceType.IMPORT_REQUEST,
                request.getId(),
                "Import request approved: " + request.getRequestNumber(),
                reviewedBy
        );
        
        log.info("Approved device import request {} by {}", request.getRequestNumber(), reviewedBy);
        return saved;
    }

    /**
     * Reject import request
     */
    public DeviceImportRequest rejectImportRequest(Long requestId, String rejectionReason, String reviewedBy) {
        DeviceImportRequest request = importRequestRepository.findByIdWithDevice(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new RuntimeException("Request is not in pending status");
        }

        request.reject(reviewedBy, rejectionReason);
        DeviceImportRequest saved = importRequestRepository.save(request);
        
        log.info("Rejected device import request {} by {}: {}", 
                request.getRequestNumber(), reviewedBy, rejectionReason);
        return saved;
    }

    /**
     * Complete import request (when goods are received)
     */
    public DeviceImportRequest completeImportRequest(Long requestId, CompleteImportRequestDto dto, String completedBy) {
        DeviceImportRequest request = importRequestRepository.findByIdWithDevice(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (!request.isApproved()) {
            throw new RuntimeException("Request is not approved");
        }

        request.complete(dto.getActualDeliveryDate(), dto.getInvoiceNumber());
        if (dto.getNotes() != null) {
            request.setNotes(dto.getNotes());
        }

        DeviceImportRequest saved = importRequestRepository.save(request);

        // Stock was already added on approval; do not add again on completion

        log.info("Completed device import request {} and added {} units to inventory", 
                request.getRequestNumber(), request.getRequestedQuantity());
        return saved;
    }

    /**
     * Cancel import request
     */
    public DeviceImportRequest cancelImportRequest(Long requestId, String reason, String cancelledBy) {
        DeviceImportRequest request = importRequestRepository.findByIdWithDevice(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (request.isCompleted()) {
            throw new RuntimeException("Cannot cancel completed request");
        }

        request.cancel();
        if (reason != null) {
            request.setNotes(reason);
        }

        DeviceImportRequest saved = importRequestRepository.save(request);
        log.info("Cancelled device import request {} by {}: {}", 
                request.getRequestNumber(), cancelledBy, reason);
        return saved;
    }

    /**
     * Get pending requests for review
     */
    @Transactional(readOnly = true)
    public List<DeviceImportRequest> getPendingRequestsForReview() {
        return importRequestRepository.findPendingRequestsForReview();
    }

    /**
     * Get approved requests ready for completion
     */
    @Transactional(readOnly = true)
    public List<DeviceImportRequest> getApprovedRequestsForCompletion() {
        return importRequestRepository.findApprovedRequestsReadyForCompletion();
    }

    /**
     * Search import requests
     */
    @Transactional(readOnly = true)
    public Page<DeviceImportRequest> searchImportRequests(
            String keyword, ImportRequestStatus status, Long supplierId, String requestedBy, Pageable pageable) {
        String keywordPattern = keyword == null || keyword.isBlank() ? null : ("%" + keyword.toLowerCase() + "%");
        String requestedByPattern = requestedBy == null || requestedBy.isBlank() ? null : ("%" + requestedBy.toLowerCase() + "%");
        return importRequestRepository.searchImportRequests(keywordPattern, status, supplierId, requestedByPattern, pageable);
    }

    /**
     * Get requests by user
     */
    @Transactional(readOnly = true)
    public Page<DeviceImportRequest> getRequestsByUser(String requestedBy, Pageable pageable) {
        return importRequestRepository.findByRequestedByOrderByRequestedAtDesc(requestedBy, pageable);
    }

    /**
     * Get import request statistics
     */
    @Transactional(readOnly = true)
    public ImportRequestStatistics getImportRequestStatistics() {
        Object[] stats = importRequestRepository.getImportRequestStatistics();
        if (stats != null && stats.length >= 6) {
            return ImportRequestStatistics.builder()
                    .totalRequests(((Number) stats[0]).longValue())
                    .pendingCount(((Number) stats[1]).longValue())
                    .approvedCount(((Number) stats[2]).longValue())
                    .completedCount(((Number) stats[3]).longValue())
                    .rejectedCount(((Number) stats[4]).longValue())
                    .totalValue(stats[5] != null ? ((Number) stats[5]).doubleValue() : 0.0)
                    .build();
        }
        return ImportRequestStatistics.builder().build();
    }

    /**
     * Generate unique request number
     */
    private String generateRequestNumber() {
        return "DIR-" + System.currentTimeMillis();
    }

    /**
     * DTO for creating import request
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateImportRequestDto {
        private Long deviceId;
        private Long supplierId;
        private Integer requestedQuantity;
        private BigDecimal unitPrice;
        private String requestReason;
        private LocalDate expectedDeliveryDate;
    }

    /**
     * DTO for completing import request
     */
    @lombok.Data
    @lombok.Builder
    public static class CompleteImportRequestDto {
        private LocalDate actualDeliveryDate;
        private String invoiceNumber;
        private String notes;
    }

    /**
     * DTO for import request statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class ImportRequestStatistics {
        private Long totalRequests;
        private Long pendingCount;
        private Long approvedCount;
        private Long completedCount;
        private Long rejectedCount;
        private Double totalValue;
    }
}
