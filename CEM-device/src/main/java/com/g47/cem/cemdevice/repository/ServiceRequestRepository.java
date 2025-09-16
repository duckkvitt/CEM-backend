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

import com.g47.cem.cemdevice.entity.ServiceRequest;
import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.enums.ServiceRequestType;

/**
 * Repository interface for ServiceRequest entity
 */
@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    
    /**
     * Find service requests by customer ID with pagination
     */
    Page<ServiceRequest> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Find service requests by customer ID and status with pagination
     */
    Page<ServiceRequest> findByCustomerIdAndStatus(Long customerId, ServiceRequestStatus status, Pageable pageable);
    
    /**
     * Find service requests by customer ID and type with pagination
     */
    Page<ServiceRequest> findByCustomerIdAndType(Long customerId, ServiceRequestType type, Pageable pageable);
    
    /**
     * Find service requests by device ID with pagination
     */
    Page<ServiceRequest> findByDeviceId(Long deviceId, Pageable pageable);
    
    /**
     * Find service requests by customer ID and device ID with pagination
     */
    Page<ServiceRequest> findByCustomerIdAndDeviceId(Long customerId, Long deviceId, Pageable pageable);
    
    /**
     * Find service request by request ID
     */
    Optional<ServiceRequest> findByRequestId(String requestId);

    @Query("SELECT sr FROM ServiceRequest sr " +
           "JOIN FETCH sr.device cd " +
           "JOIN FETCH cd.device d " +
           "WHERE sr.id = :id")
    Optional<ServiceRequest> findByIdWithDevice(@Param("id") Long id);
    
    /**
     * Find service requests by status with pagination
     */
    Page<ServiceRequest> findByStatus(ServiceRequestStatus status, Pageable pageable);
    
    /**
     * Find service requests by type with pagination
     */
    Page<ServiceRequest> findByType(ServiceRequestType type, Pageable pageable);
    
    /**
     * Find pending service requests created before a specific date
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.status = :status AND sr.createdAt < :date")
    List<ServiceRequest> findByStatusAndCreatedBefore(@Param("status") ServiceRequestStatus status, @Param("date") LocalDateTime date);
    
    /**
     * Count service requests by customer ID and status
     */
    long countByCustomerIdAndStatus(Long customerId, ServiceRequestStatus status);
    
    /**
     * Count service requests by customer ID and type
     */
    long countByCustomerIdAndType(Long customerId, ServiceRequestType type);
    
    /**
     * Count service requests by customer ID
     */
    long countByCustomerId(Long customerId);
    
    /**
     * Find service requests with keyword search (description, device name, request ID)
     */
    @Query("SELECT sr FROM ServiceRequest sr WHERE sr.customerId = :customerId AND " +
           "(sr.description LIKE %:keyword% OR sr.requestId LIKE %:keyword% OR " +
           "sr.device.device.name LIKE %:keyword% OR sr.device.device.model LIKE %:keyword%)")
    Page<ServiceRequest> findByCustomerIdAndKeyword(@Param("customerId") Long customerId, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Count service requests by status
     */
    long countByStatus(ServiceRequestStatus status);
    
    /**
     * Count service requests by type
     */
    long countByType(ServiceRequestType type);
} 