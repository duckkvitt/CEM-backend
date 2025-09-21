package com.g47.cem.cemdevice.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.request.CreateDeviceRequest;
import com.g47.cem.cemdevice.dto.request.UpdateDeviceRequest;
import com.g47.cem.cemdevice.dto.response.DeviceResponse;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.enums.DeviceStatus;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Device operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new device
     */
    @Transactional
    public DeviceResponse createDevice(CreateDeviceRequest request, String createdBy) {
        log.debug("Creating device with name: {}", request.getName());
        
        // Check if serial number already exists
        if (request.getSerialNumber() != null && 
            deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new BusinessException("Device with serial number '" + request.getSerialNumber() + "' already exists");
        }
        
        Device device = Device.builder()
                .name(request.getName())
                .model(request.getModel())
                .serialNumber(request.getSerialNumber())
                // customerId đã bị xóa
                .price(request.getPrice())
                .unit(request.getUnit())
                .status(request.getStatus() != null ? request.getStatus() : DeviceStatus.ACTIVE)
                .createdBy(createdBy)
                .build();
        
        device = deviceRepository.save(device);
        
        log.info("Device created successfully with ID: {}", device.getId());
        return mapToDeviceResponse(device);
    }
    
    /**
     * Get device by ID
     */
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        log.debug("Fetching device with ID: {}", id);
        
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        
        return mapToDeviceResponse(device);
    }
    
    /**
     * Get device by serial number
     */
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceBySerialNumber(String serialNumber) {
        log.debug("Fetching device with serial number: {}", serialNumber);
        
        Device device = deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "serialNumber", serialNumber));
        
        return mapToDeviceResponse(device);
    }
    
    /**
     * Get all devices with pagination
     */
    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAllDevices(Pageable pageable) {
        log.debug("Fetching all devices with pagination: {}", pageable);
        
        Page<Device> devices = deviceRepository.findAll(pageable);
        return devices.map(this::mapToDeviceResponse);
    }
    
    /**
     * Search devices with filters.
     * This method combines keyword search, stock status, and device status.
     * Optimized to use appropriate query based on filters.
     */
    @Transactional(readOnly = true)
    public Page<DeviceResponse> searchDevices(String keyword, Boolean inStock, DeviceStatus status, Pageable pageable) {
        log.debug("Searching devices with keyword: {}, inStock: {}, status: {}", keyword, inStock, status);

        String pattern = null;
        if (keyword != null && !keyword.isBlank()) {
            pattern = "%" + keyword.trim() + "%";
        }

        Page<Device> devices;
        
        // Use optimized query based on whether inStock filter is applied
        if (inStock != null) {
            // Use LEFT JOIN query when inStock filter is applied
            Boolean safeInStock = Boolean.TRUE.equals(inStock) ? Boolean.TRUE : Boolean.FALSE;
            devices = deviceRepository.searchDevicesWithStockFilter(pattern, safeInStock, status, pageable);
        } else {
            // Use basic query when no inStock filter (better performance)
            devices = deviceRepository.searchDevicesBasic(pattern, status, pageable);
        }
        
        return devices.map(this::mapToDeviceResponse);
    }
    
    /**
     * Get devices by status
     */
    @Transactional(readOnly = true)
    public Page<DeviceResponse> getDevicesByStatus(DeviceStatus status, Pageable pageable) {
        log.debug("Fetching devices with status: {}", status);
        
        Page<Device> devices = deviceRepository.findByStatus(status, pageable);
        return devices.map(this::mapToDeviceResponse);
    }
    
    // Xóa getDevicesByCustomerId
    
    /**
     * Update device
     */
    @Transactional
    public DeviceResponse updateDevice(Long id, UpdateDeviceRequest request) {
        log.debug("Updating device with ID: {}", id);
        
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        
        // Check if serial number already exists for another device
        if (request.getSerialNumber() != null && 
            !request.getSerialNumber().equals(device.getSerialNumber()) &&
            deviceRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new BusinessException("Device with serial number '" + request.getSerialNumber() + "' already exists");
        }
        
        // Update device fields
        device.setName(request.getName());
        device.setModel(request.getModel());
        device.setSerialNumber(request.getSerialNumber());
        // customerId đã bị xóa
        device.setPrice(request.getPrice());
        device.setUnit(request.getUnit());
        device.setWarrantyExpiry(request.getWarrantyExpiry());
        if (request.getStatus() != null) {
            device.setStatus(request.getStatus());
        }
        
        device = deviceRepository.save(device);
        
        log.info("Device updated successfully with ID: {}", device.getId());
        return mapToDeviceResponse(device);
    }
    
    /**
     * Delete device
     */
    @Transactional
    public void deleteDevice(Long id) {
        log.debug("Deleting device with ID: {}", id);
        
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        
        // Check for constraints before deletion
        validateDeviceDeletion(id);
        
        deviceRepository.delete(device);
        
        log.info("Device deleted successfully with ID: {}", id);
    }
    
    /**
     * Validate if device can be deleted by checking all constraints
     */
    @Transactional(readOnly = true)
    public void validateDeviceDeletion(Long deviceId) {
        log.debug("Validating device deletion for ID: {}", deviceId);
        
        StringBuilder constraintReasons = new StringBuilder();
        
        // Check if device is linked to customers through contracts
        if (deviceRepository.isDeviceLinkedToCustomer(deviceId)) {
            constraintReasons.append("Device is currently linked to customers through contracts. ");
        }
        
        // Check if device has any tasks assigned
        if (deviceRepository.hasDeviceTasks(deviceId)) {
            constraintReasons.append("Device has active or completed tasks assigned. ");
        }
        
        // Check if device has any notes
        if (deviceRepository.hasDeviceNotes(deviceId)) {
            constraintReasons.append("Device has associated notes. ");
        }
        
        // If there are any constraints, throw an exception
        if (constraintReasons.length() > 0) {
            String errorMessage = "Cannot delete device due to: " + constraintReasons.toString().trim();
            log.warn("Device deletion blocked for ID {}: {}", deviceId, errorMessage);
            throw new BusinessException(errorMessage);
        }
        
        log.debug("Device deletion validation passed for ID: {}", deviceId);
    }
    
    /**
     * Update device status
     */
    @Transactional
    public DeviceResponse updateDeviceStatus(Long id, DeviceStatus status) {
        log.debug("Updating device status for ID: {} to status: {}", id, status);
        
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        
        device.setStatus(status);
        device = deviceRepository.save(device);
        
        log.info("Device status updated successfully for ID: {}", id);
        return mapToDeviceResponse(device);
    }
    
    /**
     * Get device count by status
     */
    @Transactional(readOnly = true)
    public long getDeviceCountByStatus(DeviceStatus status) {
        return deviceRepository.countByStatus(status);
    }
    
    // Xóa searchDevicesByKeyword và searchDevices cũ
    
    private DeviceResponse mapToDeviceResponse(Device device) {
        return modelMapper.map(device, DeviceResponse.class);
    }
} 