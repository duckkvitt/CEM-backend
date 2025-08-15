package com.g47.cem.cemdevice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.DeviceInventory;
import com.g47.cem.cemdevice.entity.DeviceInventoryTransaction;
import com.g47.cem.cemdevice.repository.DeviceInventoryRepository;
import com.g47.cem.cemdevice.repository.DeviceInventoryTransactionRepository;
import com.g47.cem.cemdevice.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing device inventory
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceInventoryService {

    private final DeviceInventoryRepository deviceInventoryRepository;
    private final DeviceInventoryTransactionRepository transactionRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceInventoryTransactionService transactionService;

    /**
     * Get or create inventory for a device
     */
    public DeviceInventory getOrCreateInventory(Long deviceId) {
        return deviceInventoryRepository.findByDeviceId(deviceId)
                .orElseGet(() -> createInventoryForDevice(deviceId));
    }

    /**
     * Create inventory for a device
     */
    private DeviceInventory createInventoryForDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + deviceId));

        DeviceInventory inventory = DeviceInventory.builder()
                .device(device)
                .quantityInStock(0)
                .minimumStockLevel(5)
                .maximumStockLevel(100)
                .lastUpdatedBy("SYSTEM")
                .build();

        DeviceInventory saved = deviceInventoryRepository.save(inventory);
        log.info("Created inventory for device {}: {}", deviceId, saved.getId());
        return saved;
    }

    /**
     * Get inventory by device ID
     */
    @Transactional(readOnly = true)
    public Optional<DeviceInventory> getInventoryByDeviceId(Long deviceId) {
        return deviceInventoryRepository.findByDeviceIdWithDevice(deviceId);
    }

    /**
     * Add stock to inventory
     */
    public DeviceInventory addStock(Long deviceId, Integer quantity, String reason, String updatedBy, Long referenceId) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        Integer beforeQuantity = inventory.getQuantityInStock();

        inventory.addStock(quantity, updatedBy);
        DeviceInventory savedInventory = deviceInventoryRepository.save(inventory);

        // Create transaction record
        transactionService.createImportTransaction(
                inventory.getDevice(), quantity, beforeQuantity, 
                referenceId, reason, updatedBy
        );

        log.info("Added {} units to device {} inventory. New quantity: {}", 
                quantity, deviceId, savedInventory.getQuantityInStock());
        return savedInventory;
    }

    /**
     * Remove stock from inventory
     */
    public boolean removeStock(Long deviceId, Integer quantity, String reason, String updatedBy, Long referenceId) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        Integer beforeQuantity = inventory.getQuantityInStock();

        if (beforeQuantity < quantity) {
            log.warn("Insufficient stock for device {}. Requested: {}, Available: {}", 
                    deviceId, quantity, beforeQuantity);
            return false;
        }

        inventory.removeStock(quantity, updatedBy);
        deviceInventoryRepository.save(inventory);

        // Create transaction record
        transactionService.createExportTransaction(
                inventory.getDevice(), quantity, beforeQuantity, 
                referenceId, reason, updatedBy
        );

        log.info("Removed {} units from device {} inventory. New quantity: {}", 
                quantity, deviceId, inventory.getQuantityInStock());
        return true;
    }

    /**
     * Adjust stock to a specific quantity
     */
    public DeviceInventory adjustStock(Long deviceId, Integer newQuantity, String reason, String updatedBy) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        Integer beforeQuantity = inventory.getQuantityInStock();

        inventory.adjustStock(newQuantity, updatedBy);
        DeviceInventory savedInventory = deviceInventoryRepository.save(inventory);

        // Create transaction record
        transactionService.createAdjustmentTransaction(
                inventory.getDevice(), newQuantity, beforeQuantity, reason, updatedBy
        );

        log.info("Adjusted device {} inventory from {} to {}", 
                deviceId, beforeQuantity, newQuantity);
        return savedInventory;
    }

    /**
     * Update inventory settings
     */
    public DeviceInventory updateInventorySettings(Long deviceId, Integer minLevel, Integer maxLevel, String updatedBy) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        
        if (minLevel != null) {
            inventory.setMinimumStockLevel(minLevel);
        }
        if (maxLevel != null) {
            inventory.setMaximumStockLevel(maxLevel);
        }
        inventory.setLastUpdatedBy(updatedBy);

        DeviceInventory saved = deviceInventoryRepository.save(inventory);
        log.info("Updated inventory settings for device {}: min={}, max={}", 
                deviceId, minLevel, maxLevel);
        return saved;
    }

    /**
     * Get low stock items
     */
    @Transactional(readOnly = true)
    public List<DeviceInventory> getLowStockItems() {
        return deviceInventoryRepository.findLowStockItems();
    }

    /**
     * Get out of stock items
     */
    @Transactional(readOnly = true)
    public List<DeviceInventory> getOutOfStockItems() {
        return deviceInventoryRepository.findOutOfStockItems();
    }

    /**
     * Get over stock items
     */
    @Transactional(readOnly = true)
    public List<DeviceInventory> getOverStockItems() {
        return deviceInventoryRepository.findOverStockItems();
    }

    /**
     * Search inventory with filters
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventory> searchInventory(String keyword, Boolean lowStock, Boolean outOfStock, Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank()) ? null : ("%" + keyword.toLowerCase() + "%");
        return deviceInventoryRepository.searchInventory(keywordPattern, lowStock, outOfStock, pageable);
    }

    /**
     * Get inventory statistics
     */
    @Transactional(readOnly = true)
    public InventoryStatistics getInventoryStatistics() {
        Object[] stats = deviceInventoryRepository.getInventoryStatistics();
        if (stats != null && stats.length >= 4) {
            return InventoryStatistics.builder()
                    .totalItems(((Number) stats[0]).longValue())
                    .totalQuantity(((Number) stats[1]).longValue())
                    .lowStockCount(((Number) stats[2]).longValue())
                    .outOfStockCount(((Number) stats[3]).longValue())
                    .totalValue(deviceInventoryRepository.getTotalInventoryValue())
                    .activeDeviceTypesCount(deviceInventoryRepository.getActiveDeviceTypesCount())
                    .build();
        }
        return InventoryStatistics.builder().build();
    }

    /**
     * Check if sufficient stock is available
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(Long deviceId, Integer requiredQuantity) {
        DeviceInventory inventory = deviceInventoryRepository.findByDeviceId(deviceId).orElse(null);
        return inventory != null && inventory.getQuantityInStock() >= requiredQuantity;
    }

    /**
     * Get available stock quantity
     */
    @Transactional(readOnly = true)
    public Integer getAvailableStock(Long deviceId) {
        return deviceInventoryRepository.findByDeviceId(deviceId)
                .map(DeviceInventory::getQuantityInStock)
                .orElse(0);
    }

    /**
     * DTO for inventory statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class InventoryStatistics {
        private Long totalItems;
        private Long totalQuantity;
        private Long lowStockCount;
        private Long outOfStockCount;
        private Double totalValue;
        private Long activeDeviceTypesCount;
    }
}
