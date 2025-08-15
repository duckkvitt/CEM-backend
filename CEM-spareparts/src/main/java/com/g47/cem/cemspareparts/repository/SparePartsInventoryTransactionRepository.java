package com.g47.cem.cemspareparts.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemspareparts.entity.SparePartsInventoryTransaction;
import com.g47.cem.cemspareparts.enums.InventoryTransactionType;
import com.g47.cem.cemspareparts.enums.InventoryReferenceType;

/**
 * Repository interface for SparePartsInventoryTransaction entity
 */
@Repository
public interface SparePartsInventoryTransactionRepository extends JpaRepository<SparePartsInventoryTransaction, Long> {
    
    /**
     * Find transaction by transaction number
     */
    Optional<SparePartsInventoryTransaction> findByTransactionNumber(String transactionNumber);
    
    /**
     * Find all transactions for a specific spare part
     */
    @Query("SELECT spit FROM SparePartsInventoryTransaction spit " +
           "LEFT JOIN FETCH spit.sparePart " +
           "WHERE spit.sparePart.id = :sparePartId ORDER BY spit.createdAt DESC")
    List<SparePartsInventoryTransaction> findBySparePartIdOrderByCreatedAtDesc(@Param("sparePartId") Long sparePartId);
    
    /**
     * Find all transactions by type
     */
    Page<SparePartsInventoryTransaction> findByTransactionTypeOrderByCreatedAtDesc(InventoryTransactionType transactionType, Pageable pageable);
    
    /**
     * Find all transactions by reference type and ID
     */
    @Query("SELECT spit FROM SparePartsInventoryTransaction spit " +
           "LEFT JOIN FETCH spit.sparePart " +
           "WHERE spit.referenceType = :referenceType AND spit.referenceId = :referenceId ORDER BY spit.createdAt DESC")
    List<SparePartsInventoryTransaction> findByReferenceTypeAndReferenceId(@Param("referenceType") InventoryReferenceType referenceType, @Param("referenceId") Long referenceId);
    
    /**
     * Find all transactions by created by user
     */
    Page<SparePartsInventoryTransaction> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
    
    /**
     * Find transactions by date range
     */
    @Query("SELECT spit FROM SparePartsInventoryTransaction spit " +
           "LEFT JOIN FETCH spit.sparePart " +
           "WHERE spit.createdAt BETWEEN :startDate AND :endDate ORDER BY spit.createdAt DESC")
    Page<SparePartsInventoryTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        Pageable pageable);
    
    /**
     * Search transactions with filters
     */
    @Query("SELECT spit FROM SparePartsInventoryTransaction spit " +
           "LEFT JOIN FETCH spit.sparePart sp " +
           "WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(spit.transactionNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.partName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.partCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(spit.createdBy) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(spit.transactionReason) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:transactionType IS NULL OR spit.transactionType = :transactionType) AND " +
           "(:referenceType IS NULL OR spit.referenceType = :referenceType) AND " +
           "(:sparePartId IS NULL OR spit.sparePart.id = :sparePartId)")
    Page<SparePartsInventoryTransaction> searchTransactions(@Param("keyword") String keyword,
                                                           @Param("transactionType") InventoryTransactionType transactionType,
                                                           @Param("referenceType") InventoryReferenceType referenceType,
                                                           @Param("sparePartId") Long sparePartId,
                                                           Pageable pageable);
    
    /**
     * Get transaction statistics
     */
    @Query("SELECT " +
           "COUNT(spit) as totalTransactions, " +
           "COUNT(CASE WHEN spit.transactionType = 'IMPORT' THEN 1 END) as importCount, " +
           "COUNT(CASE WHEN spit.transactionType = 'EXPORT' THEN 1 END) as exportCount, " +
           "COUNT(CASE WHEN spit.transactionType = 'ADJUSTMENT' THEN 1 END) as adjustmentCount, " +
           "SUM(CASE WHEN spit.transactionType = 'IMPORT' THEN spit.quantityChange ELSE 0 END) as totalImported, " +
           "SUM(CASE WHEN spit.transactionType = 'EXPORT' THEN ABS(spit.quantityChange) ELSE 0 END) as totalExported " +
           "FROM SparePartsInventoryTransaction spit")
    Object[] getTransactionStatistics();
    
    /**
     * Get daily transaction summary for the last N days
     */
    @Query("SELECT " +
           "DATE(spit.createdAt) as transactionDate, " +
           "spit.transactionType, " +
           "COUNT(spit) as transactionCount, " +
           "SUM(ABS(spit.quantityChange)) as totalQuantity " +
           "FROM SparePartsInventoryTransaction spit " +
           "WHERE spit.createdAt >= :startDate " +
           "GROUP BY DATE(spit.createdAt), spit.transactionType " +
           "ORDER BY DATE(spit.createdAt) DESC, spit.transactionType")
    List<Object[]> getDailyTransactionSummary(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get recent transactions for dashboard
     */
    @Query("SELECT spit FROM SparePartsInventoryTransaction spit " +
           "LEFT JOIN FETCH spit.sparePart " +
           "ORDER BY spit.createdAt DESC")
    Page<SparePartsInventoryTransaction> findRecentTransactions(Pageable pageable);
    
    /**
     * Find the last transaction for a spare part
     */
    @Query("SELECT spit FROM SparePartsInventoryTransaction spit " +
           "WHERE spit.sparePart.id = :sparePartId ORDER BY spit.createdAt DESC")
    Optional<SparePartsInventoryTransaction> findLastTransactionForSparePart(@Param("sparePartId") Long sparePartId);
}
