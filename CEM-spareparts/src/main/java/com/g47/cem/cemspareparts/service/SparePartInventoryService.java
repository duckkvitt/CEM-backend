package com.g47.cem.cemspareparts.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.dto.request.CreateSparePartInventoryRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSparePartInventoryRequest;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartInventoryResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartResponse;
import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.SparePartInventory;
import com.g47.cem.cemspareparts.exception.BusinessException;
import com.g47.cem.cemspareparts.exception.ResourceNotFoundException;
import com.g47.cem.cemspareparts.repository.SparePartInventoryRepository;
import com.g47.cem.cemspareparts.repository.SparePartRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing SparePartInventory operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartInventoryService {
    
    private final SparePartInventoryRepository sparePartInventoryRepository;
    private final SparePartRepository sparePartRepository;
    
    /**
     * Create a new spare part inventory record
     */
    public SparePartInventoryResponse createSparePartInventory(CreateSparePartInventoryRequest request) {
        log.info("Creating new spare part inventory for spare part ID: {}", request.getSparePartId());
        
        // Check if spare part exists
        SparePart sparePart = sparePartRepository.findById(request.getSparePartId())
                .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", request.getSparePartId()));
        
        // Check if inventory already exists for this spare part
        Optional<SparePartInventory> existing = sparePartInventoryRepository.findBySparePartId(request.getSparePartId());
        if (existing.isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Inventory already exists for spare part: " + sparePart.getPartName());
        }
        
        SparePartInventory inventory = SparePartInventory.builder()
                .sparePart(sparePart)
                .quantityInStock(request.getQuantityInStock())
                .minimumStockLevel(request.getMinimumStockLevel())
                .maximumStockLevel(request.getMaximumStockLevel())
                .reorderPoint(request.getReorderPoint())
                .unitCost(request.getUnitCost())
                .warehouseLocation(request.getWarehouseLocation())
                .notes(request.getNotes())
                .build();
        
        SparePartInventory savedInventory = sparePartInventoryRepository.save(inventory);
        log.info("Successfully created spare part inventory with ID: {}", savedInventory.getId());
        
        return SparePartInventoryResponse.fromEntity(savedInventory);
    }
    
    /**
     * Update an existing spare part inventory record
     */
    public SparePartInventoryResponse updateSparePartInventory(Long id, UpdateSparePartInventoryRequest request) {
        log.info("Updating spare part inventory with ID: {}", id);
        
        SparePartInventory inventory = sparePartInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SparePartInventory", "id", id));
        
        if (request.getQuantityInStock() != null) {
            inventory.setQuantityInStock(request.getQuantityInStock());
        }
        if (request.getMinimumStockLevel() != null) {
            inventory.setMinimumStockLevel(request.getMinimumStockLevel());
        }
        if (request.getMaximumStockLevel() != null) {
            inventory.setMaximumStockLevel(request.getMaximumStockLevel());
        }
        if (request.getReorderPoint() != null) {
            inventory.setReorderPoint(request.getReorderPoint());
        }
        if (request.getUnitCost() != null) {
            inventory.setUnitCost(request.getUnitCost());
        }
        if (request.getWarehouseLocation() != null) {
            inventory.setWarehouseLocation(request.getWarehouseLocation());
        }
        if (request.getNotes() != null) {
            inventory.setNotes(request.getNotes());
        }
        
        SparePartInventory updatedInventory = sparePartInventoryRepository.save(inventory);
        log.info("Successfully updated spare part inventory with ID: {}", updatedInventory.getId());
        
        return SparePartInventoryResponse.fromEntity(updatedInventory);
    }
    
    /**
     * Get spare part inventory by ID
     */
    @Transactional(readOnly = true)
    public SparePartInventoryResponse getSparePartInventoryById(Long id) {
        SparePartInventory inventory = sparePartInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SparePartInventory", "id", id));
        
        return SparePartInventoryResponse.fromEntity(inventory);
    }
    
    /**
     * Get spare part inventory by spare part ID
     */
    @Transactional(readOnly = true)
    public SparePartInventoryResponse getSparePartInventoryBySparePartId(Long sparePartId) {
        SparePartInventory inventory = sparePartInventoryRepository.findBySparePartId(sparePartId)
                .orElseThrow(() -> new ResourceNotFoundException("SparePartInventory", "sparePartId", sparePartId));
        
        return SparePartInventoryResponse.fromEntity(inventory);
    }
    
    /**
     * Get all spare part inventory with pagination and sorting
     */
    @Transactional(readOnly = true)
    public PagedResponse<SparePartInventoryResponse> getAllSparePartInventory(
            int page, int size, String sortBy, String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SparePartInventory> inventoryPage = sparePartInventoryRepository.findAll(pageable);
        
        List<SparePartInventoryResponse> content = inventoryPage.getContent().stream()
                .map(SparePartInventoryResponse::fromEntity)
                .toList();
        
        return PagedResponse.<SparePartInventoryResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .last(inventoryPage.isLast())
                .build();
    }
    
    /**
     * Get all spare parts with inventory data, creating default inventory for missing ones
     */
    @Transactional(readOnly = true)
    public PagedResponse<SparePartInventoryResponse> getAllSparePartsWithInventory(
            int page, int size, String sortBy, String sortDir) {
        
        // Get all spare parts
        List<SparePart> allSpareParts = sparePartRepository.findAll();
        
        // Get existing inventory records
        List<SparePartInventory> existingInventory = sparePartInventoryRepository.findAll();
        
        // Create a map of spare part ID to inventory
        Map<Long, SparePartInventory> inventoryMap = existingInventory.stream()
                .collect(Collectors.toMap(
                    inv -> inv.getSparePart().getId(),
                    inv -> inv
                ));
        
        // Create inventory responses for all spare parts
        List<SparePartInventoryResponse> allResponses = allSpareParts.stream()
                .map(sparePart -> {
                    SparePartInventory existing = inventoryMap.get(sparePart.getId());
                    if (existing != null) {
                        // Return existing inventory
                        return SparePartInventoryResponse.fromEntity(existing);
                    } else {
                        // Create default inventory response
                        SparePartResponse sparePartResponse = new SparePartResponse();
                        sparePartResponse.setId(sparePart.getId());
                        sparePartResponse.setPartName(sparePart.getPartName());
                        sparePartResponse.setPartCode(sparePart.getPartCode());
                        sparePartResponse.setDescription(sparePart.getDescription());
                        sparePartResponse.setCompatibleDevices(sparePart.getCompatibleDevices());
                        sparePartResponse.setUnitOfMeasurement(sparePart.getUnitOfMeasurement());
                        sparePartResponse.setStatus(sparePart.getStatus());
                        sparePartResponse.setCreatedAt(sparePart.getCreatedAt());
                        sparePartResponse.setUpdatedAt(sparePart.getUpdatedAt());
                        
                        return SparePartInventoryResponse.builder()
                                .id(null) // No inventory record yet
                                .sparePart(sparePartResponse)
                                .quantityInStock(0)
                                .minimumStockLevel(5)
                                .maximumStockLevel(100)
                                .reorderPoint(10)
                                .unitCost(BigDecimal.ZERO)
                                .warehouseLocation("Main Warehouse")
                                .notes("Default inventory record")
                                .createdBy("system")
                                .createdAt(sparePart.getCreatedAt())
                                .updatedAt(sparePart.getUpdatedAt())
                                .build();
                    }
                })
                .collect(Collectors.toList());
        
        // Apply pagination
        int totalElements = allResponses.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<SparePartInventoryResponse> content = allResponses.subList(startIndex, endIndex);
        
        return PagedResponse.<SparePartInventoryResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(endIndex >= totalElements)
                .build();
    }
    
    /**
     * Search spare part inventory with filters
     */
    @Transactional(readOnly = true)
    public PagedResponse<SparePartInventoryResponse> searchSparePartInventory(
            String keyword, Boolean inStock, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SparePartInventory> inventoryPage = sparePartInventoryRepository.searchInventory(keyword, inStock, pageable);
        
        List<SparePartInventoryResponse> content = inventoryPage.getContent().stream()
                .map(SparePartInventoryResponse::fromEntity)
                .toList();
        
        return PagedResponse.<SparePartInventoryResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(inventoryPage.getTotalElements())
                .totalPages(inventoryPage.getTotalPages())
                .last(inventoryPage.isLast())
                .build();
    }
    
    /**
     * Get low stock items
     */
    @Transactional(readOnly = true)
    public List<SparePartInventoryResponse> getLowStockItems() {
        List<SparePartInventory> lowStockItems = sparePartInventoryRepository.findLowStockItems();
        return lowStockItems.stream()
                .map(SparePartInventoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Get items needing reorder
     */
    @Transactional(readOnly = true)
    public List<SparePartInventoryResponse> getItemsNeedingReorder() {
        List<SparePartInventory> itemsNeedingReorder = sparePartInventoryRepository.findItemsNeedingReorder();
        return itemsNeedingReorder.stream()
                .map(SparePartInventoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Get out of stock items
     */
    @Transactional(readOnly = true)
    public List<SparePartInventoryResponse> getOutOfStockItems() {
        List<SparePartInventory> outOfStockItems = sparePartInventoryRepository.findOutOfStockItems();
        return outOfStockItems.stream()
                .map(SparePartInventoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Get inventory by warehouse location
     */
    @Transactional(readOnly = true)
    public List<SparePartInventoryResponse> getInventoryByWarehouseLocation(String location) {
        List<SparePartInventory> inventory = sparePartInventoryRepository.findByWarehouseLocation(location);
        return inventory.stream()
                .map(SparePartInventoryResponse::fromEntity)
                .toList();
    }
    
    /**
     * Add stock to existing inventory
     */
    public SparePartInventoryResponse addStock(Long sparePartId, Integer quantity, String notes) {
        log.info("Adding {} units to spare part inventory for spare part ID: {}", quantity, sparePartId);
        
        // Use a more robust approach to handle race conditions
        SparePartInventory inventory = null;
        try {
            // First, try to find existing inventory
            Optional<SparePartInventory> existingInventory = sparePartInventoryRepository.findBySparePartId(sparePartId);
            
            if (existingInventory.isPresent()) {
                inventory = existingInventory.get();
                log.debug("Found existing inventory record for spare part ID: {}", sparePartId);
            } else {
                // Create new inventory record with proper error handling
                log.info("No inventory record found for spare part ID: {}, creating new one", sparePartId);
                SparePart sparePart = sparePartRepository.findById(sparePartId)
                        .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", sparePartId));
                
                try {
                    inventory = SparePartInventory.builder()
                            .sparePart(sparePart)
                            .quantityInStock(0)
                            .minimumStockLevel(5)
                            .maximumStockLevel(100)
                            .reorderPoint(10)
                            .warehouseLocation("Main Warehouse")
                            .createdBy("System")
                            .build();
                    
                    // Save the new inventory record
                    inventory = sparePartInventoryRepository.save(inventory);
                    log.info("Successfully created new inventory record for spare part ID: {}", sparePartId);
                } catch (Exception e) {
                    // If save fails due to duplicate, try to find the record again
                    log.warn("Failed to create inventory record, checking if it was created by another thread: {}", e.getMessage());
                    Optional<SparePartInventory> retryInventory = sparePartInventoryRepository.findBySparePartId(sparePartId);
                    if (retryInventory.isPresent()) {
                        inventory = retryInventory.get();
                        log.info("Found inventory record created by another thread for spare part ID: {}", sparePartId);
                    } else {
                        // If still not found, re-throw the original exception
                        throw e;
                    }
                }
            }
            
            // Now add stock to the inventory
            inventory.addStock(quantity);
            if (notes != null && !notes.trim().isEmpty()) {
                String currentNotes = inventory.getNotes();
                String newNotes = currentNotes != null ? currentNotes + "\n" + notes : notes;
                inventory.setNotes(newNotes);
            }
            
            SparePartInventory updatedInventory = sparePartInventoryRepository.save(inventory);
            log.info("Successfully added {} units to spare part inventory. New quantity: {}", 
                    quantity, updatedInventory.getQuantityInStock());
            
            return SparePartInventoryResponse.fromEntity(updatedInventory);
            
        } catch (Exception e) {
            log.error("Error adding stock to spare part inventory for spare part ID: {}", sparePartId, e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to add stock to spare part inventory: " + e.getMessage());
        }
    }
    
    /**
     * Remove stock from existing inventory
     */
    public SparePartInventoryResponse removeStock(Long sparePartId, Integer quantity, String notes) {
        log.info("Removing {} units from spare part inventory for spare part ID: {}", quantity, sparePartId);
        
        // Use a more robust approach to handle race conditions
        SparePartInventory inventory = null;
        try {
            // First, try to find existing inventory
            Optional<SparePartInventory> existingInventory = sparePartInventoryRepository.findBySparePartId(sparePartId);
            
            if (existingInventory.isPresent()) {
                inventory = existingInventory.get();
                log.debug("Found existing inventory record for spare part ID: {}", sparePartId);
            } else {
                // Create new inventory record with proper error handling
                log.info("No inventory record found for spare part ID: {}, creating new one", sparePartId);
                SparePart sparePart = sparePartRepository.findById(sparePartId)
                        .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", sparePartId));
                
                try {
                    inventory = SparePartInventory.builder()
                            .sparePart(sparePart)
                            .quantityInStock(0)
                            .minimumStockLevel(5)
                            .maximumStockLevel(100)
                            .reorderPoint(10)
                            .warehouseLocation("Main Warehouse")
                            .createdBy("System")
                            .build();
                    
                    // Save the new inventory record
                    inventory = sparePartInventoryRepository.save(inventory);
                    log.info("Successfully created new inventory record for spare part ID: {}", sparePartId);
                } catch (Exception e) {
                    // If save fails due to duplicate, try to find the record again
                    log.warn("Failed to create inventory record, checking if it was created by another thread: {}", e.getMessage());
                    Optional<SparePartInventory> retryInventory = sparePartInventoryRepository.findBySparePartId(sparePartId);
                    if (retryInventory.isPresent()) {
                        inventory = retryInventory.get();
                        log.info("Found inventory record created by another thread for spare part ID: {}", sparePartId);
                    } else {
                        // If still not found, re-throw the original exception
                        throw e;
                    }
                }
            }
            
            // Check if we have sufficient stock to remove
            if (inventory.getQuantityInStock() < quantity) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Insufficient stock. Available: " + inventory.getQuantityInStock() + 
                        ", Requested: " + quantity);
            }
            
            // Now remove stock from the inventory
            inventory.removeStock(quantity);
            if (notes != null && !notes.trim().isEmpty()) {
                String currentNotes = inventory.getNotes();
                String newNotes = currentNotes != null ? currentNotes + "\n" + notes : notes;
                inventory.setNotes(newNotes);
            }
            
            SparePartInventory updatedInventory = sparePartInventoryRepository.save(inventory);
            log.info("Successfully removed {} units from spare part inventory. New quantity: {}", 
                    quantity, updatedInventory.getQuantityInStock());
            
            return SparePartInventoryResponse.fromEntity(updatedInventory);
            
        } catch (Exception e) {
            log.error("Error removing stock from spare part inventory for spare part ID: {}", sparePartId, e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to remove stock from spare part inventory: " + e.getMessage());
        }
    }
    
    /**
     * Check if spare part has sufficient stock
     */
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(Long sparePartId, Integer requiredQuantity) {
        Optional<SparePartInventory> inventory = sparePartInventoryRepository.findBySparePartId(sparePartId);
        if (inventory.isEmpty()) {
            return false;
        }
        
        return inventory.get().getQuantityInStock() >= requiredQuantity;
    }
    
    /**
     * Get current stock level for a spare part
     */
    @Transactional(readOnly = true)
    public Integer getCurrentStockLevel(Long sparePartId) {
        Optional<SparePartInventory> inventory = sparePartInventoryRepository.findBySparePartId(sparePartId);
        return inventory.map(SparePartInventory::getQuantityInStock).orElse(0);
    }
    
    /**
     * Get total inventory value
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue() {
        List<SparePartInventory> allInventory = sparePartInventoryRepository.findAll();
        return allInventory.stream()
                .filter(inv -> inv.getUnitCost() != null && inv.getQuantityInStock() > 0)
                .map(inv -> inv.getUnitCost().multiply(BigDecimal.valueOf(inv.getQuantityInStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Delete spare part inventory
     */
    public void deleteSparePartInventory(Long id) {
        log.info("Deleting spare part inventory with ID: {}", id);
        
        if (!sparePartInventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("SparePartInventory", "id", id);
        }
        
        sparePartInventoryRepository.deleteById(id);
        log.info("Successfully deleted spare part inventory with ID: {}", id);
    }
    
    /**
     * Get dashboard statistics for spare part inventory
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long totalItems = sparePartInventoryRepository.count();
        long lowStockItems = sparePartInventoryRepository.countLowStockItems();
        long outOfStockItems = sparePartInventoryRepository.countOutOfStockItems();
        long itemsNeedingReorder = sparePartInventoryRepository.countItemsNeedingReorder();
        BigDecimal totalValue = getTotalInventoryValue();
        
        return DashboardStats.builder()
                .totalItems(totalItems)
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .itemsNeedingReorder(itemsNeedingReorder)
                .totalValue(totalValue)
                .build();
    }
    
    /**
     * Inner class for dashboard statistics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private long totalItems;
        private long lowStockItems;
        private long outOfStockItems;
        private long itemsNeedingReorder;
        private BigDecimal totalValue;
    }
}
