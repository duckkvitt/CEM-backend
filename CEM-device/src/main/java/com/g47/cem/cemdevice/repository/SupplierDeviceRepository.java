package com.g47.cem.cemdevice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.SupplierDevice;

/**
 * Repository interface for SupplierDevice entity
 */
@Repository
public interface SupplierDeviceRepository extends JpaRepository<SupplierDevice, Long> {
    
    /**
     * Find all supplier-device relationships for a specific supplier
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device WHERE sd.supplierId = :supplierId")
    List<SupplierDevice> findBySupplierId(@Param("supplierId") Long supplierId);
    
    /**
     * Find all supplier-device relationships for a specific device
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device WHERE sd.device.id = :deviceId")
    List<SupplierDevice> findByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * Find primary supplier for a device
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device WHERE sd.device.id = :deviceId AND sd.isPrimarySupplier = true")
    Optional<SupplierDevice> findPrimarySupplierForDevice(@Param("deviceId") Long deviceId);
    
    /**
     * Find specific supplier-device relationship
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device WHERE sd.supplierId = :supplierId AND sd.device.id = :deviceId")
    Optional<SupplierDevice> findBySupplierIdAndDeviceId(@Param("supplierId") Long supplierId, @Param("deviceId") Long deviceId);
    
    /**
     * Find all suppliers for a device with pricing information
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device WHERE sd.device.id = :deviceId ORDER BY sd.isPrimarySupplier DESC, sd.unitPrice ASC")
    List<SupplierDevice> findSuppliersForDeviceOrderByPriceAsc(@Param("deviceId") Long deviceId);
    
    /**
     * Find devices supplied by a supplier with pagination
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device d WHERE sd.supplierId = :supplierId ORDER BY d.name ASC")
    Page<SupplierDevice> findDevicesSuppliedBySupplier(@Param("supplierId") Long supplierId, Pageable pageable);
    
    /**
     * Search supplier-device relationships
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device d WHERE " +
           "(:supplierId IS NULL OR sd.supplierId = :supplierId) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SupplierDevice> searchSupplierDevices(@Param("supplierId") Long supplierId,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);
    
    /**
     * Count devices supplied by a supplier
     */
    @Query("SELECT COUNT(sd) FROM SupplierDevice sd WHERE sd.supplierId = :supplierId")
    Long countDevicesSuppliedBySupplier(@Param("supplierId") Long supplierId);
    
    /**
     * Find suppliers who can supply a device with minimum order quantity check
     */
    @Query("SELECT sd FROM SupplierDevice sd JOIN FETCH sd.device WHERE sd.device.id = :deviceId AND sd.minimumOrderQuantity <= :requestedQuantity ORDER BY sd.unitPrice ASC")
    List<SupplierDevice> findSuppliersForDeviceWithMinimumQuantity(@Param("deviceId") Long deviceId, @Param("requestedQuantity") Integer requestedQuantity);
    
    /**
     * Get supplier statistics
     */
    @Query("SELECT " +
           "COUNT(DISTINCT sd.supplierId) as totalSuppliers, " +
           "COUNT(DISTINCT sd.device.id) as totalDeviceTypes, " +
           "COUNT(sd) as totalRelationships, " +
           "AVG(sd.unitPrice) as averagePrice " +
           "FROM SupplierDevice sd WHERE sd.unitPrice IS NOT NULL")
    Object[] getSupplierDeviceStatistics();
}
