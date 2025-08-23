package com.g47.cem.cemdevice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.entity.DeviceInventoryTransaction;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.enums.InventoryReferenceType;
import com.g47.cem.cemdevice.repository.DeviceInventoryTransactionRepository;
import com.g47.cem.cemdevice.repository.DeviceRepository;

/**
 * Service class for DeviceInventoryTransaction operations
 */
@Service
@Transactional
public class DeviceInventoryTransactionService {

    @Autowired
    private DeviceInventoryTransactionRepository transactionRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * Create a new transaction
     */
    public DeviceInventoryTransaction createTransaction(DeviceInventoryTransaction transaction) {
        // Validate device exists
        Device device = deviceRepository.findById(transaction.getDevice().getId())
                .orElseThrow(() -> new RuntimeException("Device not found"));
        
        // Set transaction number if not provided
        if (transaction.getTransactionNumber() == null) {
            transaction.setTransactionNumber(generateTransactionNumber());
        }
        
        // Calculate quantities
        if (transaction.getQuantityBefore() == null) {
            transaction.setQuantityBefore(device.getQuantity());
        }
        
        if (transaction.getQuantityAfter() == null) {
            int change = transaction.getQuantityChange();
            if (transaction.getTransactionType() == InventoryTransactionType.EXPORT) {
                change = -change; // Export reduces quantity
            }
            transaction.setQuantityAfter(transaction.getQuantityBefore() + change);
        }
        
        // Update device quantity
        device.setQuantity(transaction.getQuantityAfter());
        deviceRepository.save(device);
        
        return transactionRepository.save(transaction);
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public Optional<DeviceInventoryTransaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Get all transactions with pagination
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    /**
     * Get transactions by device ID
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryTransaction> getTransactionsByDeviceId(Long deviceId) {
        return transactionRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    /**
     * Get transactions by type
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> getTransactionsByType(InventoryTransactionType type, Pageable pageable) {
        return transactionRepository.findByTransactionTypeOrderByCreatedAtDesc(type, pageable);
    }

    /**
     * Get transactions by reference
     */
    @Transactional(readOnly = true)
    public List<DeviceInventoryTransaction> getTransactionsByReference(InventoryReferenceType referenceType, Long referenceId) {
        return transactionRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    /**
     * Get transactions by created by user
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> getTransactionsByCreatedBy(String createdBy, Pageable pageable) {
        return transactionRepository.findByCreatedByOrderByCreatedAtDesc(createdBy, pageable);
    }

    /**
     * Search transactions using repository method with JOIN FETCH
     */
    @Transactional(readOnly = true)
    public Page<DeviceInventoryTransaction> searchTransactions(
            String keyword, InventoryTransactionType transactionType, 
            InventoryReferenceType referenceType, Long deviceId, Pageable pageable) {
        
        // Use repository method with JOIN FETCH to avoid LazyInitializationException
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
