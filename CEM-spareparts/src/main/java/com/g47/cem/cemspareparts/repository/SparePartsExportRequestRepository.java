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

import com.g47.cem.cemspareparts.entity.SparePartsExportRequest;
import com.g47.cem.cemspareparts.enums.ExportRequestStatus;
import com.g47.cem.cemspareparts.enums.ApprovalStatus;

/**
 * Repository interface for SparePartsExportRequest entity
 */
@Repository
public interface SparePartsExportRequestRepository extends JpaRepository<SparePartsExportRequest, Long> {
    
    /**
     * Find export request by request number
     */
    Optional<SparePartsExportRequest> findByRequestNumber(String requestNumber);
    
    /**
     * Find export request with spare part information by ID
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.id = :id")
    Optional<SparePartsExportRequest> findByIdWithSparePart(@Param("id") Long id);
    
    /**
     * Find all requests by status
     */
    Page<SparePartsExportRequest> findByRequestStatus(ExportRequestStatus status, Pageable pageable);
    
    /**
     * Find all requests by approval status
     */
    Page<SparePartsExportRequest> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * Find all requests by spare part ID
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.sparePart.id = :sparePartId ORDER BY sper.requestedAt DESC")
    List<SparePartsExportRequest> findBySparePartIdOrderByRequestedAtDesc(@Param("sparePartId") Long sparePartId);
    
    /**
     * Find all requests by task ID
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.taskId = :taskId ORDER BY sper.requestedAt DESC")
    List<SparePartsExportRequest> findByTaskIdOrderByRequestedAtDesc(@Param("taskId") Long taskId);
    
    /**
     * Find all requests by requested by user (technician)
     */
    Page<SparePartsExportRequest> findByRequestedByOrderByRequestedAtDesc(String requestedBy, Pageable pageable);
    
    /**
     * Find all pending requests that need review
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.requestStatus = 'PENDING' ORDER BY sper.requestedAt ASC")
    List<SparePartsExportRequest> findPendingRequestsForReview();
    
    /**
     * Find approved requests that need to be issued
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.requestStatus = 'APPROVED' ORDER BY sper.reviewedAt ASC")
    List<SparePartsExportRequest> findApprovedRequestsForIssuing();
    
    /**
     * Search export requests with filters using precomputed patterns
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart sp " +
           "WHERE " +
           "(:keywordPattern IS NULL OR " +
           "LOWER(sper.requestNumber) LIKE :keywordPattern OR " +
           "LOWER(sp.partName) LIKE :keywordPattern OR " +
           "LOWER(sp.partCode) LIKE :keywordPattern OR " +
           "LOWER(sper.requestedBy) LIKE :keywordPattern OR " +
           "LOWER(sper.requestReason) LIKE :keywordPattern) AND " +
           "(:status IS NULL OR sper.requestStatus = :status) AND " +
           "(:sparePartId IS NULL OR sper.sparePart.id = :sparePartId) AND " +
           "(:taskId IS NULL OR sper.taskId = :taskId) AND " +
           "(:requestedByPattern IS NULL OR LOWER(sper.requestedBy) LIKE :requestedByPattern)")
    Page<SparePartsExportRequest> searchExportRequests(@Param("keywordPattern") String keywordPattern,
                                                      @Param("status") ExportRequestStatus status,
                                                      @Param("sparePartId") Long sparePartId,
                                                      @Param("taskId") Long taskId,
                                                      @Param("requestedByPattern") String requestedByPattern,
                                                      Pageable pageable);
    
    /**
     * Get requests statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(sper) as totalRequests, " +
           "COUNT(CASE WHEN sper.requestStatus = 'PENDING' THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN sper.requestStatus = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN sper.requestStatus = 'ISSUED' THEN 1 END) as issuedCount, " +
           "COUNT(CASE WHEN sper.requestStatus = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "SUM(CASE WHEN sper.requestStatus = 'ISSUED' THEN sper.issuedQuantity ELSE 0 END) as totalIssued " +
           "FROM SparePartsExportRequest sper")
    Object[] getExportRequestStatistics();
    
    /**
     * Get requests by date range
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.requestedAt BETWEEN :startDate AND :endDate ORDER BY sper.requestedAt DESC")
    List<SparePartsExportRequest> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find requests that need approval reminder (pending for more than specified hours)
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.requestStatus = 'PENDING' AND sper.requestedAt < :thresholdDate ORDER BY sper.requestedAt ASC")
    List<SparePartsExportRequest> findRequestsNeedingApprovalReminder(@Param("thresholdDate") LocalDateTime thresholdDate);
    
    /**
     * Find recent requests for a technician
     */
    @Query("SELECT sper FROM SparePartsExportRequest sper " +
           "LEFT JOIN FETCH sper.sparePart " +
           "WHERE sper.requestedBy = :technicianUsername ORDER BY sper.requestedAt DESC")
    Page<SparePartsExportRequest> findRecentRequestsByTechnician(@Param("technicianUsername") String technicianUsername, Pageable pageable);
    
    /**
     * Check if there are any pending requests for a spare part (to prevent over-requesting)
     */
    @Query("SELECT COUNT(sper) FROM SparePartsExportRequest sper " +
           "WHERE sper.sparePart.id = :sparePartId AND sper.requestStatus IN ('PENDING', 'APPROVED')")
    Long countPendingRequestsForSparePart(@Param("sparePartId") Long sparePartId);
}
