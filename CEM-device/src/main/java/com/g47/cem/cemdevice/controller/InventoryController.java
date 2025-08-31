package com.g47.cem.cemdevice.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.request.ExportRequest;
import com.g47.cem.cemdevice.dto.request.ImportRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.DeviceInventoryResponse;
import com.g47.cem.cemdevice.dto.response.InventoryTransactionResponse;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService.SparePartDto;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService.SupplierDto;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService.SupplierDeviceTypeDto;
import com.g47.cem.cemdevice.enums.InventoryItemType;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for inventory management operations
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "APIs for managing device and spare part inventory")
public class InventoryController {
    
    private final InventoryService inventoryService;
    private final SparePartIntegrationService sparePartIntegrationService;
    
    /**
     * Import inventory items
     */
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('STAFF')")
    @Operation(summary = "Import inventory items", description = "Import devices or spare parts into inventory (Staff access required)")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse[]>> importInventory(
            @Valid @RequestBody ImportRequest request,
            Principal principal) {
        
        log.info("Importing inventory by user: {}", principal.getName());
        
        var transactions = inventoryService.importInventory(request, principal.getName());
        InventoryTransactionResponse[] response = transactions.toArray(new InventoryTransactionResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory imported successfully"));
    }
    
    /**
     * Export inventory items
     */
    @PostMapping("/export")
    @PreAuthorize("hasAnyAuthority('STAFF', 'TECHNICIAN')")
    @Operation(summary = "Export inventory items", description = "Export devices or spare parts from inventory (Staff/Technician access required)")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse[]>> exportInventory(
            @Valid @RequestBody ExportRequest request,
            Principal principal) {
        
        log.info("Exporting inventory by user: {}", principal.getName());
        
        var transactions = inventoryService.exportInventory(request, principal.getName());
        InventoryTransactionResponse[] response = transactions.toArray(new InventoryTransactionResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory exported successfully"));
    }
    
    /**
     * Get device inventory overview
     */
    @GetMapping("/devices")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get device inventory overview", description = "Get overview of all device inventory (Staff, Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<DeviceInventoryResponse[]>> getDeviceInventoryOverview() {
        log.debug("Fetching device inventory overview");
        
        var inventory = inventoryService.getDeviceInventoryOverview();
        DeviceInventoryResponse[] response = inventory.toArray(new DeviceInventoryResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Device inventory retrieved successfully"));
    }
    
    /**
     * Get spare parts inventory overview
     */
    @GetMapping("/spare-parts")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get spare parts inventory overview", description = "Get overview of all spare parts inventory (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SparePartDto[]>> getSparePartsInventoryOverview() {
        log.debug("Fetching spare parts inventory overview");
        
        try {
            var spareParts = sparePartIntegrationService.getAllSpareParts(null);
            SparePartDto[] response = spareParts.toArray(new SparePartDto[0]);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Spare parts inventory retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to fetch spare parts inventory", e);
            return ResponseEntity.ok(ApiResponse.success(new SparePartDto[0], "Failed to fetch spare parts inventory"));
        }
    }

    /**
     * Get suppliers data
     */
    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get suppliers data", description = "Get all suppliers data for inventory management (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SupplierDto[]>> getSuppliersData() {
        log.debug("Fetching suppliers data");
        
        try {
            var suppliers = sparePartIntegrationService.getSuppliersForSpareParts(null);
            SupplierDto[] response = suppliers.toArray(new SupplierDto[0]);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Suppliers data retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to fetch suppliers data", e);
            return ResponseEntity.ok(ApiResponse.success(new SupplierDto[0], "Failed to fetch suppliers data"));
        }
    }

    /**
     * Get supplier device types data
     */
    @GetMapping("/supplier-device-types")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get supplier device types data", description = "Get all supplier device types data for inventory management (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<SupplierDeviceTypeDto[]>> getSupplierDeviceTypesData() {
        log.debug("Fetching supplier device types data");
        
        try {
            var supplierDeviceTypes = sparePartIntegrationService.getSuppliersForDevices(null);
            SupplierDeviceTypeDto[] response = supplierDeviceTypes.toArray(new SupplierDeviceTypeDto[0]);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Supplier device types data retrieved successfully"));
        } catch (Exception e) {
            log.error("Failed to fetch supplier device types data", e);
            return ResponseEntity.ok(ApiResponse.success(new SupplierDeviceTypeDto[0], "Failed to fetch supplier device types data"));
        }
    }

    /**
     * Get device inventory by device ID
     */
    @GetMapping("/devices/{deviceId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get device inventory by ID", description = "Get inventory details for a specific device (Staff, Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<DeviceInventoryResponse>> getDeviceInventory(
            @Parameter(description = "Device ID") @PathVariable Long deviceId) {
        log.debug("Fetching device inventory for device ID: {}", deviceId);
        
        var inventory = inventoryService.getDeviceInventory(deviceId);
        return ResponseEntity.ok(ApiResponse.success(inventory, "Device inventory retrieved successfully"));
    }
    
    /**
     * Get low stock devices
     */
    @GetMapping("/devices/low-stock")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get low stock devices", description = "Get list of devices with low stock levels (Staff, Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<DeviceInventoryResponse[]>> getLowStockDevices() {
        log.debug("Fetching low stock devices");
        
        var lowStock = inventoryService.getLowStockDevices();
        DeviceInventoryResponse[] response = lowStock.toArray(new DeviceInventoryResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock devices retrieved successfully"));
    }
    
    /**
     * Get devices needing reorder
     */
    @GetMapping("/devices/needing-reorder")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get devices needing reorder", description = "Get list of devices that need reordering (Staff, Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<DeviceInventoryResponse[]>> getDevicesNeedingReorder() {
        log.debug("Fetching devices needing reorder");
        
        var needReorder = inventoryService.getDevicesNeedingReorder();
        DeviceInventoryResponse[] response = needReorder.toArray(new DeviceInventoryResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Devices needing reorder retrieved successfully"));
    }
    
    /**
     * Search device inventory
     */
    @GetMapping("/devices/search")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Search device inventory", description = "Search device inventory with filters (Staff, Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<Page<DeviceInventoryResponse>>> searchDeviceInventory(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "In stock filter") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Searching device inventory with keyword: {}, inStock: {}, page: {}, size: {}", keyword, inStock, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        var result = inventoryService.searchDeviceInventory(keyword, inStock, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Device inventory search completed successfully"));
    }
    
    /**
     * Get all inventory transactions
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get all inventory transactions", description = "Get all inventory transactions (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse[]>> getAllInventoryTransactions() {
        log.debug("Fetching all inventory transactions");
        
        var transactions = inventoryService.getAllInventoryTransactions();
        InventoryTransactionResponse[] response = transactions.toArray(new InventoryTransactionResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory transactions retrieved successfully"));
    }
    
    /**
     * Get inventory transactions by item type
     */
    @GetMapping("/transactions/type/{itemType}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get inventory transactions by item type", description = "Get inventory transactions filtered by item type (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse[]>> getInventoryTransactionsByItemType(
            @Parameter(description = "Item type (DEVICE or SPARE_PART)") @PathVariable InventoryItemType itemType) {
        log.debug("Fetching inventory transactions for item type: {}", itemType);
        
        var transactions = inventoryService.getInventoryTransactionsByItemType(itemType);
        InventoryTransactionResponse[] response = transactions.toArray(new InventoryTransactionResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory transactions retrieved successfully"));
    }
    
    /**
     * Get inventory transactions by transaction type
     */
    @GetMapping("/transactions/transaction-type/{transactionType}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get inventory transactions by transaction type", description = "Get inventory transactions filtered by transaction type (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse[]>> getInventoryTransactionsByType(
            @Parameter(description = "Transaction type (IMPORT, EXPORT, ADJUSTMENT, etc.)") @PathVariable InventoryTransactionType transactionType) {
        log.debug("Fetching inventory transactions for transaction type: {}", transactionType);
        
        var transactions = inventoryService.getInventoryTransactionsByType(transactionType);
        InventoryTransactionResponse[] response = transactions.toArray(new InventoryTransactionResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory transactions retrieved successfully"));
    }
    
    /**
     * Search inventory transactions
     */
    @GetMapping("/transactions/search")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Search inventory transactions", description = "Search inventory transactions with filters (Staff, Support Team, TechLead, Manager, Technician access required)")
    public ResponseEntity<ApiResponse<Page<InventoryTransactionResponse>>> searchInventoryTransactions(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Item type filter") @RequestParam(required = false) InventoryItemType itemType,
            @Parameter(description = "Transaction type filter") @RequestParam(required = false) InventoryTransactionType transactionType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Searching inventory transactions with keyword: {}, itemType: {}, transactionType: {}, page: {}, size: {}", 
                keyword, itemType, transactionType, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        var result = inventoryService.searchInventoryTransactions(keyword, itemType, transactionType, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Inventory transactions search completed successfully"));
    }
    
    /**
     * Get inventory dashboard statistics
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get inventory dashboard statistics", description = "Get statistics for inventory dashboard (Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<InventoryService.InventoryDashboardStats>> getInventoryDashboardStats() {
        log.debug("Fetching inventory dashboard statistics");
        
        var stats = inventoryService.getInventoryDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }
    
    /**
     * Get recent inventory activity
     */
    @GetMapping("/dashboard/recent-activity")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER')")
    @Operation(summary = "Get recent inventory activity", description = "Get recent inventory activity for dashboard (Support Team, TechLead, Manager access required)")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse[]>> getRecentInventoryActivity(
            @Parameter(description = "Number of recent activities to retrieve") @RequestParam(defaultValue = "10") int limit) {
        log.debug("Fetching recent inventory activity, limit: {}", limit);
        
        var activities = inventoryService.getRecentInventoryActivity(limit);
        InventoryTransactionResponse[] response = activities.toArray(new InventoryTransactionResponse[0]);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Recent inventory activity retrieved successfully"));
    }
}
