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

import com.g47.cem.cemdevice.entity.InventoryTransaction;
import com.g47.cem.cemdevice.enums.InventoryItemType;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;

/**
 * Repository interface for InventoryTransaction entity
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    
    Optional<InventoryTransaction> findByTransactionNumber(String transactionNumber);
    
    List<InventoryTransaction> findByItemTypeAndItemId(InventoryItemType itemType, Long itemId);
    
    List<InventoryTransaction> findByTransactionType(InventoryTransactionType transactionType);
    
    List<InventoryTransaction> findByItemType(InventoryItemType itemType);
    
    List<InventoryTransaction> findBySupplierId(Long supplierId);
    
    List<InventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    List<InventoryTransaction> findByCreatedBy(String createdBy);
    
    List<InventoryTransaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT it FROM InventoryTransaction it WHERE " +
           "(:itemType IS NULL OR it.itemType = :itemType) " +
           "AND (:transactionType IS NULL OR it.transactionType = :transactionType) " +
           "AND (:keyword IS NULL OR it.itemName LIKE %:keyword% OR it.transactionNumber LIKE %:keyword%) " +
           "AND (:startDate IS NULL OR it.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR it.createdAt <= :endDate)")
    Page<InventoryTransaction> searchTransactions(@Param("itemType") InventoryItemType itemType,
                                                @Param("transactionType") InventoryTransactionType transactionType,
                                                @Param("keyword") String keyword,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate,
                                                Pageable pageable);
    
    @Query("SELECT COUNT(it) FROM InventoryTransaction it WHERE it.itemType = :itemType AND it.itemId = :itemId")
    long countTransactionsByItem(@Param("itemType") InventoryItemType itemType, @Param("itemId") Long itemId);
    
    @Query("SELECT SUM(it.quantity) FROM InventoryTransaction it WHERE it.itemType = :itemType AND it.itemId = :itemId AND it.transactionType = :transactionType")
    Integer sumQuantityByItemAndType(@Param("itemType") InventoryItemType itemType, 
                                   @Param("itemId") Long itemId, 
                                   @Param("transactionType") InventoryTransactionType transactionType);
    
    // Additional methods for dashboard and recent activity
    List<InventoryTransaction> findTop10ByOrderByCreatedAtDesc();
    
    @Query("SELECT it FROM InventoryTransaction it WHERE " +
           "(:keyword IS NULL OR it.itemName LIKE %:keyword% OR it.transactionNumber LIKE %:keyword%) " +
           "AND (:itemType IS NULL OR it.itemType = :itemType) " +
           "AND (:transactionType IS NULL OR it.transactionType = :transactionType)")
    Page<InventoryTransaction> searchTransactions(@Param("keyword") String keyword,
                                                @Param("itemType") InventoryItemType itemType,
                                                @Param("transactionType") InventoryTransactionType transactionType,
                                                Pageable pageable);
}
