package com.g47.cem.cemdevice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.request.ExportRequest;
import com.g47.cem.cemdevice.dto.request.ImportRequest;
import com.g47.cem.cemdevice.dto.response.DeviceInventoryResponse;
import com.g47.cem.cemdevice.dto.response.InventoryTransactionResponse;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.DeviceInventory;
import com.g47.cem.cemdevice.entity.InventoryTransaction;
import com.g47.cem.cemdevice.enums.InventoryItemType;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService.SparePartDto;
import com.g47.cem.cemdevice.repository.DeviceInventoryRepository;
import com.g47.cem.cemdevice.repository.DeviceRepository;
import com.g47.cem.cemdevice.repository.InventoryTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing inventory operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {
    
    private final DeviceInventoryRepository deviceInventoryRepository;
    private final DeviceRepository deviceRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final SparePartIntegrationService sparePartIntegrationService;
    
    // Spare parts services will be injected when needed
    
    /**
     * Get JWT token from SecurityContextHolder
     * Note: Spare Parts service endpoints require authentication, so we need to extract JWT token
     */
    private String getJwtToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                return authentication.getCredentials().toString();
            }
            // If no JWT token found, return null (this will cause 401/403 errors)
            log.warn("No JWT token found in SecurityContextHolder");
            return null;
        } catch (Exception e) {
            log.error("Error extracting JWT token from SecurityContextHolder", e);
            return null;
        }
    }
    
    /**
     * Import inventory items
     */
    public List<InventoryTransactionResponse> importInventory(ImportRequest request, String createdBy) {
        log.info("Importing inventory items of type: {} from supplier: {}", request.getItemType(), request.getSupplierId());
        
        List<InventoryTransactionResponse> transactions = new java.util.ArrayList<>();
        
        for (ImportRequest.ImportItem item : request.getItems()) {
            InventoryTransaction transaction = createImportTransaction(request, item, createdBy);
            inventoryTransactionRepository.save(transaction);
            
            // Update inventory
            if (request.getItemType() == InventoryItemType.DEVICE) {
                updateDeviceInventory(item, request.getWarehouseLocation(), createdBy);
            } else if (request.getItemType() == InventoryItemType.SPARE_PART) {
                updateSparePartInventory(item, request.getWarehouseLocation(), createdBy);
            }
            
            transactions.add(InventoryTransactionResponse.fromEntity(transaction));
        }
        
        log.info("Successfully imported {} inventory items", transactions.size());
        return transactions;
    }
    
    /**
     * Export inventory items
     */
    public List<InventoryTransactionResponse> exportInventory(ExportRequest request, String createdBy) {
        log.info("Exporting inventory items of type: {} for reference: {} {}", 
                request.getItemType(), request.getReferenceType(), request.getReferenceId());
        
        List<InventoryTransactionResponse> transactions = new java.util.ArrayList<>();
        
        for (ExportRequest.ExportItem item : request.getItems()) {
            // Check stock availability
            if (request.getItemType() == InventoryItemType.DEVICE) {
                checkDeviceStockAvailability(item.getItemId(), item.getQuantity());
            } else if (request.getItemType() == InventoryItemType.SPARE_PART) {
                checkSparePartStockAvailability(item.getItemId(), item.getQuantity());
            }
            
            InventoryTransaction transaction = createExportTransaction(request, item, createdBy);
            inventoryTransactionRepository.save(transaction);
            
            // Update inventory
            if (request.getItemType() == InventoryItemType.DEVICE) {
                reduceDeviceInventory(item.getItemId(), item.getQuantity());
            } else if (request.getItemType() == InventoryItemType.SPARE_PART) {
                reduceSparePartInventory(item.getItemId(), item.getQuantity());
            }
            
            transactions.add(InventoryTransactionResponse.fromEntity(transaction));
        }
        
        log.info("Successfully exported {} inventory items", transactions.size());
        return transactions;
    }
    
    /**
     * Get device inventory overview
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryResponse> getDeviceInventoryOverview() {
        log.debug("Fetching device inventory overview (including devices without inventory records)");

        // Load all inventory records
        List<DeviceInventory> inventoryList = deviceInventoryRepository.findAll();
        java.util.Map<Long, DeviceInventory> deviceIdToInventory = new java.util.HashMap<>();
        for (DeviceInventory inv : inventoryList) {
            if (inv.getDevice() != null && inv.getDevice().getId() != null) {
                deviceIdToInventory.put(inv.getDevice().getId(), inv);
            }
        }

        // Build response: include all devices; for devices missing inventory, create default out-of-stock view
        List<Device> allDevices = deviceRepository.findAll();
        List<DeviceInventoryResponse> responses = new java.util.ArrayList<>(allDevices.size());

        for (Device device : allDevices) {
            DeviceInventory existing = deviceIdToInventory.get(device.getId());
            if (existing != null) {
                responses.add(DeviceInventoryResponse.fromEntity(existing));
                continue;
            }

            // No inventory row → return default out-of-stock representation
            responses.add(DeviceInventoryResponse.builder()
                    .id(null)
                    .deviceId(device.getId())
                    .deviceName(device.getName())
                    .deviceModel(device.getModel())
                    .deviceSerialNumber(device.getSerialNumber())
                    .deviceStatus(device.getStatus() != null ? device.getStatus().name() : null)
                    .quantityInStock(0)
                    .minimumStockLevel(0)
                    .maximumStockLevel(null)
                    .reorderPoint(null)
                    .unitCost(null)
                    .warehouseLocation(null)
                    .notes(null)
                    .createdBy(null)
                    .createdAt(null)
                    .updatedAt(null)
                    .isLowStock(false)
                    .needsReorder(false)
                    .isOutOfStock(true)
                    .build());
        }

        return responses;
    }
    
    /**
     * Get device inventory by device ID
     */
    @Transactional(readOnly = true)
    public DeviceInventoryResponse getDeviceInventory(Long deviceId) {
        log.debug("Fetching device inventory for device ID: {}", deviceId);
        
        DeviceInventory inventory = deviceInventoryRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceInventory", "deviceId", deviceId));
        
        return DeviceInventoryResponse.fromEntity(inventory);
    }
    
    /**
     * Get low stock devices
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryResponse> getLowStockDevices() {
        log.debug("Fetching low stock devices");
        
        List<DeviceInventory> lowStock = deviceInventoryRepository.findLowStockItems();
        return lowStock.stream()
                .map(DeviceInventoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Get devices needing reorder
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryResponse> getDevicesNeedingReorder() {
        log.debug("Fetching devices needing reorder");
        
        List<DeviceInventory> needReorder = deviceInventoryRepository.findItemsNeedingReorder();
        return needReorder.stream()
                .map(DeviceInventoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Search device inventory
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<DeviceInventoryResponse> searchDeviceInventory(
            String keyword, Boolean inStock, org.springframework.data.domain.Pageable pageable) {
        log.debug("Searching device inventory with keyword: {}, inStock: {}", keyword, inStock);
        
        org.springframework.data.domain.Page<DeviceInventory> inventory = deviceInventoryRepository.searchInventory(keyword, inStock, pageable);
        return inventory.map(DeviceInventoryResponse::fromEntity);
    }
    
    /**
     * Get all inventory transactions
     */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getAllInventoryTransactions() {
        log.debug("Fetching all inventory transactions");
        
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findAll();
        return transactions.stream()
                .map(InventoryTransactionResponse::fromEntity)
                .toList();
    }
    
    /**
     * Get inventory transactions by item type
     */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getInventoryTransactionsByItemType(InventoryItemType itemType) {
        log.debug("Fetching inventory transactions for item type: {}", itemType);
        
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByItemType(itemType);
        return transactions.stream()
                .map(InventoryTransactionResponse::fromEntity)
                .toList();
    }
    
    /**
     * Get inventory transactions by transaction type
     */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getInventoryTransactionsByType(InventoryTransactionType transactionType) {
        log.debug("Fetching inventory transactions for transaction type: {}", transactionType);
        
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findByTransactionType(transactionType);
        return transactions.stream()
                .map(InventoryTransactionResponse::fromEntity)
                .toList();
    }
    
    /**
     * Search inventory transactions
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<InventoryTransactionResponse> searchInventoryTransactions(
            String keyword, InventoryItemType itemType, InventoryTransactionType transactionType, 
            org.springframework.data.domain.Pageable pageable) {
        log.debug("Searching inventory transactions with keyword: {}, itemType: {}, transactionType: {}", 
                keyword, itemType, transactionType);
        
        org.springframework.data.domain.Page<InventoryTransaction> transactions = 
                inventoryTransactionRepository.searchTransactions(keyword, itemType, transactionType, pageable);
        return transactions.map(InventoryTransactionResponse::fromEntity);
    }
    
    /**
     * Get inventory dashboard statistics
     */
    @Transactional(readOnly = true)
    public InventoryDashboardStats getInventoryDashboardStats() {
        log.debug("Fetching inventory dashboard statistics");
        
        long totalDevices = deviceInventoryRepository.count();
        long totalSpareParts = 0;
        long lowStockSpareParts = 0;
        long outOfStockSpareParts = 0;
        
        try {
            // Get spare parts data from spare parts service
            List<SparePartDto> spareParts = sparePartIntegrationService.getAllSpareParts(null);
            totalSpareParts = spareParts.size();
            
            // TODO: Get low stock and out of stock counts when spare part inventory service is available
            // For now, we just count total spare parts
            // In the future, we can call the spare parts service dashboard stats endpoint
            
        } catch (Exception e) {
            log.warn("Failed to get spare parts data for dashboard stats, using defaults", e);
        }
        
        long lowStockDevices = deviceInventoryRepository.countByQuantityInStockLessThanEqualAndMinimumStockLevelGreaterThan(0, 0);
        long outOfStockDevices = deviceInventoryRepository.countByQuantityInStock(0);
        
        // Calculate total value (simplified - in production, this should be more sophisticated)
        BigDecimal totalValue = deviceInventoryRepository.findAll().stream()
                .map(inv -> inv.getUnitCost() != null ? 
                    inv.getUnitCost().multiply(BigDecimal.valueOf(inv.getQuantityInStock())) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return InventoryDashboardStats.builder()
                .totalDevices((int) totalDevices)
                .totalSpareParts((int) totalSpareParts)
                .lowStockDevices((int) lowStockDevices)
                .lowStockSpareParts((int) lowStockSpareParts)
                .outOfStockDevices((int) outOfStockDevices)
                .outOfStockSpareParts((int) outOfStockSpareParts)
                .totalValue(totalValue)
                .build();
    }
    
    /**
     * Get recent inventory activity
     */
    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getRecentInventoryActivity(int limit) {
        log.debug("Fetching recent inventory activity, limit: {}", limit);
        
        List<InventoryTransaction> transactions = inventoryTransactionRepository.findTop10ByOrderByCreatedAtDesc();
        return transactions.stream()
                .limit(limit)
                .map(InventoryTransactionResponse::fromEntity)
                .toList();
    }
    
    // Private helper methods
    
    private InventoryTransaction createImportTransaction(ImportRequest request, ImportRequest.ImportItem item, String createdBy) {
        String itemName = getItemName(request.getItemType(), item.getItemId());
        
        return InventoryTransaction.builder()
                .transactionNumber(generateTransactionNumber())
                .transactionType(InventoryTransactionType.IMPORT)
                .itemType(request.getItemType())
                .itemId(item.getItemId())
                .itemName(itemName)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalAmount(item.getUnitPrice() != null ? 
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : null)
                .supplierId(request.getSupplierId())
                .referenceNumber(request.getReferenceNumber())
                .referenceType("IMPORT")
                .warehouseLocation(request.getWarehouseLocation())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();
    }
    
    private InventoryTransaction createExportTransaction(ExportRequest request, ExportRequest.ExportItem item, String createdBy) {
        String itemName = getItemName(request.getItemType(), item.getItemId());
        
        return InventoryTransaction.builder()
                .transactionNumber(generateTransactionNumber())
                .transactionType(InventoryTransactionType.EXPORT)
                .itemType(request.getItemType())
                .itemId(item.getItemId())
                .itemName(itemName)
                .quantity(item.getQuantity())
                .referenceNumber(request.getReferenceNumber())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .warehouseLocation(request.getWarehouseLocation())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();
    }
    
    private String generateTransactionNumber() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String getItemName(InventoryItemType itemType, Long itemId) {
        if (itemType == InventoryItemType.DEVICE) {
            Device device = deviceRepository.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Device", "id", itemId));
            return device.getName();
        } else if (itemType == InventoryItemType.SPARE_PART) {
            try {
                Optional<SparePartDto> sparePart = sparePartIntegrationService.getSparePartById(itemId, getJwtToken());
                if (sparePart.isPresent()) {
                    return sparePart.get().getPartName();
                }
            } catch (Exception e) {
                log.warn("Failed to get spare part name for ID: {}, using fallback", itemId, e);
            }
            return "Spare Part " + itemId;
        }
        return "Unknown Item";
    }
    
    private void updateDeviceInventory(ImportRequest.ImportItem item, String warehouseLocation, String createdBy) {
        DeviceInventory inventory = deviceInventoryRepository.findByDeviceId(item.getItemId())
                .orElseGet(() -> createDefaultDeviceInventory(item.getItemId(), createdBy));
        
        inventory.addStock(item.getQuantity());
        inventory.setUnitCost(item.getUnitCost());
        inventory.setWarehouseLocation(warehouseLocation);
        
        if (item.getMinimumStockLevel() != null) {
            inventory.setMinimumStockLevel(item.getMinimumStockLevel());
        }
        if (item.getMaximumStockLevel() != null) {
            inventory.setMaximumStockLevel(item.getMaximumStockLevel());
        }
        if (item.getReorderPoint() != null) {
            inventory.setReorderPoint(item.getReorderPoint());
        }
        if (item.getNotes() != null) {
            inventory.setNotes(item.getNotes());
        }
        
        deviceInventoryRepository.save(inventory);
    }
    
    private DeviceInventory createDefaultDeviceInventory(Long deviceId, String createdBy) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        
        return DeviceInventory.builder()
                .device(device)
                .quantityInStock(0)
                .minimumStockLevel(0)
                .createdBy(createdBy)
                .build();
    }
    
    private void updateSparePartInventory(ImportRequest.ImportItem item, String warehouseLocation, String createdBy) {
        try {
            // Get spare part details from spare parts service to validate it exists
            Optional<SparePartDto> sparePart = sparePartIntegrationService.getSparePartById(item.getItemId(), getJwtToken());
            if (sparePart.isEmpty()) {
                throw new ResourceNotFoundException("SparePart", "id", item.getItemId());
            }
            
            // Call spare parts service to add stock
            String notes = String.format("Import from device service - %s, Warehouse: %s", createdBy, warehouseLocation);
            boolean success = sparePartIntegrationService.addStockToSparePart(item.getItemId(), item.getQuantity(), notes, getJwtToken());
            
            if (success) {
                log.info("Successfully updated spare part inventory for item: {} ({}), quantity: {}", 
                        item.getItemId(), sparePart.get().getPartName(), item.getQuantity());
            } else {
                log.warn("Failed to update spare part inventory via API, continuing with transaction logging");
                // Continue with transaction logging even if inventory update fails
            }
            
        } catch (Exception e) {
            log.error("Failed to update spare part inventory for item: {}", item.getItemId(), e);
            throw new BusinessException("Failed to update spare part inventory: " + e.getMessage());
        }
    }
    
    private void checkDeviceStockAvailability(Long deviceId, Integer quantity) {
        DeviceInventory inventory = deviceInventoryRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceInventory", "deviceId", deviceId));
        
        if (inventory.getQuantityInStock() < quantity) {
            throw new BusinessException("Insufficient stock for device " + deviceId + 
                    ". Available: " + inventory.getQuantityInStock() + ", Requested: " + quantity);
        }
    }
    
    private void checkSparePartStockAvailability(Long sparePartId, Integer quantity) {
        try {
            // Get spare part details from spare parts service to validate it exists
            Optional<SparePartDto> sparePart = sparePartIntegrationService.getSparePartById(sparePartId, getJwtToken());
            if (sparePart.isEmpty()) {
                throw new ResourceNotFoundException("SparePart", "id", sparePartId);
            }
            
            // Check if spare part has sufficient stock
            boolean hasSufficient = sparePartIntegrationService.hasSufficientStock(sparePartId, quantity, getJwtToken());
            if (!hasSufficient) {
                throw new BusinessException("Insufficient stock for spare part " + sparePartId + 
                        " (" + sparePart.get().getPartName() + "). Requested quantity: " + quantity);
            }
            
            log.info("Spare part stock availability confirmed for spare part: {} ({}), requested quantity: {}", 
                    sparePartId, sparePart.get().getPartName(), quantity);
            
        } catch (Exception e) {
            log.error("Failed to check spare part stock availability for item: {}", sparePartId, e);
            throw new BusinessException("Failed to check spare part stock availability: " + e.getMessage());
        }
    }
    
    private void reduceDeviceInventory(Long deviceId, Integer quantity) {
        DeviceInventory inventory = deviceInventoryRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceInventory", "deviceId", deviceId));
        
        inventory.removeStock(quantity);
        deviceInventoryRepository.save(inventory);
    }
    
    private void reduceSparePartInventory(Long sparePartId, Integer quantity) {
        try {
            // Get spare part details from spare parts service to validate it exists
            Optional<SparePartDto> sparePart = sparePartIntegrationService.getSparePartById(sparePartId, getJwtToken());
            if (sparePart.isEmpty()) {
                throw new ResourceNotFoundException("SparePart", "id", sparePartId);
            }
            
            // Call spare parts service to remove stock
            String notes = String.format("Export from device service, quantity: %d", quantity);
            boolean success = sparePartIntegrationService.removeStockFromSparePart(sparePartId, quantity, notes, getJwtToken());
            
            if (success) {
                log.info("Successfully reduced spare part inventory for spare part: {} ({}), quantity: {}", 
                        sparePartId, sparePart.get().getPartName(), quantity);
            } else {
                log.warn("Failed to reduce spare part inventory via API, continuing with transaction logging");
                // Continue with transaction logging even if inventory update fails
            }
            
        } catch (Exception e) {
            log.error("Failed to reduce spare part inventory for item: {}", sparePartId, e);
            throw new BusinessException("Failed to reduce spare part inventory: " + e.getMessage());
        }
    }
    
    /**
     * DTO for inventory dashboard statistics
     */
    public static class InventoryDashboardStats {
        private int totalDevices;
        private int totalSpareParts;
        private int lowStockDevices;
        private int lowStockSpareParts;
        private int outOfStockDevices;
        private int outOfStockSpareParts;
        private BigDecimal totalValue;
        
        // Builder pattern
        public static InventoryDashboardStatsBuilder builder() {
            return new InventoryDashboardStatsBuilder();
        }
        
        public static class InventoryDashboardStatsBuilder {
            private InventoryDashboardStats stats = new InventoryDashboardStats();
            
            public InventoryDashboardStatsBuilder totalDevices(int totalDevices) {
                stats.totalDevices = totalDevices;
                return this;
            }
            
            public InventoryDashboardStatsBuilder totalSpareParts(int totalSpareParts) {
                stats.totalSpareParts = totalSpareParts;
                return this;
            }
            
            public InventoryDashboardStatsBuilder lowStockDevices(int lowStockDevices) {
                stats.lowStockDevices = lowStockDevices;
                return this;
            }
            
            public InventoryDashboardStatsBuilder lowStockSpareParts(int lowStockSpareParts) {
                stats.lowStockSpareParts = lowStockSpareParts;
                return this;
            }
            
            public InventoryDashboardStatsBuilder outOfStockDevices(int outOfStockDevices) {
                stats.outOfStockDevices = outOfStockDevices;
                return this;
            }
            
            public InventoryDashboardStatsBuilder outOfStockSpareParts(int outOfStockSpareParts) {
                stats.outOfStockSpareParts = outOfStockSpareParts;
                return this;
            }
            
            public InventoryDashboardStatsBuilder totalValue(BigDecimal totalValue) {
                stats.totalValue = totalValue;
                return this;
            }
            
            public InventoryDashboardStats build() {
                return stats;
            }
        }
        
        // Getters
        public int getTotalDevices() { return totalDevices; }
        public int getTotalSpareParts() { return totalSpareParts; }
        public int getLowStockDevices() { return lowStockDevices; }
        public int getLowStockSpareParts() { return lowStockSpareParts; }
        public int getOutOfStockDevices() { return outOfStockDevices; }
        public int getOutOfStockSpareParts() { return outOfStockSpareParts; }
        public BigDecimal getTotalValue() { return totalValue; }
    }
}
