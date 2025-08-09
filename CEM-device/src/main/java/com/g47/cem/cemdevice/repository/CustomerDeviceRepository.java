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

    Page<CustomerDevice> findByCustomerIdAndContractId(Long customerId, Long contractId, Pageable pageable);

    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.warrantyEnd < :date")
    List<CustomerDevice> findExpiredWarranties(@Param("date") LocalDate date);

    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.warrantyEnd BETWEEN :startDate AND :endDate")
    List<CustomerDevice> findWarrantiesExpiringBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(cd) FROM CustomerDevice cd WHERE cd.customerId = :customerId AND cd.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") CustomerDeviceStatus status);

    boolean existsByCustomerIdAndDeviceId(Long customerId, Long deviceId);
    
    /**
     * Find all devices of a specific type for a customer
     */
    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.customerId = :customerId AND cd.device.id = :deviceId")
    List<CustomerDevice> findByCustomerIdAndDeviceId(@Param("customerId") Long customerId, @Param("deviceId") Long deviceId);
    
    /**
     * Count devices of a specific type for a customer
     */
    @Query("SELECT COUNT(cd) FROM CustomerDevice cd WHERE cd.customerId = :customerId AND cd.device.id = :deviceId")
    long countByCustomerIdAndDeviceId(@Param("customerId") Long customerId, @Param("deviceId") Long deviceId);
    
    /**
     * Count total devices for a customer
     */
    @Query("SELECT COUNT(cd) FROM CustomerDevice cd WHERE cd.customerId = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);
    
    /**
     * Find customer devices by status with pagination
     */
    Page<CustomerDevice> findByCustomerIdAndStatus(Long customerId, CustomerDeviceStatus status, Pageable pageable);
    
    /**
     * Find customer devices with expired warranty
     */
    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.customerId = :customerId AND cd.warrantyEnd < :date")
    Page<CustomerDevice> findByCustomerIdAndWarrantyExpired(@Param("customerId") Long customerId, @Param("date") LocalDate date, Pageable pageable);
    
    /**
     * Search customer devices by device information (name, model, serial number)
     */
    @Query("SELECT cd FROM CustomerDevice cd WHERE cd.customerId = :customerId AND " +
           "(LOWER(cd.device.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cd.device.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(cd.device.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CustomerDevice> findByCustomerIdAndDeviceInfoContaining(@Param("customerId") Long customerId, @Param("keyword") String keyword, Pageable pageable);

    boolean existsByCustomerDeviceCode(String customerDeviceCode);
} 