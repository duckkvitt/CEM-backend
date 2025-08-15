package com.g47.cem.cemspareparts.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.SparePartsImportRequest;
import com.g47.cem.cemspareparts.entity.Supplier;
import com.g47.cem.cemspareparts.enums.ImportRequestStatus;
import com.g47.cem.cemspareparts.repository.SparePartRepository;
import com.g47.cem.cemspareparts.repository.SparePartsImportRequestRepository;
import com.g47.cem.cemspareparts.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing spare parts import requests
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartsImportRequestService {

    private final SparePartsImportRequestRepository importRequestRepository;
    private final SparePartRepository sparePartRepository;
    private final SupplierRepository supplierRepository;
    private final SparePartsInventoryService inventoryService;
    private final SparePartsInventoryTransactionService transactionService;

    /**
     * Create a new import request
     */
    public SparePartsImportRequest createImportRequest(CreateImportRequestDto dto, String requestedBy) {
        SparePart sparePart = sparePartRepository.findById(dto.getSparePartId())
                .orElseThrow(() -> new RuntimeException("Spare part not found with id: " + dto.getSparePartId()));

        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplierId()));
        }

        String requestNumber = generateRequestNumber();
        BigDecimal totalAmount = dto.getUnitPrice() != null && dto.getRequestedQuantity() != null
                ? dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getRequestedQuantity()))
                : null;

        SparePartsImportRequest request = SparePartsImportRequest.builder()
                .requestNumber(requestNumber)
                .sparePart(sparePart)
                .supplier(supplier)
                .requestedQuantity(dto.getRequestedQuantity())
                .unitPrice(dto.getUnitPrice())
                .totalAmount(totalAmount)
                .requestReason(dto.getRequestReason())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .requestedBy(requestedBy)
                .build();

        SparePartsImportRequest saved = importRequestRepository.save(request);
        log.info("Created spare parts import request {} for spare part {} by {}", 
                requestNumber, sparePart.getId(), requestedBy);
        return saved;
    }

    /**
     * Get import request by ID
     */
    @Transactional(readOnly = true)
    public Optional<SparePartsImportRequest> getImportRequestById(Long id) {
        return importRequestRepository.findByIdWithDetails(id);
    }

    /**
     * Get import request by request number
     */
    @Transactional(readOnly = true)
    public Optional<SparePartsImportRequest> getImportRequestByNumber(String requestNumber) {
        return importRequestRepository.findByRequestNumber(requestNumber);
    }

    /**
     * Approve import request
     */
    public SparePartsImportRequest approveImportRequest(Long requestId, String approvalReason, String reviewedBy) {
        SparePartsImportRequest request = importRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new RuntimeException("Request is not in pending status");
        }

	        request.approve(reviewedBy, approvalReason);
	        SparePartsImportRequest saved = importRequestRepository.save(request);
	        
	        // Business rule: On approval, immediately increase inventory for spare parts (align with device flow)
	        var inventory = inventoryService.getOrCreateInventory(request.getSparePart().getId());
	        Integer beforeQty = inventory.getQuantityInStock();
	        inventoryService.addStock(request.getSparePart().getId(), request.getRequestedQuantity(), reviewedBy);
	        transactionService.createImportTransaction(
	                request.getSparePart(), request.getRequestedQuantity(), beforeQty,
	                request.getId(), "Import request approved: " + request.getRequestNumber(), reviewedBy
	        );
	        
	        log.info("Approved spare parts import request {} by {} and added {} units to inventory", 
	                request.getRequestNumber(), reviewedBy, request.getRequestedQuantity());
	        return saved;
    }

    /**
     * Reject import request
     */
    public SparePartsImportRequest rejectImportRequest(Long requestId, String rejectionReason, String reviewedBy) {
        SparePartsImportRequest request = importRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new RuntimeException("Request is not in pending status");
        }

        request.reject(reviewedBy, rejectionReason);
        SparePartsImportRequest saved = importRequestRepository.save(request);
        
        log.info("Rejected spare parts import request {} by {}: {}", 
                request.getRequestNumber(), reviewedBy, rejectionReason);
        return saved;
    }

    /**
     * Complete import request (when goods are received)
     */
    public SparePartsImportRequest completeImportRequest(Long requestId, CompleteImportRequestDto dto, String completedBy) {
        SparePartsImportRequest request = importRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (!request.isApproved()) {
            throw new RuntimeException("Request is not approved");
        }

        request.complete(dto.getActualDeliveryDate(), dto.getInvoiceNumber());
        if (dto.getNotes() != null) {
            request.setNotes(dto.getNotes());
        }

	        SparePartsImportRequest saved = importRequestRepository.save(request);

	        // Stock was already added on approval; do not add again on completion
	        log.info("Completed spare parts import request {} (stock already added on approval)", 
	                request.getRequestNumber());
        return saved;
    }

    /**
     * Cancel import request
     */
    public SparePartsImportRequest cancelImportRequest(Long requestId, String reason, String cancelledBy) {
        SparePartsImportRequest request = importRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new RuntimeException("Import request not found with id: " + requestId));

        if (request.isCompleted()) {
            throw new RuntimeException("Cannot cancel completed request");
        }

        request.cancel();
        if (reason != null) {
            request.setNotes(reason);
        }

        SparePartsImportRequest saved = importRequestRepository.save(request);
        log.info("Cancelled spare parts import request {} by {}: {}", 
                request.getRequestNumber(), cancelledBy, reason);
        return saved;
    }

    /**
     * Get pending requests for review
     */
    @Transactional(readOnly = true)
    public List<SparePartsImportRequest> getPendingRequestsForReview() {
        return importRequestRepository.findPendingRequestsForReview();
    }

    /**
     * Get approved requests ready for completion
     */
    @Transactional(readOnly = true)
    public List<SparePartsImportRequest> getApprovedRequestsForCompletion() {
        return importRequestRepository.findApprovedRequestsReadyForCompletion();
    }

    /**
     * Search import requests
     */
    @Transactional(readOnly = true)
    public Page<SparePartsImportRequest> searchImportRequests(
            String keyword, ImportRequestStatus status, Long supplierId, Long sparePartId, String requestedBy, Pageable pageable) {
        String keywordPattern = keyword == null || keyword.isBlank() ? null : ("%" + keyword.toLowerCase() + "%");
        String requestedByPattern = requestedBy == null || requestedBy.isBlank() ? null : ("%" + requestedBy.toLowerCase() + "%");
        return importRequestRepository.searchImportRequests(keywordPattern, status, supplierId, sparePartId, requestedByPattern, pageable);
    }

    /**
     * Get requests by user
     */
    @Transactional(readOnly = true)
    public Page<SparePartsImportRequest> getRequestsByUser(String requestedBy, Pageable pageable) {
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
        return "SPIR-" + System.currentTimeMillis();
    }

    /**
     * DTO for creating import request
     */
    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateImportRequestDto {
        private Long sparePartId;
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
    @NoArgsConstructor
    @AllArgsConstructor
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
