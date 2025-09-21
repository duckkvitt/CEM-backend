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

    /**
     * Search devices with flexible filters for keyword, stock status, and device status.
     * Optimized to only use LEFT JOIN when inStock filter is applied.
     */
    @Query("SELECT d FROM Device d " +
           "WHERE (:keyword IS NULL OR d.serialNumber LIKE :keyword OR d.name LIKE :keyword OR d.model LIKE :keyword) " +
           "AND (:status IS NULL OR d.status = :status)")
    Page<Device> searchDevicesBasic(@Param("keyword") String keyword,
                                    @Param("status") DeviceStatus status,
                                    Pageable pageable);

    /**
     * Search devices with inStock filter (requires LEFT JOIN)
     */
    @Query("SELECT d FROM Device d LEFT JOIN d.customerDevices cd " +
           "WHERE (:keyword IS NULL OR d.serialNumber LIKE :keyword OR d.name LIKE :keyword OR d.model LIKE :keyword) " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND ((:inStock = true AND cd.id IS NULL) OR (:inStock = false AND cd.id IS NOT NULL))")
    Page<Device> searchDevicesWithStockFilter(@Param("keyword") String keyword,
                                              @Param("inStock") Boolean inStock,
                                              @Param("status") DeviceStatus status,
                                              Pageable pageable);

    /**
     * Check if device is linked to any customer through contracts
     */
    @Query("SELECT COUNT(cd) > 0 FROM CustomerDevice cd WHERE cd.device.id = :deviceId")
    boolean isDeviceLinkedToCustomer(@Param("deviceId") Long deviceId);

    /**
     * Check if device has any tasks assigned
     */
    @Query("SELECT COUNT(t) > 0 FROM Task t JOIN t.customerDevice cd WHERE cd.device.id = :deviceId")
    boolean hasDeviceTasks(@Param("deviceId") Long deviceId);

    /**
     * Check if device has any notes
     */
    @Query("SELECT COUNT(dn) > 0 FROM DeviceNote dn WHERE dn.device.id = :deviceId")
    boolean hasDeviceNotes(@Param("deviceId") Long deviceId);
} 