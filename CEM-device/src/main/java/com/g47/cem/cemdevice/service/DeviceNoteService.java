package com.g47.cem.cemdevice.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.request.CreateDeviceNoteRequest;
import com.g47.cem.cemdevice.dto.request.UpdateDeviceNoteRequest;
import com.g47.cem.cemdevice.dto.response.DeviceNoteResponse;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.DeviceNote;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.repository.DeviceNoteRepository;
import com.g47.cem.cemdevice.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for DeviceNote operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceNoteService {
    
    private final DeviceNoteRepository deviceNoteRepository;
    private final DeviceRepository deviceRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new device note
     */
    @Transactional
    public DeviceNoteResponse createDeviceNote(Long deviceId, CreateDeviceNoteRequest request, String createdBy) {
        log.debug("Creating device note for device ID: {}", deviceId);
        
        // Verify device exists
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        
        DeviceNote deviceNote = DeviceNote.builder()
                .device(device)
                .note(request.getNote())
                .createdBy(createdBy)
                .build();
        
        deviceNote = deviceNoteRepository.save(deviceNote);
        
        log.info("Device note created successfully with ID: {} for device: {}", deviceNote.getId(), deviceId);
        return mapToDeviceNoteResponse(deviceNote);
    }
    
    /**
     * Get device note by ID
     */
    @Transactional(readOnly = true)
    public DeviceNoteResponse getDeviceNoteById(Long id) {
        log.debug("Fetching device note with ID: {}", id);
        
        DeviceNote deviceNote = deviceNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceNote", "id", id));
        
        return mapToDeviceNoteResponse(deviceNote);
    }
    
    /**
     * Get all device notes for a specific device
     */
    @Transactional(readOnly = true)
    public List<DeviceNoteResponse> getDeviceNotesByDeviceId(Long deviceId) {
        log.debug("Fetching device notes for device ID: {}", deviceId);
        
        // Verify device exists
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        
        List<DeviceNote> deviceNotes = deviceNoteRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
        return deviceNotes.stream()
                .map(this::mapToDeviceNoteResponse)
                .toList();
    }
    
    /**
     * Get device notes for a device with pagination
     */
    @Transactional(readOnly = true)
    public Page<DeviceNoteResponse> getDeviceNotesByDeviceId(Long deviceId, Pageable pageable) {
        log.debug("Fetching device notes for device ID: {} with pagination", deviceId);
        
        // Verify device exists
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        
        Page<DeviceNote> deviceNotes = deviceNoteRepository.findByDeviceId(deviceId, pageable);
        return deviceNotes.map(this::mapToDeviceNoteResponse);
    }
    
    /**
     * Search device notes by keyword
     */
    @Transactional(readOnly = true)
    public List<DeviceNoteResponse> searchDeviceNotes(Long deviceId, String keyword) {
        log.debug("Searching device notes for device ID: {} with keyword: {}", deviceId, keyword);
        
        // Verify device exists
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        
        List<DeviceNote> deviceNotes = deviceNoteRepository.findByDeviceIdAndNoteContaining(deviceId, keyword);
        return deviceNotes.stream()
                .map(this::mapToDeviceNoteResponse)
                .toList();
    }
    
    /**
     * Get device notes by creator
     */
    @Transactional(readOnly = true)
    public List<DeviceNoteResponse> getDeviceNotesByCreator(String createdBy) {
        log.debug("Fetching device notes created by: {}", createdBy);
        
        List<DeviceNote> deviceNotes = deviceNoteRepository.findByCreatedBy(createdBy);
        return deviceNotes.stream()
                .map(this::mapToDeviceNoteResponse)
                .toList();
    }
    
    /**
     * Update device note
     */
    @Transactional
    public DeviceNoteResponse updateDeviceNote(Long id, UpdateDeviceNoteRequest request) {
        log.debug("Updating device note with ID: {}", id);
        
        DeviceNote deviceNote = deviceNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceNote", "id", id));
        
        deviceNote.setNote(request.getNote());
        deviceNote = deviceNoteRepository.save(deviceNote);
        
        log.info("Device note updated successfully with ID: {}", id);
        return mapToDeviceNoteResponse(deviceNote);
    }
    
    /**
     * Delete device note
     */
    @Transactional
    public void deleteDeviceNote(Long id) {
        log.debug("Deleting device note with ID: {}", id);
        
        DeviceNote deviceNote = deviceNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeviceNote", "id", id));
        
        deviceNoteRepository.delete(deviceNote);
        
        log.info("Device note deleted successfully with ID: {}", id);
    }
    
    /**
     * Get device note count for a device
     */
    @Transactional(readOnly = true)
    public long getDeviceNoteCount(Long deviceId) {
        return deviceNoteRepository.countByDeviceId(deviceId);
    }
    
    /**
     * Delete all device notes for a device
     */
    @Transactional
    public void deleteAllDeviceNotes(Long deviceId) {
        log.debug("Deleting all device notes for device ID: {}", deviceId);
        
        // Verify device exists
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResourceNotFoundException("Device", "id", deviceId);
        }
        
        deviceNoteRepository.deleteByDeviceId(deviceId);
        
        log.info("All device notes deleted for device ID: {}", deviceId);
    }
    
    private DeviceNoteResponse mapToDeviceNoteResponse(DeviceNote deviceNote) {
        DeviceNoteResponse response = modelMapper.map(deviceNote, DeviceNoteResponse.class);
        if (deviceNote.getDevice() != null) {
            response.setDeviceId(deviceNote.getDevice().getId());
        }
        return response;
    }
} 