package com.g47.cem.cemdevice.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.repository.CustomerDeviceRepository;
import com.g47.cem.cemdevice.repository.DeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for linking devices to customers when contracts are completed
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractDeviceLinkService {
    
    private final CustomerDeviceRepository customerDeviceRepository;
    private final DeviceRepository deviceRepository;
    
    /**
     * Link devices from contract details to customer
     * This method should be called when a contract becomes active
     */
    @Transactional
    public void linkDevicesFromContract(Long customerId, List<ContractDeviceInfo> contractDevices) {
        log.info("Linking {} devices to customer ID: {}", contractDevices.size(), customerId);
        
        for (ContractDeviceInfo deviceInfo : contractDevices) {
            try {
                linkDeviceToCustomer(customerId, deviceInfo);
            } catch (Exception e) {
                log.error("Failed to link device {} to customer {}: {}", 
                        deviceInfo.getDeviceId(), customerId, e.getMessage());
                // Continue with other devices even if one fails
            }
        }
        
        log.info("Successfully linked devices to customer ID: {}", customerId);
    }
    
    /**
     * Link a single device to customer
     */
    @Transactional
    public void linkDeviceToCustomer(Long customerId, ContractDeviceInfo deviceInfo) {
        log.debug("Linking device ID: {} to customer ID: {} with quantity: {}", 
                deviceInfo.getDeviceId(), customerId, deviceInfo.getQuantity());
        
        // Check if device exists
        Device device = deviceRepository.findById(deviceInfo.getDeviceId())
                .orElseThrow(() -> new BusinessException("Device not found with ID: " + deviceInfo.getDeviceId()));
        
        // Get quantity (default to 1 if not specified)
        int quantity = deviceInfo.getQuantity() != null ? deviceInfo.getQuantity() : 1;
        
        // Calculate warranty end date
        LocalDate warrantyEnd = calculateWarrantyEndDate(deviceInfo.getWarrantyMonths());
        
        // Create customer device records for each quantity
        for (int i = 0; i < quantity; i++) {
            // Check if we've already linked enough devices of this type to this customer
            long existingCount = customerDeviceRepository.countByCustomerIdAndDeviceId(customerId, deviceInfo.getDeviceId());
            if (existingCount >= quantity) {
                log.warn("Already linked {} devices of type {} to customer {}. Skipping additional links.", 
                        existingCount, deviceInfo.getDeviceId(), customerId);
                break;
            }
            
            // Create customer device record
            CustomerDevice customerDevice = CustomerDevice.builder()
                    .customerId(customerId)
                    .device(device)
                    .warrantyEnd(warrantyEnd)
                    .status(CustomerDeviceStatus.ACTIVE)
                    .build();
            
            customerDeviceRepository.save(customerDevice);
            
            log.debug("Created customer device record {} for device {} to customer {}", 
                    customerDevice.getId(), deviceInfo.getDeviceId(), customerId);
        }
        
        log.info("Successfully linked {} devices of type {} to customer {} with warranty until {}", 
                quantity, deviceInfo.getDeviceId(), customerId, warrantyEnd);
    }
    
    /**
     * Calculate warranty end date based on warranty months
     */
    private LocalDate calculateWarrantyEndDate(Integer warrantyMonths) {
        if (warrantyMonths == null || warrantyMonths <= 0) {
            return null; // No warranty
        }
        
        return LocalDate.now().plusMonths(warrantyMonths);
    }
    
    /**
     * Remove device link from customer (for contract cancellation)
     */
    @Transactional
    public void unlinkDeviceFromCustomer(Long customerId, Long deviceId) {
        log.info("Unlinking device ID: {} from customer ID: {}", deviceId, customerId);
        
        Optional<CustomerDevice> customerDevice = customerDeviceRepository.findById(deviceId);
        
        if (customerDevice.isPresent() && customerDevice.get().getCustomerId().equals(customerId)) {
            customerDeviceRepository.delete(customerDevice.get());
            log.info("Successfully unlinked device {} from customer {}", deviceId, customerId);
        } else {
            log.warn("Device {} is not linked to customer {}. Nothing to unlink.", deviceId, customerId);
        }
    }
    
    /**
     * Unlink all devices of a specific type from customer (for contract cancellation)
     */
    @Transactional
    public void unlinkAllDevicesOfTypeFromCustomer(Long customerId, Long deviceId) {
        log.info("Unlinking all devices of type {} from customer ID: {}", deviceId, customerId);
        
        List<CustomerDevice> customerDevices = customerDeviceRepository.findByCustomerIdAndDeviceId(customerId, deviceId);
        
        if (!customerDevices.isEmpty()) {
            customerDeviceRepository.deleteAll(customerDevices);
            log.info("Successfully unlinked {} devices of type {} from customer {}", 
                    customerDevices.size(), deviceId, customerId);
        } else {
            log.warn("No devices of type {} found for customer {}. Nothing to unlink.", deviceId, customerId);
        }
    }
    
    /**
     * Get all devices linked to a customer
     */
    @Transactional(readOnly = true)
    public List<CustomerDevice> getCustomerDevices(Long customerId) {
        return customerDeviceRepository.findByCustomerId(customerId);
    }
    
    /**
     * Check if a device is linked to a customer
     */
    @Transactional(readOnly = true)
    public boolean isDeviceLinkedToCustomer(Long customerId, Long deviceId) {
        return customerDeviceRepository.existsByCustomerIdAndDeviceId(customerId, deviceId);
    }
    
    /**
     * DTO for contract device information
     */
    public static class ContractDeviceInfo {
        private Long deviceId;
        private Integer quantity;
        private Integer warrantyMonths;
        
        public ContractDeviceInfo() {}
        
        public ContractDeviceInfo(Long deviceId, Integer quantity, Integer warrantyMonths) {
            this.deviceId = deviceId;
            this.quantity = quantity;
            this.warrantyMonths = warrantyMonths;
        }
        
        // Getters and setters
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public Integer getWarrantyMonths() { return warrantyMonths; }
        public void setWarrantyMonths(Integer warrantyMonths) { this.warrantyMonths = warrantyMonths; }
    }
} 