package com.g47.cem.cemdevice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.DeviceInventoryTransaction;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.enums.InventoryReferenceType;

/**
 * Repository interface for DeviceInventoryTransaction entity
 */
@Repository
public interface DeviceInventoryTransactionRepository extends JpaRepository<DeviceInventoryTransaction, Long> {
    
    /**
     * Find transaction by transaction number
     */
    Optional<DeviceInventoryTransaction> findByTransactionNumber(String transactionNumber);
    
    /**
     * Find all transactions for a specific device
     */
    @Query("SELECT dit FROM DeviceInventoryTransaction dit JOIN FETCH dit.device WHERE dit.device.id = :deviceId ORDER BY dit.createdAt DESC")
    List<DeviceInventoryTransaction> findByDeviceIdOrderByCreatedAtDesc(@Param("deviceId") Long deviceId);
    
    /**
     * Find all transactions by type
     */
    Page<DeviceInventoryTransaction> findByTransactionTypeOrderByCreatedAtDesc(InventoryTransactionType transactionType, Pageable pageable);
    
    /**
     * Find all transactions by reference type and ID
     */
    @Query("SELECT dit FROM DeviceInventoryTransaction dit JOIN FETCH dit.device WHERE dit.referenceType = :referenceType AND dit.referenceId = :referenceId ORDER BY dit.createdAt DESC")
    List<DeviceInventoryTransaction> findByReferenceTypeAndReferenceId(@Param("referenceType") InventoryReferenceType referenceType, @Param("referenceId") Long referenceId);
    
    /**
     * Find all transactions by created by user
     */
    Page<DeviceInventoryTransaction> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
    
    /**
     * Find transactions by date range
     */
    @Query("SELECT dit FROM DeviceInventoryTransaction dit JOIN FETCH dit.device WHERE dit.createdAt BETWEEN :startDate AND :endDate ORDER BY dit.createdAt DESC")
    Page<DeviceInventoryTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);
    
    /**
     * Get transaction statistics
     */
    @Query("SELECT " +
           "COUNT(dit) as totalTransactions, " +
           "COUNT(CASE WHEN dit.transactionType = 'IMPORT' THEN 1 END) as importCount, " +
           "COUNT(CASE WHEN dit.transactionType = 'EXPORT' THEN 1 END) as exportCount, " +
           "COUNT(CASE WHEN dit.transactionType = 'ADJUSTMENT' THEN 1 END) as adjustmentCount, " +
           "SUM(CASE WHEN dit.transactionType = 'IMPORT' THEN dit.quantityChange ELSE 0 END) as totalImported, " +
           "SUM(CASE WHEN dit.transactionType = 'EXPORT' THEN ABS(dit.quantityChange) ELSE 0 END) as totalExported " +
           "FROM DeviceInventoryTransaction dit")
    Object[] getTransactionStatistics();
    
    /**
     * Get daily transaction summary for the last N days
     */
    @Query("SELECT " +
           "DATE(dit.createdAt) as transactionDate, " +
           "dit.transactionType, " +
           "COUNT(dit) as transactionCount, " +
           "SUM(ABS(dit.quantityChange)) as totalQuantity " +
           "FROM DeviceInventoryTransaction dit " +
           "WHERE dit.createdAt >= :startDate " +
           "GROUP BY DATE(dit.createdAt), dit.transactionType " +
           "ORDER BY DATE(dit.createdAt) DESC, dit.transactionType")
    List<Object[]> getDailyTransactionSummary(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get recent transactions for dashboard
     */
    @Query("SELECT dit FROM DeviceInventoryTransaction dit JOIN FETCH dit.device ORDER BY dit.createdAt DESC")
    Page<DeviceInventoryTransaction> findRecentTransactions(Pageable pageable);
    
    /**
     * Find the last transaction for a device
     */
    @Query("SELECT dit FROM DeviceInventoryTransaction dit WHERE dit.device.id = :deviceId ORDER BY dit.createdAt DESC")
    Optional<DeviceInventoryTransaction> findLastTransactionForDevice(@Param("deviceId") Long deviceId);

    /**
     * Search transactions with filters using JPQL with JOIN FETCH
     */
    @Query("SELECT DISTINCT dit FROM DeviceInventoryTransaction dit " +
           "JOIN FETCH dit.device d " +
           "WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(dit.transactionNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dit.createdBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(dit.transactionReason) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:transactionType IS NULL OR dit.transactionType = :transactionType) " +
           "AND (:referenceType IS NULL OR dit.referenceType = :referenceType) " +
           "AND (:deviceId IS NULL OR dit.device.id = :deviceId)")
    Page<DeviceInventoryTransaction> searchTransactions(
            @Param("keyword") String keyword,
            @Param("transactionType") InventoryTransactionType transactionType,
            @Param("referenceType") InventoryReferenceType referenceType,
            @Param("deviceId") Long deviceId,
            Pageable pageable);
}
