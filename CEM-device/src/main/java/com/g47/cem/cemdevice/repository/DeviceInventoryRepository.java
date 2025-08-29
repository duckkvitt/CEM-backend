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
    
    Optional<DeviceInventory> findByDeviceId(Long deviceId);
    
    List<DeviceInventory> findByQuantityInStockLessThanEqual(Integer quantity);
    
    List<DeviceInventory> findByQuantityInStockLessThanEqualAndMinimumStockLevelGreaterThan(
            Integer quantity, Integer minimumStockLevel);
    
    @Query("SELECT di FROM DeviceInventory di WHERE di.quantityInStock <= di.reorderPoint AND di.reorderPoint IS NOT NULL")
    List<DeviceInventory> findItemsNeedingReorder();
    
    @Query("SELECT di FROM DeviceInventory di WHERE di.quantityInStock <= di.minimumStockLevel")
    List<DeviceInventory> findLowStockItems();
    
    @Query("SELECT di FROM DeviceInventory di WHERE di.quantityInStock = 0")
    List<DeviceInventory> findOutOfStockItems();
    
    @Query("SELECT di FROM DeviceInventory di WHERE di.warehouseLocation = :location")
    List<DeviceInventory> findByWarehouseLocation(@Param("location") String location);
    
    @Query("SELECT di FROM DeviceInventory di JOIN di.device d WHERE " +
           "(:keyword IS NULL OR d.name LIKE %:keyword% OR d.model LIKE %:keyword% OR d.serialNumber LIKE %:keyword%) " +
           "AND (:inStock IS NULL OR " +
           "     (:inStock = true AND di.quantityInStock > 0) OR " +
           "     (:inStock = false AND di.quantityInStock <= 0)" +
           ")")
    Page<DeviceInventory> searchInventory(@Param("keyword") String keyword,
                                         @Param("inStock") Boolean inStock,
                                         Pageable pageable);
    
    // Additional methods for dashboard statistics
    long countByQuantityInStockLessThanEqualAndMinimumStockLevelGreaterThan(Integer quantity, Integer minimumStockLevel);
    
    long countByQuantityInStock(Integer quantity);
}
