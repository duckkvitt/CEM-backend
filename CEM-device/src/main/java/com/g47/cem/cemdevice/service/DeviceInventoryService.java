package com.g47.cem.cemdevice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.DeviceInventory;
import com.g47.cem.cemdevice.entity.DeviceInventoryTransaction;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.enums.InventoryReferenceType;
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
    public DeviceInventory addStock(Long deviceId, Integer quantity,
                                   InventoryReferenceType referenceType, Long referenceId,
                                   String reason, String createdBy) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        
        // Create transaction record
        DeviceInventoryTransaction transaction = DeviceInventoryTransaction.builder()
                .device(inventory.getDevice())
                .transactionType(InventoryTransactionType.IMPORT)
                .quantityChange(quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
        
        transactionRepository.save(transaction);
        
        // Update inventory
        inventory.setQuantityInStock(inventory.getQuantityInStock() + quantity);
        inventory.setLastUpdatedBy(createdBy);
        
        DeviceInventory saved = deviceInventoryRepository.save(inventory);
        log.info("Added {} stock to device {} inventory. New total: {}", 
                quantity, deviceId, saved.getQuantityInStock());
        return saved;
    }

    /**
     * Remove stock from inventory
     */
    public DeviceInventory removeStock(Long deviceId, Integer quantity,
                                      InventoryReferenceType referenceType, Long referenceId,
                                      String reason, String createdBy) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        
        if (inventory.getQuantityInStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + inventory.getQuantityInStock() + ", Requested: " + quantity);
        }
        
        // Create transaction record
        DeviceInventoryTransaction transaction = DeviceInventoryTransaction.builder()
                .device(inventory.getDevice())
                .transactionType(InventoryTransactionType.EXPORT)
                .quantityChange(-quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
        
        transactionRepository.save(transaction);
        
        // Update inventory
        inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
        inventory.setLastUpdatedBy(createdBy);
        
        DeviceInventory saved = deviceInventoryRepository.save(inventory);
        log.info("Removed {} stock from device {} inventory. New total: {}", 
                quantity, deviceId, saved.getQuantityInStock());
        return saved;
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
     * Adjust stock to a specific quantity
     */
    public DeviceInventory adjustStock(Long deviceId, Integer newQuantity,
                                      InventoryReferenceType referenceType, Long referenceId,
                                      String reason, String createdBy) {
        DeviceInventory inventory = getOrCreateInventory(deviceId);
        Integer beforeQuantity = inventory.getQuantityInStock();
        Integer quantityChange = newQuantity - beforeQuantity;
        
        // Create transaction record
        DeviceInventoryTransaction transaction = DeviceInventoryTransaction.builder()
                .device(inventory.getDevice())
                .transactionType(InventoryTransactionType.ADJUSTMENT)
                .quantityChange(quantityChange)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .transactionReason(reason)
                .createdBy(createdBy)
                .build();
        
        transactionRepository.save(transaction);
        
        // Update inventory
        inventory.setQuantityInStock(newQuantity);
        inventory.setLastUpdatedBy(createdBy);
        
        DeviceInventory saved = deviceInventoryRepository.save(inventory);
        log.info("Adjusted device {} inventory from {} to {}. Change: {}", 
                deviceId, beforeQuantity, newQuantity, quantityChange);
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
     * Search inventory using repository method with JOIN FETCH
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventory> searchInventory(String keyword, Boolean lowStock, Boolean outOfStock, Pageable pageable) {
        // Use repository method with JOIN FETCH to avoid LazyInitializationException
        return deviceInventoryRepository.searchInventory(keyword, lowStock, outOfStock, pageable);
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
