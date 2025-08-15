package com.g47.cem.cemdevice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.DeviceInventoryTransaction;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.enums.InventoryReferenceType;
import com.g47.cem.cemdevice.repository.DeviceInventoryTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing device inventory transactions
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceInventoryTransactionService {

    private final DeviceInventoryTransactionRepository transactionRepository;

    /**
     * Create import transaction
     */
    public DeviceInventoryTransaction createImportTransaction(
            Device device, Integer quantity, Integer beforeQuantity, 
            Long importRequestId, String reason, String createdBy) {
        
        String transactionNumber = generateTransactionNumber();
        DeviceInventoryTransaction transaction = DeviceInventoryTransaction.createImportTransaction(
                device, quantity, beforeQuantity, importRequestId, reason, createdBy, transactionNumber);
        
        DeviceInventoryTransaction saved = transactionRepository.save(transaction);
        log.info("Created import transaction {} for device {} with quantity {}", 
                transactionNumber, device.getId(), quantity);
        return saved;
    }

    /**
     * Create export transaction
     */
    public DeviceInventoryTransaction createExportTransaction(
            Device device, Integer quantity, Integer beforeQuantity, 
            Long contractId, String reason, String createdBy) {
        
        String transactionNumber = generateTransactionNumber();
        DeviceInventoryTransaction transaction = DeviceInventoryTransaction.createExportTransaction(
                device, quantity, beforeQuantity, contractId, reason, createdBy, transactionNumber);
        
        DeviceInventoryTransaction saved = transactionRepository.save(transaction);
        log.info("Created export transaction {} for device {} with quantity {}", 
                transactionNumber, device.getId(), quantity);
        return saved;
    }

    /**
     * Create adjustment transaction
     */
    public DeviceInventoryTransaction createAdjustmentTransaction(
            Device device, Integer newQuantity, Integer beforeQuantity, 
            String reason, String createdBy) {
        
        String transactionNumber = generateTransactionNumber();
        DeviceInventoryTransaction transaction = DeviceInventoryTransaction.createAdjustmentTransaction(
                device, newQuantity, beforeQuantity, reason, createdBy, transactionNumber);
        
        DeviceInventoryTransaction saved = transactionRepository.save(transaction);
        log.info("Created adjustment transaction {} for device {} from {} to {}", 
                transactionNumber, device.getId(), beforeQuantity, newQuantity);
        return saved;
    }

    /**
     * Get transactions for a device
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryTransaction> getTransactionsByDeviceId(Long deviceId) {
        return transactionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    /**
     * Get transactions by reference
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryTransaction> getTransactionsByReference(InventoryReferenceType referenceType, Long referenceId) {
        return transactionRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    /**
     * Search transactions
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> searchTransactions(
            String keyword, InventoryTransactionType transactionType, 
            InventoryReferenceType referenceType, Long deviceId, Pageable pageable) {
        return transactionRepository.searchTransactions(keyword, transactionType, referenceType, deviceId, pageable);
    }

    /**
     * Get transactions by date range
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> getTransactionsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Get recent transactions
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> getRecentTransactions(Pageable pageable) {
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
     * Get daily transaction summary
     */
    @Transactional(readOnly = true)
    public List<DailyTransactionSummary> getDailyTransactionSummary(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = transactionRepository.getDailyTransactionSummary(startDate);
        
        return results.stream()
                .map(row -> DailyTransactionSummary.builder()
                        .date(row[0].toString())
                        .transactionType((InventoryTransactionType) row[1])
                        .transactionCount(((Number) row[2]).longValue())
                        .totalQuantity(((Number) row[3]).longValue())
                        .build())
                .toList();
    }

    /**
     * Generate unique transaction number
     */
    private String generateTransactionNumber() {
        // This would typically call a database function or use a sequence
        // For now, using a simple timestamp-based approach
        return "DT-" + System.currentTimeMillis();
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

    /**
     * DTO for daily transaction summary
     */
    @lombok.Data
    @lombok.Builder
    public static class DailyTransactionSummary {
        private String date;
        private InventoryTransactionType transactionType;
        private Long transactionCount;
        private Long totalQuantity;
    }
}
