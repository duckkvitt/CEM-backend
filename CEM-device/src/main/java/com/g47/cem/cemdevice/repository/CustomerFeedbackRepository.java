package com.g47.cem.cemdevice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.g47.cem.cemdevice.entity.CustomerFeedback;

public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long>, JpaSpecificationExecutor<CustomerFeedback> {
    Page<CustomerFeedback> findByCustomerId(Long customerId, Pageable pageable);
    boolean existsByServiceRequestIdAndCustomerId(Long serviceRequestId, Long customerId);
    java.util.Optional<CustomerFeedback> findByServiceRequestIdAndCustomerId(Long serviceRequestId, Long customerId);
}


