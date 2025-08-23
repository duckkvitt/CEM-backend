package com.g47.cem.cemspareparts.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.SparePartsInventoryTransaction;
import com.g47.cem.cemspareparts.enums.InventoryReferenceType;
import com.g47.cem.cemspareparts.enums.InventoryTransactionType;
import com.g47.cem.cemspareparts.repository.SparePartsInventoryTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartsInventoryTransactionService {

    private final SparePartsInventoryTransactionRepository transactionRepository;

    public SparePartsInventoryTransaction createImportTransaction(
            SparePart sparePart, Integer quantity, Integer beforeQuantity,
            Long importRequestId, String reason, String createdBy) {
        String transactionNumber = generateTransactionNumber();
        SparePartsInventoryTransaction tx = SparePartsInventoryTransaction.createImportTransaction(
                sparePart, quantity, beforeQuantity, importRequestId, reason, createdBy, transactionNumber);
        SparePartsInventoryTransaction saved = transactionRepository.save(tx);
        log.info("Created spare-part import transaction {} for part {} qty {}", transactionNumber, sparePart.getId(), quantity);
        return saved;
    }

    public SparePartsInventoryTransaction createExportTransaction(
            SparePart sparePart, Integer quantity, Integer beforeQuantity,
            Long exportRequestId, String reason, String createdBy) {
        String transactionNumber = generateTransactionNumber();
        SparePartsInventoryTransaction tx = SparePartsInventoryTransaction.createExportTransaction(
                sparePart, quantity, beforeQuantity, exportRequestId, reason, createdBy, transactionNumber);
        SparePartsInventoryTransaction saved = transactionRepository.save(tx);
        log.info("Created spare-part export transaction {} for part {} qty {}", transactionNumber, sparePart.getId(), quantity);
        return saved;
    }

    public SparePartsInventoryTransaction createAdjustmentTransaction(
            SparePart sparePart, Integer newQuantity, Integer beforeQuantity,
            String reason, String createdBy) {
        String transactionNumber = generateTransactionNumber();
        SparePartsInventoryTransaction tx = SparePartsInventoryTransaction.createAdjustmentTransaction(
                sparePart, newQuantity, beforeQuantity, reason, createdBy, transactionNumber);
        SparePartsInventoryTransaction saved = transactionRepository.save(tx);
        log.info("Created spare-part adjustment transaction {} for part {} from {} to {}", transactionNumber, sparePart.getId(), beforeQuantity, newQuantity);
        return saved;
    }

    private String generateTransactionNumber() {
        return "SPT-" + System.currentTimeMillis();
    }

    /**
     * Get transactions for a specific spare part
     */
    @Transactional(readOnly = true)
    public List<SparePartsInventoryTransaction> getTransactionsBySparePartId(Long sparePartId) {
        return transactionRepository.findBySparePartIdOrderByCreatedAtDesc(sparePartId);
    }

    /**
     * Search transactions using repository method with JOIN FETCH
     */
    @Transactional(readOnly = true)
    public Page<SparePartsInventoryTransaction> searchTransactions(
            String keyword, InventoryTransactionType transactionType, 
            InventoryReferenceType referenceType, Long sparePartId, Pageable pageable) {
        
        // Use repository method with JOIN FETCH to avoid LazyInitializationException
        return transactionRepository.searchTransactions(keyword, transactionType, referenceType, sparePartId, pageable);
    }

    /**
     * Get recent transactions
     */
    @Transactional(readOnly = true)
    public Page<SparePartsInventoryTransaction> getRecentTransactions(Pageable pageable) {
        return transactionRepository.findRecentTransactions(pageable);
    }

    /**
     * Get transaction statistics
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics() {
        Object[] stats = transactionRepository.getTransactionStatistics();
        if (stats != null && stats.length >= 6) {
            return TransactionStatistics.builder()
                    .totalTransactions(((Number) stats[0]).longValue())
                    .importCount(((Number) stats[1]).longValue())
                    .exportCount(((Number) stats[2]).longValue())
                    .adjustmentCount(((Number) stats[3]).longValue())
                    .totalImported(((Number) stats[4]).longValue())
                    .totalExported(((Number) stats[5]).longValue())
                    .build();
        }
        return TransactionStatistics.builder().build();
    }

    /**
     * DTO for transaction statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class TransactionStatistics {
        private Long totalTransactions;
        private Long importCount;
        private Long exportCount;
        private Long adjustmentCount;
        private Long totalImported;
        private Long totalExported;
    }
}


