package com.g47.cem.cemdevice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.g47.cem.cemdevice.entity.DeviceImportRequest;
import com.g47.cem.cemdevice.entity.DeviceInventory;
import com.g47.cem.cemdevice.entity.DeviceInventoryTransaction;
import com.g47.cem.cemdevice.enums.ImportRequestStatus;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.enums.InventoryReferenceType;
import com.g47.cem.cemdevice.service.DeviceImportRequestService;
import com.g47.cem.cemdevice.service.DeviceInventoryService;
import com.g47.cem.cemdevice.service.DeviceInventoryTransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for device warehouse management
 */
@RestController
@RequestMapping(value = {"/warehouse", "/device/warehouse", "/api/device/warehouse"})
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "https://cem.vercel.app"})
public class DeviceWarehouseController {

    private final DeviceInventoryService inventoryService;
    private final DeviceImportRequestService importRequestService;
    private final DeviceInventoryTransactionService transactionService;

    // === INVENTORY MANAGEMENT ===

    /**
     * Get inventory for a specific device
     */
    @GetMapping("/inventory/device/{deviceId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<DeviceInventory> getDeviceInventory(@PathVariable Long deviceId) {
        return inventoryService.getInventoryByDeviceId(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search device inventory
     */
    @GetMapping("/inventory/search")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<Page<DeviceInventory>> searchInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) Boolean outOfStock,
            Pageable pageable) {
        Page<DeviceInventory> inventory = inventoryService.searchInventory(keyword, lowStock, outOfStock, pageable);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get inventory statistics
     */
    @GetMapping("/inventory/statistics")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<DeviceInventoryService.InventoryStatistics> getInventoryStatistics() {
        DeviceInventoryService.InventoryStatistics stats = inventoryService.getInventoryStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get low stock items
     */
    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<List<DeviceInventory>> getLowStockItems() {
        List<DeviceInventory> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }

    /**
     * Get out of stock items
     */
    @GetMapping("/inventory/out-of-stock")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<List<DeviceInventory>> getOutOfStockItems() {
        List<DeviceInventory> outOfStockItems = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(outOfStockItems);
    }

    /**
     * Adjust inventory manually
     */
    @PostMapping("/inventory/adjust")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<DeviceInventory> adjustInventory(
            @RequestBody AdjustInventoryRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        DeviceInventory adjusted = inventoryService.adjustStock(
                request.getDeviceId(),
                request.getNewQuantity(),
                InventoryReferenceType.ADJUSTMENT,
                null, // referenceId is null for manual adjustment
                request.getReason(),
                username
        );
        return ResponseEntity.ok(adjusted);
    }

    /**
     * Update inventory settings
     */
    @PutMapping("/inventory/{deviceId}/settings")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<DeviceInventory> updateInventorySettings(
            @PathVariable Long deviceId,
            @RequestBody UpdateInventorySettingsRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        DeviceInventory updated = inventoryService.updateInventorySettings(
                deviceId,
                request.getMinimumStockLevel(),
                request.getMaximumStockLevel(),
                username
        );
        return ResponseEntity.ok(updated);
    }

    // === IMPORT REQUEST MANAGEMENT ===

    /**
     * Create import request
     */
    @PostMapping("/import-requests")
    @PreAuthorize("hasAuthority('STAFF')")
    public ResponseEntity<ImportRequestResponse> createImportRequest(
            @RequestBody DeviceImportRequestService.CreateImportRequestDto request,
            Authentication authentication) {
        String username = authentication.getName();
        DeviceImportRequest created = importRequestService.createImportRequest(request, username);
        return ResponseEntity.ok(mapImportRequest(created));
    }

    /**
     * Get import request by ID
     */
    @GetMapping("/import-requests/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ImportRequestResponse> getImportRequest(@PathVariable Long id) {
        return importRequestService.getImportRequestById(id)
                .map(this::mapImportRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search import requests
     */
    @GetMapping("/import-requests/search")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<Page<ImportRequestResponse>> searchImportRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ImportRequestStatus status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String requestedBy,
            Pageable pageable) {
        Page<DeviceImportRequest> requests = importRequestService.searchImportRequests(
                keyword, status, supplierId, requestedBy, pageable);
        java.util.List<ImportRequestResponse> content = requests.getContent().stream()
                .map(this::mapImportRequest)
                .toList();
        PageImpl<ImportRequestResponse> page = new PageImpl<>(content, pageable, requests.getTotalElements());
        return ResponseEntity.ok(page);
    }

    /**
     * Get pending requests for review
     */
    @GetMapping("/import-requests/pending")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<List<ImportRequestResponse>> getPendingRequests() {
        List<DeviceImportRequest> pendingRequests = importRequestService.getPendingRequestsForReview();
        List<ImportRequestResponse> dto = pendingRequests.stream().map(this::mapImportRequest).toList();
        return ResponseEntity.ok(dto);
    }

    /**
     * Approve import request
     */
    @PostMapping("/import-requests/{id}/approve")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<ImportRequestResponse> approveImportRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        DeviceImportRequest approved = importRequestService.approveImportRequest(
                id, request.getReason(), username);
        return ResponseEntity.ok(mapImportRequest(approved));
    }

    /**
     * Reject import request
     */
    @PostMapping("/import-requests/{id}/reject")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<ImportRequestResponse> rejectImportRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        DeviceImportRequest rejected = importRequestService.rejectImportRequest(
                id, request.getReason(), username);
        return ResponseEntity.ok(mapImportRequest(rejected));
    }

    /**
     * Complete import request
     */
    @PostMapping("/import-requests/{id}/complete")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<ImportRequestResponse> completeImportRequest(
            @PathVariable Long id,
            @RequestBody DeviceImportRequestService.CompleteImportRequestDto request,
            Authentication authentication) {
        String username = authentication.getName();
        DeviceImportRequest completed = importRequestService.completeImportRequest(id, request, username);
        return ResponseEntity.ok(mapImportRequest(completed));
    }

    /**
     * Get import request statistics
     */
    @GetMapping("/import-requests/statistics")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<DeviceImportRequestService.ImportRequestStatistics> getImportRequestStatistics() {
        DeviceImportRequestService.ImportRequestStatistics stats = importRequestService.getImportRequestStatistics();
        return ResponseEntity.ok(stats);
    }

    // === TRANSACTION MANAGEMENT ===

    /**
     * Get transactions for a device
     */
    @GetMapping("/transactions/device/{deviceId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<List<DeviceInventoryTransaction>> getDeviceTransactions(@PathVariable Long deviceId) {
        List<DeviceInventoryTransaction> transactions = transactionService.getTransactionsByDeviceId(deviceId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Search transactions
     */
    @GetMapping("/transactions/search")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<Page<DeviceInventoryTransaction>> searchTransactions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) InventoryTransactionType transactionType,
            @RequestParam(required = false) InventoryReferenceType referenceType,
            @RequestParam(required = false) Long deviceId,
            Pageable pageable) {
        Page<DeviceInventoryTransaction> transactions = transactionService.searchTransactions(
                keyword, transactionType, referenceType, deviceId, pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get recent transactions
     */
    @GetMapping("/transactions/recent")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<Page<DeviceInventoryTransaction>> getRecentTransactions(Pageable pageable) {
        Page<DeviceInventoryTransaction> transactions = transactionService.getRecentTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transaction statistics
     */
    @GetMapping("/transactions/statistics")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<DeviceInventoryTransactionService.TransactionStatistics> getTransactionStatistics() {
        DeviceInventoryTransactionService.TransactionStatistics stats = transactionService.getTransactionStatistics();
        return ResponseEntity.ok(stats);
    }

    // === DTOs ===

    @lombok.Data
    public static class AdjustInventoryRequest {
        private Long deviceId;
        private Integer newQuantity;
        private String reason;
    }

    @lombok.Data
    public static class UpdateInventorySettingsRequest {
        private Integer minimumStockLevel;
        private Integer maximumStockLevel;
    }

    @lombok.Data
    public static class ApprovalRequest {
        private String reason;
    }

    // === DTOs and Mappers for Import Requests ===

    @lombok.Data
    @lombok.Builder
    public static class ImportRequestResponse {
        private Long id;
        private String requestNumber;
        private DeviceItem device;
        private Long supplierId;
        private Integer requestedQuantity;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal totalAmount;
        private String requestStatus;
        private String approvalStatus;
        private String requestReason;
        private String requestedBy;
        private java.time.LocalDateTime requestedAt;
        private String reviewedBy;
        private java.time.LocalDateTime reviewedAt;
        private String approvalReason;
        private java.time.LocalDate expectedDeliveryDate;
        private java.time.LocalDate actualDeliveryDate;
        private String invoiceNumber;
        private String notes;

        @lombok.Data
        @lombok.Builder
        public static class DeviceItem {
            private Long id;
            private String name;
            private String model;
            private String serialNumber;
        }
    }

    private ImportRequestResponse mapImportRequest(DeviceImportRequest entity) {
        ImportRequestResponse.DeviceItem device = null;
        if (entity.getDevice() != null) {
            device = ImportRequestResponse.DeviceItem.builder()
                    .id(entity.getDevice().getId())
                    .name(entity.getDevice().getName())
                    .model(entity.getDevice().getModel())
                    .serialNumber(entity.getDevice().getSerialNumber())
                    .build();
        }

        return ImportRequestResponse.builder()
                .id(entity.getId())
                .requestNumber(entity.getRequestNumber())
                .device(device)
                .supplierId(entity.getSupplierId())
                .requestedQuantity(entity.getRequestedQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalAmount(entity.getTotalAmount())
                .requestStatus(String.valueOf(entity.getRequestStatus()))
                .approvalStatus(entity.getApprovalStatus() != null ? String.valueOf(entity.getApprovalStatus()) : null)
                .requestReason(entity.getRequestReason())
                .requestedBy(entity.getRequestedBy())
                .requestedAt(entity.getRequestedAt())
                .reviewedBy(entity.getReviewedBy())
                .reviewedAt(entity.getReviewedAt())
                .approvalReason(entity.getApprovalReason())
                .expectedDeliveryDate(entity.getExpectedDeliveryDate())
                .actualDeliveryDate(entity.getActualDeliveryDate())
                .invoiceNumber(entity.getInvoiceNumber())
                .notes(entity.getNotes())
                .build();
    }
}
