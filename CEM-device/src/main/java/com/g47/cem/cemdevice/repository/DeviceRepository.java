package com.g47.cem.cemdevice.repository;

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
    
    Page<Device> findByStatus(DeviceStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = :status")
    long countByStatus(@Param("status") DeviceStatus status);
    
    @Query("SELECT d FROM Device d WHERE d.warrantyExpiry IS NOT NULL AND d.warrantyExpiry < CURRENT_DATE")
    Page<Device> findExpiredWarrantyDevices(Pageable pageable);

    /**
     * Search devices with flexible filters for keyword, stock status, and device status.
     */
    @Query("SELECT d FROM Device d WHERE " +
           "(:keyword IS NULL OR d.serialNumber LIKE :keyword OR d.name LIKE :keyword OR d.model LIKE :keyword) " +
           "AND (:status IS NULL OR d.status = :status) " +
           // Nếu inStock là true, chỉ lấy device không có trong bảng customer_devices
           "AND (:inStock IS NULL OR " +
           "     (:inStock = true AND NOT EXISTS (SELECT 1 FROM CustomerDevice cd WHERE cd.device = d)) OR " +
           "     (:inStock = false AND EXISTS (SELECT 1 FROM CustomerDevice cd WHERE cd.device = d))" +
           ")")
    Page<Device> searchDevices(@Param("keyword") String keyword,
                               @Param("inStock") Boolean inStock,
                               @Param("status") DeviceStatus status,
                               Pageable pageable);
} 