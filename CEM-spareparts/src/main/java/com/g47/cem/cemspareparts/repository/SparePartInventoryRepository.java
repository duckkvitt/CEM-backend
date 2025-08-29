package com.g47.cem.cemspareparts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemspareparts.entity.SparePartInventory;

/**
 * Repository interface for SparePartInventory entity
 */
@Repository
public interface SparePartInventoryRepository extends JpaRepository<SparePartInventory, Long> {
    
    Optional<SparePartInventory> findBySparePartId(Long sparePartId);
    
    List<SparePartInventory> findByQuantityInStockLessThanEqual(Integer quantity);
    
    List<SparePartInventory> findByQuantityInStockLessThanEqualAndMinimumStockLevelGreaterThan(
            Integer quantity, Integer minimumStockLevel);
    
    @Query("SELECT spi FROM SparePartInventory spi WHERE spi.quantityInStock <= spi.reorderPoint AND spi.reorderPoint IS NOT NULL")
    List<SparePartInventory> findItemsNeedingReorder();
    
    @Query("SELECT spi FROM SparePartInventory spi WHERE spi.quantityInStock <= spi.minimumStockLevel")
    List<SparePartInventory> findLowStockItems();
    
    @Query("SELECT spi FROM SparePartInventory spi WHERE spi.quantityInStock = 0")
    List<SparePartInventory> findOutOfStockItems();
    
    @Query("SELECT spi FROM SparePartInventory spi WHERE spi.warehouseLocation = :location")
    List<SparePartInventory> findByWarehouseLocation(@Param("location") String location);
    
    @Query("SELECT spi FROM SparePartInventory spi JOIN spi.sparePart sp WHERE " +
           "(:keyword IS NULL OR sp.partName LIKE %:keyword% OR sp.partCode LIKE %:keyword%) " +
           "AND (:inStock IS NULL OR " +
           "     (:inStock = true AND spi.quantityInStock > 0) OR " +
           "     (:inStock = false AND spi.quantityInStock <= 0)" +
           ")")
    Page<SparePartInventory> searchInventory(@Param("keyword") String keyword,
                                            @Param("inStock") Boolean inStock,
                                            Pageable pageable);
    
    // Additional methods for dashboard statistics
    @Query("SELECT COUNT(spi) FROM SparePartInventory spi WHERE spi.quantityInStock <= spi.minimumStockLevel")
    long countLowStockItems();
    
    @Query("SELECT COUNT(spi) FROM SparePartInventory spi WHERE spi.quantityInStock = 0")
    long countOutOfStockItems();
    
    @Query("SELECT COUNT(spi) FROM SparePartInventory spi WHERE spi.quantityInStock <= spi.reorderPoint AND spi.reorderPoint IS NOT NULL")
    long countItemsNeedingReorder();
}
