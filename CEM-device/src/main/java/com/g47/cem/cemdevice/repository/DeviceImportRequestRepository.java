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

import com.g47.cem.cemdevice.entity.DeviceImportRequest;
import com.g47.cem.cemdevice.enums.ImportRequestStatus;
import com.g47.cem.cemdevice.enums.ApprovalStatus;

/**
 * Repository interface for DeviceImportRequest entity
 */
@Repository
public interface DeviceImportRequestRepository extends JpaRepository<DeviceImportRequest, Long> {
    
    /**
     * Find import request by request number
     */
    Optional<DeviceImportRequest> findByRequestNumber(String requestNumber);
    
    /**
     * Find import request with device information by ID
     */
    @Query("SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device WHERE dir.id = :id")
    Optional<DeviceImportRequest> findByIdWithDevice(@Param("id") Long id);
    
    /**
     * Find all requests by status
     */
    Page<DeviceImportRequest> findByRequestStatus(ImportRequestStatus status, Pageable pageable);
    
    /**
     * Find all requests by approval status
     */
    Page<DeviceImportRequest> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * Find all requests by device ID
     */
    @Query("SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device WHERE dir.device.id = :deviceId ORDER BY dir.requestedAt DESC")
    List<DeviceImportRequest> findByDeviceIdOrderByRequestedAtDesc(@Param("deviceId") Long deviceId);
    
    /**
     * Find all requests by requested by user
     */
    Page<DeviceImportRequest> findByRequestedByOrderByRequestedAtDesc(String requestedBy, Pageable pageable);
    
    /**
     * Find all pending requests that need review
     */
    @Query("SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device WHERE dir.requestStatus = 'PENDING' ORDER BY dir.requestedAt ASC")
    List<DeviceImportRequest> findPendingRequestsForReview();
    
    /**
     * Find approved requests that are ready for completion
     */
    @Query("SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device WHERE dir.requestStatus = 'APPROVED' ORDER BY dir.expectedDeliveryDate ASC")
    List<DeviceImportRequest> findApprovedRequestsReadyForCompletion();
    
    /**
     * Search import requests with filters using precomputed LIKE patterns to avoid DB casting issues
     */
    @Query(
        "SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device d WHERE " +
        "(:keywordPattern IS NULL OR " +
        "LOWER(dir.requestNumber) LIKE :keywordPattern OR " +
        "LOWER(d.name) LIKE :keywordPattern OR " +
        "LOWER(d.model) LIKE :keywordPattern OR " +
        "LOWER(dir.requestedBy) LIKE :keywordPattern) AND " +
        "(:status IS NULL OR dir.requestStatus = :status) AND " +
        "(:supplierId IS NULL OR dir.supplierId = :supplierId) AND " +
        "(:requestedByPattern IS NULL OR LOWER(dir.requestedBy) LIKE :requestedByPattern)"
    )
    Page<DeviceImportRequest> searchImportRequests(@Param("keywordPattern") String keywordPattern,
                                                   @Param("status") ImportRequestStatus status,
                                                   @Param("supplierId") Long supplierId,
                                                   @Param("requestedByPattern") String requestedByPattern,
                                                   Pageable pageable);
    
    /**
     * Get requests statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(dir) as totalRequests, " +
           "COUNT(CASE WHEN dir.requestStatus = 'PENDING' THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN dir.requestStatus = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN dir.requestStatus = 'COMPLETED' THEN 1 END) as completedCount, " +
           "COUNT(CASE WHEN dir.requestStatus = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "SUM(CASE WHEN dir.requestStatus = 'COMPLETED' THEN dir.totalAmount ELSE 0 END) as totalValue " +
           "FROM DeviceImportRequest dir")
    Object[] getImportRequestStatistics();
    
    /**
     * Get requests by date range
     */
    @Query("SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device WHERE " +
           "dir.requestedAt BETWEEN :startDate AND :endDate ORDER BY dir.requestedAt DESC")
    List<DeviceImportRequest> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find requests that need approval reminder (pending for more than specified hours)
     */
    @Query("SELECT dir FROM DeviceImportRequest dir JOIN FETCH dir.device WHERE " +
           "dir.requestStatus = 'PENDING' AND dir.requestedAt < :thresholdDate ORDER BY dir.requestedAt ASC")
    List<DeviceImportRequest> findRequestsNeedingApprovalReminder(@Param("thresholdDate") LocalDateTime thresholdDate);
}
