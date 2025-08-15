package com.g47.cem.cemdevice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.DeviceInventory;

/**
 * Repository interface for DeviceInventory entity
 */
@Repository
public interface DeviceInventoryRepository extends JpaRepository<DeviceInventory, Long> {
    
    /**
     * Find inventory by device ID
     */
    Optional<DeviceInventory> findByDeviceId(Long deviceId);
    
    /**
     * Find inventory with device information by device ID
     */
    @Query("SELECT di FROM DeviceInventory di JOIN FETCH di.device WHERE di.device.id = :deviceId")
    Optional<DeviceInventory> findByDeviceIdWithDevice(@Param("deviceId") Long deviceId);
    
    /**
     * Find all inventory items with low stock
     */
    @Query("SELECT di FROM DeviceInventory di JOIN FETCH di.device WHERE di.quantityInStock <= di.minimumStockLevel")
    List<DeviceInventory> findLowStockItems();
    
    /**
     * Find all inventory items that are out of stock
     */
    @Query("SELECT di FROM DeviceInventory di JOIN FETCH di.device WHERE di.quantityInStock = 0")
    List<DeviceInventory> findOutOfStockItems();
    
    /**
     * Find all inventory items with over stock
     */
    @Query("SELECT di FROM DeviceInventory di JOIN FETCH di.device WHERE di.quantityInStock >= di.maximumStockLevel")
    List<DeviceInventory> findOverStockItems();
    
    /**
     * Get total value of inventory
     */
    @Query("SELECT SUM(di.quantityInStock * d.price) FROM DeviceInventory di JOIN di.device d WHERE d.price IS NOT NULL")
    Double getTotalInventoryValue();
    
    /**
     * Get count of different device types in inventory
     */
    @Query("SELECT COUNT(DISTINCT di.device.id) FROM DeviceInventory di WHERE di.quantityInStock > 0")
    Long getActiveDeviceTypesCount();
    
    /**
     * Search inventory with filters
     */
    @Query("SELECT di FROM DeviceInventory di JOIN FETCH di.device d WHERE " +
           "(:keywordPattern IS NULL OR " +
           "LOWER(d.name) LIKE :keywordPattern OR " +
           "LOWER(d.model) LIKE :keywordPattern OR " +
           "LOWER(d.serialNumber) LIKE :keywordPattern) AND " +
           "(:lowStock IS NULL OR " +
           "(:lowStock = true AND di.quantityInStock <= di.minimumStockLevel) OR " +
           "(:lowStock = false AND di.quantityInStock > di.minimumStockLevel)) AND " +
           "(:outOfStock IS NULL OR " +
           "(:outOfStock = true AND di.quantityInStock = 0) OR " +
           "(:outOfStock = false AND di.quantityInStock > 0))")
    Page<DeviceInventory> searchInventory(@Param("keywordPattern") String keywordPattern,
                                         @Param("lowStock") Boolean lowStock,
                                         @Param("outOfStock") Boolean outOfStock,
                                         Pageable pageable);
    
    /**
     * Get inventory statistics
     */
    @Query("SELECT " +
           "COUNT(di) as totalItems, " +
           "SUM(di.quantityInStock) as totalQuantity, " +
           "COUNT(CASE WHEN di.quantityInStock <= di.minimumStockLevel THEN 1 END) as lowStockCount, " +
           "COUNT(CASE WHEN di.quantityInStock = 0 THEN 1 END) as outOfStockCount " +
           "FROM DeviceInventory di")
    Object[] getInventoryStatistics();
}
