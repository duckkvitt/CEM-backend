package com.g47.cem.cemspareparts.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.SparePartsInventory;
import com.g47.cem.cemspareparts.repository.SparePartRepository;
import com.g47.cem.cemspareparts.repository.SparePartsInventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing spare parts inventory
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartsInventoryService {

    private final SparePartsInventoryRepository inventoryRepository;
    private final SparePartRepository sparePartRepository;

    /**
     * Get or create inventory for a spare part
     */
    public SparePartsInventory getOrCreateInventory(Long sparePartId) {
        return inventoryRepository.findBySparePartId(sparePartId)
                .orElseGet(() -> createInventoryForSparePart(sparePartId));
    }

    /**
     * Create inventory for a spare part
     */
    private SparePartsInventory createInventoryForSparePart(Long sparePartId) {
        SparePart sparePart = sparePartRepository.findById(sparePartId)
                .orElseThrow(() -> new RuntimeException("Spare part not found with id: " + sparePartId));

        SparePartsInventory inventory = SparePartsInventory.builder()
                .sparePart(sparePart)
                .quantityInStock(0)
                .minimumStockLevel(10)
                .maximumStockLevel(500)
                .lastUpdatedBy("SYSTEM")
                .build();

        SparePartsInventory saved = inventoryRepository.save(inventory);
        log.info("Created inventory for spare part {}: {}", sparePartId, saved.getId());
        return saved;
    }

    /**
     * Get inventory by spare part ID
     */
    @Transactional(readOnly = true)
    public Optional<SparePartsInventory> getInventoryBySparePartId(Long sparePartId) {
        return inventoryRepository.findBySparePartIdWithSparePart(sparePartId);
    }

    /**
     * Add stock to inventory
     */
    public SparePartsInventory addStock(Long sparePartId, Integer quantity, String updatedBy) {
        SparePartsInventory inventory = getOrCreateInventory(sparePartId);
        inventory.addStock(quantity, updatedBy);
        SparePartsInventory saved = inventoryRepository.save(inventory);
        log.info("Added {} units to spare part {} inventory. New quantity: {}", 
                quantity, sparePartId, saved.getQuantityInStock());
        return saved;
    }

    /**
     * Remove stock from inventory
     */
    public boolean removeStock(Long sparePartId, Integer quantity, String updatedBy) {
        SparePartsInventory inventory = getOrCreateInventory(sparePartId);
        if (!inventory.removeStock(quantity, updatedBy)) {
            log.warn("Insufficient stock for spare part {}. Requested: {}, Available: {}", 
                    sparePartId, quantity, inventory.getQuantityInStock());
            return false;
        }
        inventoryRepository.save(inventory);
        log.info("Removed {} units from spare part {} inventory. New quantity: {}", 
                quantity, sparePartId, inventory.getQuantityInStock());
        return true;
    }

    /**
     * Adjust stock to a specific quantity
     */
    public SparePartsInventory adjustStock(Long sparePartId, Integer newQuantity, String updatedBy) {
        SparePartsInventory inventory = getOrCreateInventory(sparePartId);
        inventory.adjustStock(newQuantity, updatedBy);
        SparePartsInventory saved = inventoryRepository.save(inventory);
        log.info("Adjusted spare part {} inventory to {}", sparePartId, newQuantity);
        return saved;
    }

    /**
     * Update inventory settings
     */
    public SparePartsInventory updateInventorySettings(Long sparePartId, Integer minLevel, Integer maxLevel, String updatedBy) {
        SparePartsInventory inventory = getOrCreateInventory(sparePartId);
        
        if (minLevel != null) {
            inventory.setMinimumStockLevel(minLevel);
        }
        if (maxLevel != null) {
            inventory.setMaximumStockLevel(maxLevel);
        }
        inventory.setLastUpdatedBy(updatedBy);

        SparePartsInventory saved = inventoryRepository.save(inventory);
        log.info("Updated inventory settings for spare part {}: min={}, max={}", 
                sparePartId, minLevel, maxLevel);
        return saved;
    }

    /**
     * Get low stock items
     */
    @Transactional(readOnly = true)
    public List<SparePartsInventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    /**
     * Get out of stock items
     */
    @Transactional(readOnly = true)
    public List<SparePartsInventory> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems();
    }

    /**
     * Search inventory using repository method with JOIN FETCH
     */
    @Transactional(readOnly = true)
    public Page<SparePartsInventory> searchInventory(String keyword, Boolean lowStock, Boolean outOfStock, Pageable pageable) {
        // Use repository method with JOIN FETCH to avoid LazyInitializationException
        return inventoryRepository.searchInventory(keyword, lowStock, outOfStock, pageable);
    }

    /**
     * Get inventory statistics
     */
    @Transactional(readOnly = true)
    public InventoryStatistics getInventoryStatistics() {
        Object[] stats = inventoryRepository.getInventoryStatistics();
        if (stats != null && stats.length >= 4) {
            return InventoryStatistics.builder()
                    .totalItems(((Number) stats[0]).longValue())
                    .totalQuantity(((Number) stats[1]).longValue())
                    .lowStockCount(((Number) stats[2]).longValue())
                    .outOfStockCount(((Number) stats[3]).longValue())
                    .activeSparePartTypesCount(inventoryRepository.getActiveSparePartTypesCount())
                    .build();
        }
        return InventoryStatistics.builder().build();
    }

    /**
     * Check if sufficient stock is available
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(Long sparePartId, Integer requiredQuantity) {
        SparePartsInventory inventory = inventoryRepository.findBySparePartId(sparePartId).orElse(null);
        return inventory != null && inventory.getQuantityInStock() >= requiredQuantity;
    }

    /**
     * Get available stock quantity
     */
    @Transactional(readOnly = true)
    public Integer getAvailableStock(Long sparePartId) {
        return inventoryRepository.findBySparePartId(sparePartId)
                .map(SparePartsInventory::getQuantityInStock)
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
        private Long activeSparePartTypesCount;
    }

    @lombok.Data
    public static class LowStockItemDto {
        private Long inventoryId;
        private Long sparePartId;
        private String sparePartName;
        private String sparePartCode;
        private Integer quantityInStock;
        private Integer minimumStockLevel;
    }
}
