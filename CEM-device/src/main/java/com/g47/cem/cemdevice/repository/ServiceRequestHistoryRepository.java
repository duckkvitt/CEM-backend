package com.g47.cem.cemdevice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.ServiceRequestHistory;

/**
 * Repository interface for ServiceRequestHistory entity
 */
@Repository
public interface ServiceRequestHistoryRepository extends JpaRepository<ServiceRequestHistory, Long> {
    
    /**
     * Find history entries by service request ID, ordered by creation date descending
     */
    List<ServiceRequestHistory> findByServiceRequestIdOrderByCreatedAtDesc(Long serviceRequestId);
} 