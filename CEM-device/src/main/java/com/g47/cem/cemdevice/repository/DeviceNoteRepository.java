package com.g47.cem.cemdevice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.DeviceNote;

/**
 * Repository interface for DeviceNote entity
 */
@Repository
public interface DeviceNoteRepository extends JpaRepository<DeviceNote, Long> {
    
    List<DeviceNote> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
    
    Page<DeviceNote> findByDeviceId(Long deviceId, Pageable pageable);
    
    List<DeviceNote> findByCreatedBy(String createdBy);
    
    @Query("SELECT dn FROM DeviceNote dn WHERE dn.device.id = :deviceId AND " +
           "LOWER(dn.note) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DeviceNote> findByDeviceIdAndNoteContaining(
            @Param("deviceId") Long deviceId, 
            @Param("keyword") String keyword);
    
    long countByDeviceId(Long deviceId);
    
    void deleteByDeviceId(Long deviceId);
} 