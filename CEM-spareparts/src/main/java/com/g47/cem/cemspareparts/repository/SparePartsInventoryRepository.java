package com.g47.cem.cemspareparts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemspareparts.entity.SparePartsInventory;

/**
 * Repository interface for SparePartsInventory entity
 */
@Repository
public interface SparePartsInventoryRepository extends JpaRepository<SparePartsInventory, Long> {
    
    /**
     * Find inventory by spare part ID
     */
    Optional<SparePartsInventory> findBySparePartId(Long sparePartId);
    
    /**
     * Find inventory with spare part information by spare part ID
     */
    @Query("SELECT spi FROM SparePartsInventory spi JOIN FETCH spi.sparePart WHERE spi.sparePart.id = :sparePartId")
    Optional<SparePartsInventory> findBySparePartIdWithSparePart(@Param("sparePartId") Long sparePartId);
    
    /**
     * Find all inventory items with low stock
     */
    @Query("SELECT spi FROM SparePartsInventory spi JOIN FETCH spi.sparePart WHERE spi.quantityInStock <= spi.minimumStockLevel")
    List<SparePartsInventory> findLowStockItems();
    
    /**
     * Find all inventory items that are out of stock
     */
    @Query("SELECT spi FROM SparePartsInventory spi JOIN FETCH spi.sparePart WHERE spi.quantityInStock = 0")
    List<SparePartsInventory> findOutOfStockItems();
    
    /**
     * Find all inventory items with over stock
     */
    @Query("SELECT spi FROM SparePartsInventory spi JOIN FETCH spi.sparePart WHERE spi.quantityInStock >= spi.maximumStockLevel")
    List<SparePartsInventory> findOverStockItems();
    
    /**
     * Get count of different spare part types in inventory
     */
    @Query("SELECT COUNT(DISTINCT spi.sparePart.id) FROM SparePartsInventory spi WHERE spi.quantityInStock > 0")
    Long getActiveSparePartTypesCount();
    
    /**
     * Get inventory statistics
     */
    @Query("SELECT " +
           "COUNT(spi) as totalItems, " +
           "SUM(spi.quantityInStock) as totalQuantity, " +
           "COUNT(CASE WHEN spi.quantityInStock <= spi.minimumStockLevel THEN 1 END) as lowStockCount, " +
           "COUNT(CASE WHEN spi.quantityInStock = 0 THEN 1 END) as outOfStockCount " +
           "FROM SparePartsInventory spi")
    Object[] getInventoryStatistics();
    
    /**
     * Get low stock items count for dashboard
     */
    @Query("SELECT COUNT(spi) FROM SparePartsInventory spi WHERE spi.quantityInStock <= spi.minimumStockLevel")
    Long getLowStockItemsCount();
    
    /**
     * Get out of stock items count for dashboard
     */
    @Query("SELECT COUNT(spi) FROM SparePartsInventory spi WHERE spi.quantityInStock = 0")
    Long getOutOfStockItemsCount();

    /**
     * Search inventory with filters using JPQL with JOIN FETCH
     */
    @Query("SELECT DISTINCT spi FROM SparePartsInventory spi " +
           "JOIN FETCH spi.sparePart sp " +
           "WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(sp.partName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.partCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:lowStock IS NULL OR " +
           "(:lowStock = true AND spi.quantityInStock <= spi.minimumStockLevel) OR " +
           "(:lowStock = false AND spi.quantityInStock > spi.minimumStockLevel)) " +
           "AND (:outOfStock IS NULL OR " +
           "(:outOfStock = true AND spi.quantityInStock = 0) OR " +
           "(:outOfStock = false AND spi.quantityInStock > 0))")
    Page<SparePartsInventory> searchInventory(
            @Param("keyword") String keyword,
            @Param("lowStock") Boolean lowStock,
            @Param("outOfStock") Boolean outOfStock,
            Pageable pageable);
}
