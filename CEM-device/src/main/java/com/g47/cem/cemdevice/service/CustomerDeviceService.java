package com.g47.cem.cemdevice.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.response.CustomerDeviceResponse;
import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.repository.CustomerDeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for CustomerDevice operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerDeviceService {
    
    private final CustomerDeviceRepository customerDeviceRepository;
    
    /**
     * Get customer's purchased devices with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<CustomerDeviceResponse> getCustomerPurchasedDevices(
            Long customerId,
            String keyword,
            CustomerDeviceStatus status,
            Boolean warrantyExpired,
            Long contractId,
            Pageable pageable) {
        
        log.debug("Fetching purchased devices for customer: {} with filters - keyword: {}, status: {}, warrantyExpired: {}", 
                customerId, keyword, status, warrantyExpired);
        
        Page<CustomerDevice> customerDevices;
        
        if (contractId != null) {
            customerDevices = customerDeviceRepository.findByCustomerIdAndContractId(customerId, contractId, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Search by device name, model, or serial number
            customerDevices = customerDeviceRepository.findByCustomerIdAndDeviceInfoContaining(
                    customerId, keyword.trim(), pageable);
        } else if (status != null) {
            // Filter by status
            customerDevices = customerDeviceRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        } else if (warrantyExpired != null && warrantyExpired) {
            // Filter by expired warranty
            customerDevices = customerDeviceRepository.findByCustomerIdAndWarrantyExpired(customerId, LocalDate.now(), pageable);
        } else {
            // Get all customer devices
            customerDevices = customerDeviceRepository.findByCustomerId(customerId, pageable);
        }
        
        return customerDevices.map(this::mapToCustomerDeviceResponse);
    }

    /**
     * Get customer devices for staff by explicit customerId (no principal binding)
     */
    @Transactional(readOnly = true)
    public Page<CustomerDeviceResponse> getCustomerDevicesForStaff(Long customerId, Pageable pageable) {
        Page<CustomerDevice> customerDevices = customerDeviceRepository.findByCustomerId(customerId, pageable);
        return customerDevices.map(this::mapToCustomerDeviceResponse);
    }
    
    /**
     * Get customer device by ID
     */
    @Transactional(readOnly = true)
    public CustomerDeviceResponse getCustomerDeviceById(Long customerId, Long deviceId) {
        log.debug("Fetching customer device with ID: {} for customer: {}", deviceId, customerId);
        
        CustomerDevice customerDevice = customerDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerDevice", "id", deviceId));
        
        // Verify the device belongs to the customer
        if (!customerDevice.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("CustomerDevice", "id", deviceId);
        }
        
        return mapToCustomerDeviceResponse(customerDevice);
    }
    
    /**
     * Get customer device statistics
     */
    @Transactional(readOnly = true)
    public CustomerDeviceStatistics getCustomerDeviceStatistics(Long customerId) {
        log.debug("Fetching device statistics for customer: {}", customerId);
        
        long totalDevices = customerDeviceRepository.countByCustomerId(customerId);
        long activeDevices = customerDeviceRepository.countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.ACTIVE);
        long maintenanceDevices = customerDeviceRepository.countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.INACTIVE);
        long brokenDevices = customerDeviceRepository.countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.ERROR);
        
        // Count devices with expired warranty
        List<CustomerDevice> expiredWarrantyDevices = customerDeviceRepository.findExpiredWarranties(LocalDate.now());
        long expiredWarrantyCount = expiredWarrantyDevices.stream()
                .filter(cd -> cd.getCustomerId().equals(customerId))
                .count();
        
        return CustomerDeviceStatistics.builder()
                .totalDevices(totalDevices)
                .activeDevices(activeDevices)
                .maintenanceDevices(maintenanceDevices)
                .brokenDevices(brokenDevices)
                .expiredWarrantyDevices(expiredWarrantyCount)
                .build();
    }
    
    /**
     * Get devices with expiring warranty (within next 30 days)
     */
    @Transactional(readOnly = true)
    public List<CustomerDeviceResponse> getDevicesWithExpiringWarranty(Long customerId) {
        log.debug("Fetching devices with expiring warranty for customer: {}", customerId);
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        
        List<CustomerDevice> expiringDevices = customerDeviceRepository.findWarrantiesExpiringBetween(startDate, endDate);
        
        return expiringDevices.stream()
                .filter(cd -> cd.getCustomerId().equals(customerId))
                .map(this::mapToCustomerDeviceResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map CustomerDevice entity to CustomerDeviceResponse DTO
     */
    private CustomerDeviceResponse mapToCustomerDeviceResponse(CustomerDevice customerDevice) {
        CustomerDeviceResponse response = new CustomerDeviceResponse();
        
        // Map device information
        if (customerDevice.getDevice() != null) {
            response.setDeviceId(customerDevice.getDevice().getId());
            response.setDeviceName(customerDevice.getDevice().getName());
            response.setDeviceModel(customerDevice.getDevice().getModel());
            response.setSerialNumber(customerDevice.getDevice().getSerialNumber());
            response.setDevicePrice(customerDevice.getDevice().getPrice());
            response.setDeviceUnit(customerDevice.getDevice().getUnit());
        }
        
        // Map customer device information
        response.setId(customerDevice.getId());
        response.setCustomerId(customerDevice.getCustomerId());
        response.setContractId(customerDevice.getContractId());
        response.setWarrantyEnd(customerDevice.getWarrantyEnd());
        response.setStatus(customerDevice.getStatus());
        response.setCreatedAt(customerDevice.getCreatedAt());
        response.setUpdatedAt(customerDevice.getUpdatedAt());
        response.setCustomerDeviceCode(customerDevice.getCustomerDeviceCode());
        
        // Calculate warranty status
        if (customerDevice.getWarrantyEnd() != null) {
            response.setWarrantyExpired(customerDevice.getWarrantyEnd().isBefore(LocalDate.now()));
            response.setWarrantyExpiringSoon(customerDevice.getWarrantyEnd().isBefore(LocalDate.now().plusDays(30)) 
                    && !customerDevice.getWarrantyEnd().isBefore(LocalDate.now()));
        }
        
        return response;
    }
    
    /**
     * Statistics class for customer devices
     */
    public static class CustomerDeviceStatistics {
        private long totalDevices;
        private long activeDevices;
        private long maintenanceDevices;
        private long brokenDevices;
        private long expiredWarrantyDevices;
        
        // Builder pattern
        public static CustomerDeviceStatisticsBuilder builder() {
            return new CustomerDeviceStatisticsBuilder();
        }
        
        public static class CustomerDeviceStatisticsBuilder {
            private final CustomerDeviceStatistics statistics = new CustomerDeviceStatistics();
            
            public CustomerDeviceStatisticsBuilder totalDevices(long totalDevices) {
                statistics.totalDevices = totalDevices;
                return this;
            }
            
            public CustomerDeviceStatisticsBuilder activeDevices(long activeDevices) {
                statistics.activeDevices = activeDevices;
                return this;
            }
            
            public CustomerDeviceStatisticsBuilder maintenanceDevices(long maintenanceDevices) {
                statistics.maintenanceDevices = maintenanceDevices;
                return this;
            }
            
            public CustomerDeviceStatisticsBuilder brokenDevices(long brokenDevices) {
                statistics.brokenDevices = brokenDevices;
                return this;
            }
            
            public CustomerDeviceStatisticsBuilder expiredWarrantyDevices(long expiredWarrantyDevices) {
                statistics.expiredWarrantyDevices = expiredWarrantyDevices;
                return this;
            }
            
            public CustomerDeviceStatistics build() {
                return statistics;
            }
        }
        
        // Getters
        public long getTotalDevices() { return totalDevices; }
        public long getActiveDevices() { return activeDevices; }
        public long getMaintenanceDevices() { return maintenanceDevices; }
        public long getBrokenDevices() { return brokenDevices; }
        public long getExpiredWarrantyDevices() { return expiredWarrantyDevices; }
    }
} 