package com.g47.cem.cemspareparts.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.SparePartsExportRequest;
import com.g47.cem.cemspareparts.enums.ExportRequestStatus;
import com.g47.cem.cemspareparts.repository.SparePartRepository;
import com.g47.cem.cemspareparts.repository.SparePartsExportRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing spare parts export requests (technician requests)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartsExportRequestService {

    private final SparePartsExportRequestRepository exportRequestRepository;
    private final SparePartRepository sparePartRepository;
    private final SparePartsInventoryService inventoryService;
    private final SparePartsInventoryTransactionService transactionService;

    /**
     * Create a new export request
     */
    public SparePartsExportRequest createExportRequest(CreateExportRequestDto dto, String requestedBy) {
        SparePart sparePart = sparePartRepository.findById(dto.getSparePartId())
                .orElseThrow(() -> new RuntimeException("Spare part not found with id: " + dto.getSparePartId()));

        // Check current stock
        Integer availableStock = inventoryService.getAvailableStock(dto.getSparePartId());
        if (availableStock < dto.getRequestedQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + availableStock + ", Requested: " + dto.getRequestedQuantity());
        }

        String requestNumber = generateRequestNumber();

        SparePartsExportRequest request = SparePartsExportRequest.builder()
                .requestNumber(requestNumber)
                .sparePart(sparePart)
                .taskId(dto.getTaskId())
                .requestedQuantity(dto.getRequestedQuantity())
                .requestReason(dto.getRequestReason())
                .requestedBy(requestedBy)
                .build();

        SparePartsExportRequest saved = exportRequestRepository.save(request);
        log.info("Created spare parts export request {} for spare part {} by {}", 
                requestNumber, sparePart.getId(), requestedBy);
        return saved;
    }

    /**
     * Get export request by ID
     */
    @Transactional(readOnly = true)
    public Optional<SparePartsExportRequest> getExportRequestById(Long id) {
        return exportRequestRepository.findByIdWithSparePart(id);
    }

    /**
     * Get export request by request number
     */
    @Transactional(readOnly = true)
    public Optional<SparePartsExportRequest> getExportRequestByNumber(String requestNumber) {
        return exportRequestRepository.findByRequestNumber(requestNumber);
    }

    /**
     * Approve export request
     */
    public SparePartsExportRequest approveExportRequest(Long requestId, String approvalReason, String reviewedBy) {
        SparePartsExportRequest request = exportRequestRepository.findByIdWithSparePart(requestId)
                .orElseThrow(() -> new RuntimeException("Export request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new RuntimeException("Request is not in pending status");
        }

        // Check stock availability again
        Integer availableStock = inventoryService.getAvailableStock(request.getSparePart().getId());
        if (availableStock < request.getRequestedQuantity()) {
            throw new RuntimeException("Insufficient stock for approval. Available: " + availableStock + ", Requested: " + request.getRequestedQuantity());
        }

        request.approve(reviewedBy, approvalReason);
        SparePartsExportRequest saved = exportRequestRepository.save(request);
        
        log.info("Approved spare parts export request {} by {}", request.getRequestNumber(), reviewedBy);
        return saved;
    }

    /**
     * Reject export request
     */
    public SparePartsExportRequest rejectExportRequest(Long requestId, String rejectionReason, String reviewedBy) {
        SparePartsExportRequest request = exportRequestRepository.findByIdWithSparePart(requestId)
                .orElseThrow(() -> new RuntimeException("Export request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new RuntimeException("Request is not in pending status");
        }

        request.reject(reviewedBy, rejectionReason);
        SparePartsExportRequest saved = exportRequestRepository.save(request);
        
        log.info("Rejected spare parts export request {} by {}: {}", 
                request.getRequestNumber(), reviewedBy, rejectionReason);
        return saved;
    }

    /**
     * Issue spare parts (complete the export request)
     */
    public SparePartsExportRequest issueExportRequest(Long requestId, IssueExportRequestDto dto, String issuedBy) {
        SparePartsExportRequest request = exportRequestRepository.findByIdWithSparePart(requestId)
                .orElseThrow(() -> new RuntimeException("Export request not found with id: " + requestId));

        if (!request.isApproved()) {
            throw new RuntimeException("Request is not approved");
        }

        Integer issuedQuantity = dto.getIssuedQuantity() != null ? dto.getIssuedQuantity() : request.getRequestedQuantity();

        // Remove stock from inventory
        boolean stockRemoved = inventoryService.removeStock(
                request.getSparePart().getId(),
                issuedQuantity,
                issuedBy
        );

        if (!stockRemoved) {
            throw new RuntimeException("Failed to remove stock from inventory");
        }

        request.issue(issuedQuantity, issuedBy);
        // Record export transaction
        var inventory = inventoryService.getOrCreateInventory(request.getSparePart().getId());
        Integer beforeQty = inventory.getQuantityInStock() + issuedQuantity; // before removal
        transactionService.createExportTransaction(
                request.getSparePart(), issuedQuantity, beforeQty,
                request.getId(), "Export request issued: " + request.getRequestNumber(), issuedBy
        );
        if (dto.getNotes() != null) {
            request.setNotes(dto.getNotes());
        }

        SparePartsExportRequest saved = exportRequestRepository.save(request);
        log.info("Issued spare parts export request {} - {} units to technician {}", 
                request.getRequestNumber(), issuedQuantity, request.getRequestedBy());
        return saved;
    }

    /**
     * Cancel export request
     */
    public SparePartsExportRequest cancelExportRequest(Long requestId, String reason, String cancelledBy) {
        SparePartsExportRequest request = exportRequestRepository.findByIdWithSparePart(requestId)
                .orElseThrow(() -> new RuntimeException("Export request not found with id: " + requestId));

        if (request.isIssued()) {
            throw new RuntimeException("Cannot cancel issued request");
        }

        request.cancel();
        if (reason != null) {
            request.setNotes(reason);
        }

        SparePartsExportRequest saved = exportRequestRepository.save(request);
        log.info("Cancelled spare parts export request {} by {}: {}", 
                request.getRequestNumber(), cancelledBy, reason);
        return saved;
    }

    /**
     * Get pending requests for review
     */
    @Transactional(readOnly = true)
    public List<SparePartsExportRequest> getPendingRequestsForReview() {
        return exportRequestRepository.findPendingRequestsForReview();
    }

    /**
     * Get approved requests ready for issuing
     */
    @Transactional(readOnly = true)
    public List<SparePartsExportRequest> getApprovedRequestsForIssuing() {
        return exportRequestRepository.findApprovedRequestsForIssuing();
    }

    /**
     * Search export requests
     */
    @Transactional(readOnly = true)
    public Page<SparePartsExportRequest> searchExportRequests(
            String keyword, ExportRequestStatus status, Long sparePartId, Long taskId, String requestedBy, Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank()) ? null : ("%" + keyword.toLowerCase() + "%");
        String requestedByPattern = (requestedBy == null || requestedBy.isBlank()) ? null : ("%" + requestedBy.toLowerCase() + "%");
        return exportRequestRepository.searchExportRequests(keywordPattern, status, sparePartId, taskId, requestedByPattern, pageable);
    }

    /**
     * Get requests by technician
     */
    @Transactional(readOnly = true)
    public Page<SparePartsExportRequest> getRequestsByTechnician(String requestedBy, Pageable pageable) {
        return exportRequestRepository.findByRequestedByOrderByRequestedAtDesc(requestedBy, pageable);
    }

    /**
     * Get export request statistics
     */
    @Transactional(readOnly = true)
    public ExportRequestStatistics getExportRequestStatistics() {
        Object[] stats = exportRequestRepository.getExportRequestStatistics();
        if (stats != null && stats.length >= 6) {
            return ExportRequestStatistics.builder()
                    .totalRequests(((Number) stats[0]).longValue())
                    .pendingCount(((Number) stats[1]).longValue())
                    .approvedCount(((Number) stats[2]).longValue())
                    .issuedCount(((Number) stats[3]).longValue())
                    .rejectedCount(((Number) stats[4]).longValue())
                    .totalIssued(((Number) stats[5]).longValue())
                    .build();
        }
        return ExportRequestStatistics.builder().build();
    }

    /**
     * Generate unique request number
     */
    private String generateRequestNumber() {
        return "SPER-" + System.currentTimeMillis();
    }

    /**
     * DTO for creating export request
     */
    @lombok.Data
    @lombok.Builder
    public static class CreateExportRequestDto {
        private Long sparePartId;
        private Long taskId;
        private Integer requestedQuantity;
        private String requestReason;
    }

    /**
     * DTO for issuing export request
     */
    @lombok.Data
    @lombok.Builder
    public static class IssueExportRequestDto {
        private Integer issuedQuantity;
        private String notes;
    }

    /**
     * DTO for export request statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class ExportRequestStatistics {
        private Long totalRequests;
        private Long pendingCount;
        private Long approvedCount;
        private Long issuedCount;
        private Long rejectedCount;
        private Long totalIssued;
    }
}
