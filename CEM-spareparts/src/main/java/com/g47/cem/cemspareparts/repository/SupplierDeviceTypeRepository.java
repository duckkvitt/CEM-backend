package com.g47.cem.cemspareparts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemspareparts.entity.SupplierDeviceType;

/**
 * Repository interface for SupplierDeviceType entity
 */
@Repository
public interface SupplierDeviceTypeRepository extends JpaRepository<SupplierDeviceType, Long> {
    
    /**
     * Find all device types for a specific supplier
     */
    @Query("SELECT sdt FROM SupplierDeviceType sdt " +
           "LEFT JOIN FETCH sdt.supplier " +
           "WHERE sdt.supplier.id = :supplierId AND sdt.isActive = true " +
           "ORDER BY sdt.deviceType ASC")
    List<SupplierDeviceType> findActiveBySupplierIdOrderByDeviceType(@Param("supplierId") Long supplierId);
    
    /**
     * Find all suppliers for a specific device type
     */
    @Query("SELECT sdt FROM SupplierDeviceType sdt " +
           "LEFT JOIN FETCH sdt.supplier " +
           "WHERE LOWER(sdt.deviceType) = LOWER(:deviceType) AND sdt.isActive = true " +
           "ORDER BY sdt.unitPrice ASC")
    List<SupplierDeviceType> findActiveByDeviceTypeOrderByPrice(@Param("deviceType") String deviceType);
    
    /**
     * Find specific supplier-device type relationship
     */
    @Query("SELECT sdt FROM SupplierDeviceType sdt " +
           "LEFT JOIN FETCH sdt.supplier " +
           "WHERE sdt.supplier.id = :supplierId AND " +
           "LOWER(sdt.deviceType) = LOWER(:deviceType) AND " +
           "(:deviceModel IS NULL OR LOWER(sdt.deviceModel) = LOWER(:deviceModel))")
    Optional<SupplierDeviceType> findBySupplierIdAndDeviceTypeAndModel(@Param("supplierId") Long supplierId,
                                                                      @Param("deviceType") String deviceType,
                                                                      @Param("deviceModel") String deviceModel);
    
    /**
     * Search supplier device types with filters
     */
    @Query("SELECT sdt FROM SupplierDeviceType sdt " +
           "LEFT JOIN FETCH sdt.supplier s " +
           "WHERE " +
           "(:supplierId IS NULL OR sdt.supplier.id = :supplierId) AND " +
           "(:isActive IS NULL OR sdt.isActive = :isActive) AND " +
           "(:keyword IS NULL OR " +
           "LOWER(sdt.deviceType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sdt.deviceModel) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SupplierDeviceType> searchSupplierDeviceTypes(@Param("supplierId") Long supplierId,
                                                       @Param("isActive") Boolean isActive,
                                                       @Param("keyword") String keyword,
                                                       Pageable pageable);
    
    /**
     * Find suppliers who can provide a device type with minimum order quantity check
     */
    @Query("SELECT sdt FROM SupplierDeviceType sdt " +
           "LEFT JOIN FETCH sdt.supplier " +
           "WHERE LOWER(sdt.deviceType) = LOWER(:deviceType) AND " +
           "sdt.isActive = true AND " +
           "sdt.minimumOrderQuantity <= :requestedQuantity " +
           "ORDER BY sdt.unitPrice ASC")
    List<SupplierDeviceType> findSuppliersForDeviceTypeWithMinimumQuantity(@Param("deviceType") String deviceType,
                                                                           @Param("requestedQuantity") Integer requestedQuantity);
    
    /**
     * Get distinct device types available from suppliers
     */
    @Query("SELECT DISTINCT sdt.deviceType FROM SupplierDeviceType sdt WHERE sdt.isActive = true ORDER BY sdt.deviceType")
    List<String> findDistinctActiveDeviceTypes();
    
    /**
     * Count device types by supplier
     */
    @Query("SELECT COUNT(sdt) FROM SupplierDeviceType sdt WHERE sdt.supplier.id = :supplierId AND sdt.isActive = true")
    Long countActiveDeviceTypesBySupplier(@Param("supplierId") Long supplierId);
    
    /**
     * Get supplier device type statistics
     */
    @Query("SELECT " +
           "COUNT(DISTINCT sdt.supplier.id) as totalSuppliers, " +
           "COUNT(DISTINCT sdt.deviceType) as totalDeviceTypes, " +
           "COUNT(sdt) as totalRelationships, " +
           "AVG(sdt.unitPrice) as averagePrice " +
           "FROM SupplierDeviceType sdt WHERE sdt.isActive = true AND sdt.unitPrice IS NOT NULL")
    Object[] getSupplierDeviceTypeStatistics();
    
    /**
     * Find device types by supplier with pagination
     */
    @Query("SELECT sdt FROM SupplierDeviceType sdt " +
           "LEFT JOIN FETCH sdt.supplier " +
           "WHERE sdt.supplier.id = :supplierId ORDER BY sdt.deviceType ASC")
    Page<SupplierDeviceType> findBySupplierIdOrderByDeviceType(@Param("supplierId") Long supplierId, Pageable pageable);
}
