package com.g47.cem.cemspareparts.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.g47.cem.cemspareparts.entity.SparePartsExportRequest;
import com.g47.cem.cemspareparts.entity.SparePartsImportRequest;
import com.g47.cem.cemspareparts.entity.SparePartsInventory;
import com.g47.cem.cemspareparts.enums.ExportRequestStatus;
import com.g47.cem.cemspareparts.enums.ImportRequestStatus;
import com.g47.cem.cemspareparts.service.SparePartsExportRequestService;
import com.g47.cem.cemspareparts.service.SparePartsImportRequestService;
import com.g47.cem.cemspareparts.service.SparePartsInventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for spare parts warehouse management
 */
@RestController
@RequestMapping(value = {"/warehouse", "/spare-parts/warehouse", "/api/spare-parts/warehouse"})
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "https://cem.vercel.app"})
public class SparePartsWarehouseController {

    private final SparePartsInventoryService inventoryService;
    private final SparePartsExportRequestService exportRequestService;
    private final SparePartsImportRequestService importRequestService;

    // === INVENTORY MANAGEMENT ===

    /**
     * Get inventory for a specific spare part
     */
    @GetMapping("/inventory/spare-part/{sparePartId}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF', 'TECHNICIAN')")
    public ResponseEntity<SparePartsInventory> getSparePartInventory(@PathVariable Long sparePartId) {
        return inventoryService.getInventoryBySparePartId(sparePartId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search spare parts inventory
     */
    @GetMapping("/inventory/search")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF', 'TECHNICIAN')")
    public ResponseEntity<Page<InventoryItemResponse>> searchInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) Boolean outOfStock,
            Pageable pageable) {
        Page<SparePartsInventory> inventory = inventoryService.searchInventory(keyword, lowStock, outOfStock, pageable);
        java.util.List<InventoryItemResponse> content = inventory.getContent().stream().map(this::mapInventoryItem).toList();
        PageImpl<InventoryItemResponse> page = new PageImpl<>(content, pageable, inventory.getTotalElements());
        return ResponseEntity.ok(page);
    }

    /**
     * Get inventory statistics
     */
    @GetMapping("/inventory/statistics")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<SparePartsInventoryService.InventoryStatistics> getInventoryStatistics() {
        SparePartsInventoryService.InventoryStatistics stats = inventoryService.getInventoryStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get low stock items
     */
    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<List<SparePartsInventoryService.LowStockItemDto>> getLowStockItems() {
        List<SparePartsInventory> lowStockItems = inventoryService.getLowStockItems();
        List<SparePartsInventoryService.LowStockItemDto> dto = lowStockItems.stream().map(inv -> {
            SparePartsInventoryService.LowStockItemDto item = new SparePartsInventoryService.LowStockItemDto();
            item.setInventoryId(inv.getId());
            item.setSparePartId(inv.getSparePart().getId());
            item.setSparePartName(inv.getSparePart().getPartName());
            item.setSparePartCode(inv.getSparePart().getPartCode());
            item.setQuantityInStock(inv.getQuantityInStock());
            item.setMinimumStockLevel(inv.getMinimumStockLevel());
            return item;
        }).toList();
        return ResponseEntity.ok(dto);
    }

    /**
     * Get out of stock items
     */
    @GetMapping("/inventory/out-of-stock")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<List<SparePartsInventoryService.LowStockItemDto>> getOutOfStockItems() {
        List<SparePartsInventory> outOfStockItems = inventoryService.getOutOfStockItems();
        List<SparePartsInventoryService.LowStockItemDto> dto = outOfStockItems.stream().map(inv -> {
            SparePartsInventoryService.LowStockItemDto item = new SparePartsInventoryService.LowStockItemDto();
            item.setInventoryId(inv.getId());
            item.setSparePartId(inv.getSparePart().getId());
            item.setSparePartName(inv.getSparePart().getPartName());
            item.setSparePartCode(inv.getSparePart().getPartCode());
            item.setQuantityInStock(inv.getQuantityInStock());
            item.setMinimumStockLevel(inv.getMinimumStockLevel());
            return item;
        }).toList();
        return ResponseEntity.ok(dto);
    }

    /**
     * Adjust inventory manually
     */
    @PostMapping("/inventory/adjust")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<SparePartsInventory> adjustInventory(
            @RequestBody AdjustInventoryRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SparePartsInventory adjusted = inventoryService.adjustStock(
                request.getSparePartId(),
                request.getNewQuantity(),
                username
        );
        return ResponseEntity.ok(adjusted);
    }

    /**
     * Update inventory settings
     */
    @PutMapping("/inventory/{sparePartId}/settings")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<SparePartsInventory> updateInventorySettings(
            @PathVariable Long sparePartId,
            @RequestBody UpdateInventorySettingsRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SparePartsInventory updated = inventoryService.updateInventorySettings(
                sparePartId,
                request.getMinimumStockLevel(),
                request.getMaximumStockLevel(),
                username
        );
        return ResponseEntity.ok(updated);
    }

    // === EXPORT REQUEST MANAGEMENT (TECHNICIAN REQUESTS) ===

    /**
     * Create export request (used by technicians)
     */
    @PostMapping("/export-requests")
    @PreAuthorize("hasAnyAuthority('TECHNICIAN', 'MANAGER')")
    public ResponseEntity<SparePartsExportRequest> createExportRequest(
            @RequestBody SparePartsExportRequestService.CreateExportRequestDto request,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            SparePartsExportRequest created = exportRequestService.createExportRequest(request, username);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get export request by ID
     */
    @GetMapping("/export-requests/{id}")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'TECHNICIAN')")
    public ResponseEntity<SparePartsExportRequest> getExportRequest(@PathVariable Long id) {
        return exportRequestService.getExportRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search export requests
     */
    @GetMapping("/export-requests/search")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'TECHNICIAN')")
    public ResponseEntity<Page<SparePartsExportRequest>> searchExportRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ExportRequestStatus status,
            @RequestParam(required = false) Long sparePartId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String requestedBy,
            Pageable pageable) {
        Page<SparePartsExportRequest> requests = exportRequestService.searchExportRequests(
                keyword, status, sparePartId, taskId, requestedBy, pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get pending requests for review (manager only)
     */
    @GetMapping("/export-requests/pending")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<List<SparePartsExportRequest>> getPendingRequests() {
        List<SparePartsExportRequest> pendingRequests = exportRequestService.getPendingRequestsForReview();
        return ResponseEntity.ok(pendingRequests);
    }

    /**
     * Get approved requests ready for issuing
     */
    @GetMapping("/export-requests/approved")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<List<SparePartsExportRequest>> getApprovedRequests() {
        List<SparePartsExportRequest> approvedRequests = exportRequestService.getApprovedRequestsForIssuing();
        return ResponseEntity.ok(approvedRequests);
    }

    /**
     * Get technician's own requests
     */
    @GetMapping("/export-requests/my-requests")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    public ResponseEntity<Page<SparePartsExportRequest>> getMyRequests(
            Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<SparePartsExportRequest> myRequests = exportRequestService.getRequestsByTechnician(username, pageable);
        return ResponseEntity.ok(myRequests);
    }

    /**
     * Approve export request (manager only)
     */
    @PostMapping("/export-requests/{id}/approve")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<SparePartsExportRequest> approveExportRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            SparePartsExportRequest approved = exportRequestService.approveExportRequest(
                    id, request.getReason(), username);
            return ResponseEntity.ok(approved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reject export request (manager only)
     */
    @PostMapping("/export-requests/{id}/reject")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<SparePartsExportRequest> rejectExportRequest(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SparePartsExportRequest rejected = exportRequestService.rejectExportRequest(
                id, request.getReason(), username);
        return ResponseEntity.ok(rejected);
    }

    /**
     * Issue spare parts (complete the export request)
     */
    @PostMapping("/export-requests/{id}/issue")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<SparePartsExportRequest> issueExportRequest(
            @PathVariable Long id,
            @RequestBody SparePartsExportRequestService.IssueExportRequestDto request,
            Authentication authentication) {
        String username = authentication.getName();
        try {
            SparePartsExportRequest issued = exportRequestService.issueExportRequest(id, request, username);
            return ResponseEntity.ok(issued);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel export request
     */
    @PostMapping("/export-requests/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'TECHNICIAN')")
    public ResponseEntity<SparePartsExportRequest> cancelExportRequest(
            @PathVariable Long id,
            @RequestBody CancelRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        SparePartsExportRequest cancelled = exportRequestService.cancelExportRequest(id, request.getReason(), username);
        return ResponseEntity.ok(cancelled);
    }

    /**
     * Get export request statistics
     */
    @GetMapping("/export-requests/statistics")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<SparePartsExportRequestService.ExportRequestStatistics> getExportRequestStatistics() {
        SparePartsExportRequestService.ExportRequestStatistics stats = exportRequestService.getExportRequestStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Check spare part availability for technicians
     */
    @GetMapping("/availability/{sparePartId}")
    @PreAuthorize("hasAnyAuthority('TECHNICIAN', 'MANAGER', 'STAFF')")
    public ResponseEntity<AvailabilityResponse> checkSparePartAvailability(@PathVariable Long sparePartId) {
        Integer availableStock = inventoryService.getAvailableStock(sparePartId);
        boolean isLowStock = inventoryService.getInventoryBySparePartId(sparePartId)
                .map(inv -> inv.isLowStock())
                .orElse(true);
        
        AvailabilityResponse response = AvailabilityResponse.builder()
                .sparePartId(sparePartId)
                .availableQuantity(availableStock)
                .isLowStock(isLowStock)
                .isOutOfStock(availableStock <= 0)
                .build();
        
        return ResponseEntity.ok(response);
    }

    // === IMPORT REQUEST MANAGEMENT ===

    /**
     * Create import request
     */
    @PostMapping("/import-requests")
    @PreAuthorize("hasAuthority('STAFF')")
    public ResponseEntity<ImportRequestResponse> createImportRequest(
            @RequestBody SparePartsImportRequestService.CreateImportRequestDto request,
            Authentication authentication) {
        String username = authentication.getName();
        SparePartsImportRequest created = importRequestService.createImportRequest(request, username);
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
            @RequestParam(required = false) Long sparePartId,
            @RequestParam(required = false) String requestedBy,
            Pageable pageable) {
        Page<SparePartsImportRequest> requests = importRequestService.searchImportRequests(
                keyword, status, supplierId, sparePartId, requestedBy, pageable);
        List<ImportRequestResponse> content = requests.getContent().stream()
                .map(this::mapImportRequest)
                .collect(Collectors.toList());
        PageImpl<ImportRequestResponse> page = new PageImpl<>(content, pageable, requests.getTotalElements());
        return ResponseEntity.ok(page);
    }

    /**
     * Get pending requests for review
     */
    @GetMapping("/import-requests/pending")
    @PreAuthorize("hasAuthority('MANAGER')")
    public ResponseEntity<List<ImportRequestResponse>> getPendingImportRequests() {
        List<ImportRequestResponse> pendingRequests = importRequestService.getPendingRequestsForReview()
                .stream().map(this::mapImportRequest).collect(Collectors.toList());
        return ResponseEntity.ok(pendingRequests);
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
        SparePartsImportRequest approved = importRequestService.approveImportRequest(
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
        SparePartsImportRequest rejected = importRequestService.rejectImportRequest(
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
            @RequestBody SparePartsImportRequestService.CompleteImportRequestDto request,
            Authentication authentication) {
        String username = authentication.getName();
        SparePartsImportRequest completed = importRequestService.completeImportRequest(id, request, username);
        return ResponseEntity.ok(mapImportRequest(completed));
    }

    /**
     * Get import request statistics
     */
    @GetMapping("/import-requests/statistics")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'STAFF')")
    public ResponseEntity<SparePartsImportRequestService.ImportRequestStatistics> getImportRequestStatistics() {
        SparePartsImportRequestService.ImportRequestStatistics stats = importRequestService.getImportRequestStatistics();
        return ResponseEntity.ok(stats);
    }

    // === MAPPERS ===
    private ImportRequestResponse mapImportRequest(SparePartsImportRequest entity) {
        ImportRequestResponse.ImportItem sparePart = null;
        if (entity.getSparePart() != null) {
            sparePart = ImportRequestResponse.ImportItem.builder()
                    .id(entity.getSparePart().getId())
                    .name(entity.getSparePart().getPartName())
                    .code(entity.getSparePart().getPartCode())
                    .build();
        }
        ImportRequestResponse.SupplierItem supplier = null;
        if (entity.getSupplier() != null) {
            supplier = ImportRequestResponse.SupplierItem.builder()
                    .id(entity.getSupplier().getId())
                    .companyName(entity.getSupplier().getCompanyName())
                    .build();
        }

        return ImportRequestResponse.builder()
                .id(entity.getId())
                .requestNumber(entity.getRequestNumber())
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
                .sparePart(sparePart)
                .supplier(supplier)
                .build();
    }

    // === DTOs ===

    @lombok.Data
    public static class AdjustInventoryRequest {
        private Long sparePartId;
        private Integer newQuantity;
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

    @lombok.Data
    public static class CancelRequest {
        private String reason;
    }

    @lombok.Data
    @lombok.Builder
    public static class AvailabilityResponse {
        private Long sparePartId;
        private Integer availableQuantity;
        private boolean isLowStock;
        private boolean isOutOfStock;
    }

    @lombok.Data
    @lombok.Builder
    public static class InventoryItemResponse {
        private Long inventoryId;
        private Long sparePartId;
        private String partName;
        private String partCode;
        private String description;
        private Integer quantityInStock;
        private Integer minimumStockLevel;
        private Integer maximumStockLevel;
    }

    private InventoryItemResponse mapInventoryItem(SparePartsInventory inv) {
        return InventoryItemResponse.builder()
                .inventoryId(inv.getId())
                .sparePartId(inv.getSparePart() != null ? inv.getSparePart().getId() : null)
                .partName(inv.getSparePart() != null ? inv.getSparePart().getPartName() : null)
                .partCode(inv.getSparePart() != null ? inv.getSparePart().getPartCode() : null)
                .description(inv.getSparePart() != null ? inv.getSparePart().getDescription() : null)
                .quantityInStock(inv.getQuantityInStock())
                .minimumStockLevel(inv.getMinimumStockLevel())
                .maximumStockLevel(inv.getMaximumStockLevel())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class ImportRequestResponse {
        private Long id;
        private String requestNumber;
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
        private ImportItem sparePart;
        private SupplierItem supplier;

        @lombok.Data
        @lombok.Builder
        public static class ImportItem {
            private Long id;
            private String name;
            private String code;
        }

        @lombok.Data
        @lombok.Builder
        public static class SupplierItem {
            private Long id;
            private String companyName;
        }
    }
}
