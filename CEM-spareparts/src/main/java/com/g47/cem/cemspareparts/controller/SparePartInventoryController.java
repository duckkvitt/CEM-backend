package com.g47.cem.cemspareparts.controller;

import java.util.List;

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

import com.g47.cem.cemspareparts.dto.request.CreateSparePartInventoryRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSparePartInventoryRequest;
import com.g47.cem.cemspareparts.dto.response.ApiResponse;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartInventoryResponse;
import com.g47.cem.cemspareparts.service.SparePartInventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing SparePartInventory operations
 */
@RestController
@RequestMapping({"/api/v1/spare-part-inventory", "/spare-part-inventory"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Spare Part Inventory", description = "APIs for managing spare part inventory")
public class SparePartInventoryController {
    
    private final SparePartInventoryService sparePartInventoryService;
    
    @PostMapping
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Create new spare part inventory", description = "Create a new spare part inventory record (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryResponse>> createSparePartInventory(
            @Valid @RequestBody CreateSparePartInventoryRequest request) {
        log.info("Creating new spare part inventory for spare part ID: {}", request.getSparePartId());
        SparePartInventoryResponse response = sparePartInventoryService.createSparePartInventory(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Update spare part inventory", description = "Update an existing spare part inventory record (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryResponse>> updateSparePartInventory(
            @Parameter(description = "Inventory ID") @PathVariable Long id,
            @Valid @RequestBody UpdateSparePartInventoryRequest request) {
        log.info("Updating spare part inventory with ID: {}", id);
        SparePartInventoryResponse response = sparePartInventoryService.updateSparePartInventory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get spare part inventory by ID", description = "Get spare part inventory by ID (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryResponse>> getSparePartInventoryById(
            @Parameter(description = "Inventory ID") @PathVariable Long id) {
        SparePartInventoryResponse response = sparePartInventoryService.getSparePartInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/spare-part/{sparePartId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get spare part inventory by spare part ID", description = "Get spare part inventory by spare part ID (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryResponse>> getSparePartInventoryBySparePartId(
            @Parameter(description = "Spare part ID") @PathVariable Long sparePartId) {
        SparePartInventoryResponse response = sparePartInventoryService.getSparePartInventoryBySparePartId(sparePartId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get all spare part inventory", description = "Get all spare part inventory with pagination and sorting (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<PagedResponse<SparePartInventoryResponse>>> getAllSparePartInventory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        PagedResponse<SparePartInventoryResponse> response = sparePartInventoryService
                .getAllSparePartInventory(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/all-with-inventory")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get all spare parts with inventory data", description = "Get all spare parts with inventory data, creating default inventory for missing ones (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<PagedResponse<SparePartInventoryResponse>>> getAllSparePartsWithInventory(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        PagedResponse<SparePartInventoryResponse> response = sparePartInventoryService
                .getAllSparePartsWithInventory(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Search spare part inventory", description = "Search spare part inventory with filters (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<PagedResponse<SparePartInventoryResponse>>> searchSparePartInventory(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "In stock filter") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PagedResponse<SparePartInventoryResponse> response = sparePartInventoryService
                .searchSparePartInventory(keyword, inStock, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get low stock items", description = "Get spare parts with low stock levels (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<List<SparePartInventoryResponse>>> getLowStockItems() {
        List<SparePartInventoryResponse> response = sparePartInventoryService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/needing-reorder")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get items needing reorder", description = "Get spare parts that need reordering (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<List<SparePartInventoryResponse>>> getItemsNeedingReorder() {
        List<SparePartInventoryResponse> response = sparePartInventoryService.getItemsNeedingReorder();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get out of stock items", description = "Get spare parts that are out of stock (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<List<SparePartInventoryResponse>>> getOutOfStockItems() {
        List<SparePartInventoryResponse> response = sparePartInventoryService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/warehouse/{location}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get inventory by warehouse location", description = "Get spare part inventory by warehouse location (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<List<SparePartInventoryResponse>>> getInventoryByWarehouseLocation(
            @Parameter(description = "Warehouse location") @PathVariable String location) {
        List<SparePartInventoryResponse> response = sparePartInventoryService.getInventoryByWarehouseLocation(location);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{sparePartId}/add-stock")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Add stock to spare part inventory", description = "Add stock to existing spare part inventory (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryResponse>> addStock(
            @Parameter(description = "Spare part ID") @PathVariable Long sparePartId,
            @Parameter(description = "Quantity to add") @RequestParam Integer quantity,
            @Parameter(description = "Notes") @RequestParam(required = false) String notes) {
        log.info("Adding {} units to spare part inventory for spare part ID: {}", quantity, sparePartId);
        SparePartInventoryResponse response = sparePartInventoryService.addStock(sparePartId, quantity, notes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{sparePartId}/remove-stock")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Remove stock from spare part inventory", description = "Remove stock from existing spare part inventory (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryResponse>> removeStock(
            @Parameter(description = "Spare part ID") @PathVariable Long sparePartId,
            @Parameter(description = "Quantity to remove") @RequestParam Integer quantity,
            @Parameter(description = "Notes") @RequestParam(required = false) String notes) {
        log.info("Removing {} units from spare part inventory for spare part ID: {}", quantity, sparePartId);
        SparePartInventoryResponse response = sparePartInventoryService.removeStock(sparePartId, quantity, notes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{sparePartId}/stock-level")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get current stock level", description = "Get current stock level for a spare part (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<Integer>> getCurrentStockLevel(
            @Parameter(description = "Spare part ID") @PathVariable Long sparePartId) {
        Integer stockLevel = sparePartInventoryService.getCurrentStockLevel(sparePartId);
        return ResponseEntity.ok(ApiResponse.success(stockLevel));
    }
    
    @GetMapping("/{sparePartId}/has-sufficient-stock")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Check sufficient stock", description = "Check if spare part has sufficient stock (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<Boolean>> hasSufficientStock(
            @Parameter(description = "Spare part ID") @PathVariable Long sparePartId,
            @Parameter(description = "Required quantity") @RequestParam Integer requiredQuantity) {
        boolean hasSufficient = sparePartInventoryService.hasSufficientStock(sparePartId, requiredQuantity);
        return ResponseEntity.ok(ApiResponse.success(hasSufficient));
    }
    
    @GetMapping("/total-value")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get total inventory value", description = "Get total value of all spare part inventory (Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<Object>> getTotalInventoryValue() {
        Object totalValue = sparePartInventoryService.getTotalInventoryValue();
        return ResponseEntity.ok(ApiResponse.success(totalValue));
    }
    
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get dashboard statistics", description = "Get dashboard statistics for spare part inventory (Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<SparePartInventoryService.DashboardStats>> getDashboardStats() {
        SparePartInventoryService.DashboardStats stats = sparePartInventoryService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Delete spare part inventory", description = "Delete spare part inventory record (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<Void>> deleteSparePartInventory(
            @Parameter(description = "Inventory ID") @PathVariable Long id) {
        log.info("Deleting spare part inventory with ID: {}", id);
        sparePartInventoryService.deleteSparePartInventory(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
