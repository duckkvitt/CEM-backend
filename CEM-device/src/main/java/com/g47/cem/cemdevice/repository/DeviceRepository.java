package com.g47.cem.cemdevice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.enums.DeviceStatus;

/**
 * Repository interface for Device entity
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    
    Optional<Device> findBySerialNumber(String serialNumber);
    
    boolean existsBySerialNumber(String serialNumber);
    
    List<Device> findByCustomerId(Long customerId);
    
    Page<Device> findByStatus(DeviceStatus status, Pageable pageable);
    
    @Query("SELECT d FROM Device d WHERE " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:model IS NULL OR LOWER(d.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
           "(:serialNumber IS NULL OR d.serialNumber LIKE CONCAT('%', :serialNumber, '%')) AND " +
           "(:customerId IS NULL OR d.customerId = :customerId) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<Device> findDevicesWithFilters(
            @Param("name") String name,
            @Param("model") String model,
            @Param("serialNumber") String serialNumber,
            @Param("customerId") Long customerId,
            @Param("status") DeviceStatus status,
            Pageable pageable);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    long countByStatus(@Param("status") DeviceStatus status);
    
    @Query("SELECT d FROM Device d WHERE d.warrantyExpiry IS NOT NULL AND d.warrantyExpiry < CURRENT_DATE")
    Page<Device> findExpiredWarrantyDevices(Pageable pageable);

    /**
     * Search devices by a generic keyword that matches name, model or serial number (case-insensitive)
     */
    @Query("SELECT d FROM Device d WHERE " +
           "(:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.model) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR d.serialNumber LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:customerId IS NULL OR d.customerId = :customerId) " +
           "AND (:status IS NULL OR d.status = :status)")
    Page<Device> searchByKeyword(@Param("keyword") String keyword,
                                 @Param("customerId") Long customerId,
                                 @Param("status") DeviceStatus status,
                                 Pageable pageable);
} 