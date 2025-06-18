package com.g47.cem.cemdevice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;

/**
 * Repository interface for CustomerDevice entity
 */
@Repository
public interface CustomerDeviceRepository extends JpaRepository<CustomerDevice, Long> {
    
    List<CustomerDevice> findByCustomerId(Long customerId);
    
    List<CustomerDevice> findByDeviceId(Long deviceId);
    
    Page<CustomerDevice> findByCustomerId(Long customerId, Pageable pageable);
    
    Page<CustomerDevice> findByStatus(CustomerDeviceStatus status, Pageable pageable);
    
    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.warrantyEnd < :date")
    List<CustomerDevice> findExpiredWarranties(@Param("date") LocalDate date);
    
    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.warrantyEnd BETWEEN :startDate AND :endDate")
    List<CustomerDevice> findWarrantiesExpiringBetween(
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(cd) FROM CustomerDevice cd WHERE cd.customerId = :customerId AND cd.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") CustomerDeviceStatus status);
    
    boolean existsByCustomerIdAndDeviceId(Long customerId, Long deviceId);
} 