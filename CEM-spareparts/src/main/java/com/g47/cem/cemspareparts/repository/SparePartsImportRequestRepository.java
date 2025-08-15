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

import com.g47.cem.cemspareparts.entity.SparePartsImportRequest;
import com.g47.cem.cemspareparts.enums.ImportRequestStatus;
import com.g47.cem.cemspareparts.enums.ApprovalStatus;

/**
 * Repository interface for SparePartsImportRequest entity
 */
@Repository
public interface SparePartsImportRequestRepository extends JpaRepository<SparePartsImportRequest, Long> {
    
    /**
     * Find import request by request number
     */
    Optional<SparePartsImportRequest> findByRequestNumber(String requestNumber);
    
    /**
     * Find import request with spare part and supplier information by ID
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.id = :id")
    Optional<SparePartsImportRequest> findByIdWithDetails(@Param("id") Long id);
    
    /**
     * Find all requests by status
     */
    Page<SparePartsImportRequest> findByRequestStatus(ImportRequestStatus status, Pageable pageable);
    
    /**
     * Find all requests by approval status
     */
    Page<SparePartsImportRequest> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
    
    /**
     * Find all requests by spare part ID
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.sparePart.id = :sparePartId ORDER BY spir.requestedAt DESC")
    List<SparePartsImportRequest> findBySparePartIdOrderByRequestedAtDesc(@Param("sparePartId") Long sparePartId);
    
    /**
     * Find all requests by supplier ID
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.supplier.id = :supplierId ORDER BY spir.requestedAt DESC")
    List<SparePartsImportRequest> findBySupplierIdOrderByRequestedAtDesc(@Param("supplierId") Long supplierId);
    
    /**
     * Find all requests by requested by user
     */
    Page<SparePartsImportRequest> findByRequestedByOrderByRequestedAtDesc(String requestedBy, Pageable pageable);
    
    /**
     * Find all pending requests that need review
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.requestStatus = 'PENDING' ORDER BY spir.requestedAt ASC")
    List<SparePartsImportRequest> findPendingRequestsForReview();
    
    /**
     * Find approved requests that are ready for completion
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.requestStatus = 'APPROVED' ORDER BY spir.expectedDeliveryDate ASC")
    List<SparePartsImportRequest> findApprovedRequestsReadyForCompletion();
    
    /**
     * Search import requests with filters using precomputed LIKE patterns
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart sp " +
           "LEFT JOIN FETCH spir.supplier s " +
           "WHERE " +
           "(:keywordPattern IS NULL OR " +
           "LOWER(spir.requestNumber) LIKE :keywordPattern OR " +
           "LOWER(sp.partName) LIKE :keywordPattern OR " +
           "LOWER(sp.partCode) LIKE :keywordPattern OR " +
           "LOWER(s.companyName) LIKE :keywordPattern OR " +
           "LOWER(spir.requestedBy) LIKE :keywordPattern) AND " +
           "(:status IS NULL OR spir.requestStatus = :status) AND " +
           "(:supplierId IS NULL OR spir.supplier.id = :supplierId) AND " +
           "(:sparePartId IS NULL OR spir.sparePart.id = :sparePartId) AND " +
           "(:requestedByPattern IS NULL OR LOWER(spir.requestedBy) LIKE :requestedByPattern)")
    Page<SparePartsImportRequest> searchImportRequests(@Param("keywordPattern") String keywordPattern,
                                                      @Param("status") ImportRequestStatus status,
                                                      @Param("supplierId") Long supplierId,
                                                      @Param("sparePartId") Long sparePartId,
                                                      @Param("requestedByPattern") String requestedByPattern,
                                                      Pageable pageable);
    
    /**
     * Get requests statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(spir) as totalRequests, " +
           "COUNT(CASE WHEN spir.requestStatus = 'PENDING' THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN spir.requestStatus = 'APPROVED' THEN 1 END) as approvedCount, " +
           "COUNT(CASE WHEN spir.requestStatus = 'COMPLETED' THEN 1 END) as completedCount, " +
           "COUNT(CASE WHEN spir.requestStatus = 'REJECTED' THEN 1 END) as rejectedCount, " +
           "SUM(CASE WHEN spir.requestStatus = 'COMPLETED' THEN spir.totalAmount ELSE 0 END) as totalValue " +
           "FROM SparePartsImportRequest spir")
    Object[] getImportRequestStatistics();
    
    /**
     * Get requests by date range
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.requestedAt BETWEEN :startDate AND :endDate ORDER BY spir.requestedAt DESC")
    List<SparePartsImportRequest> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find requests that need approval reminder (pending for more than specified hours)
     */
    @Query("SELECT spir FROM SparePartsImportRequest spir " +
           "LEFT JOIN FETCH spir.sparePart " +
           "LEFT JOIN FETCH spir.supplier " +
           "WHERE spir.requestStatus = 'PENDING' AND spir.requestedAt < :thresholdDate ORDER BY spir.requestedAt ASC")
    List<SparePartsImportRequest> findRequestsNeedingApprovalReminder(@Param("thresholdDate") LocalDateTime thresholdDate);
}
