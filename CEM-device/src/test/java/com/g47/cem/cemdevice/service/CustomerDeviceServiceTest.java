package com.g47.cem.cemdevice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.g47.cem.cemdevice.dto.response.CustomerDeviceResponse;
import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.enums.CustomerDeviceStatus;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.repository.CustomerDeviceRepository;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class CustomerDeviceServiceTest {

    @Mock
    private CustomerDeviceRepository customerDeviceRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerDeviceService customerDeviceService;

    private CustomerDevice customerDevice;
    private Device device;
    private CustomerDeviceResponse customerDeviceResponse;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(1L)
                .name("Test Device")
                .model("Test Model")
                .serialNumber("SN123")
                .price(java.math.BigDecimal.valueOf(1000))
                .unit("piece")
                .build();

        customerDevice = CustomerDevice.builder()
                .id(1L)
                .customerId(1L)
                .device(device)
                .warrantyEnd(LocalDate.now().plusMonths(12))
                .status(CustomerDeviceStatus.ACTIVE)
                .build();

        customerDeviceResponse = new CustomerDeviceResponse();
        customerDeviceResponse.setId(1L);
        customerDeviceResponse.setCustomerId(1L);
        customerDeviceResponse.setDeviceId(1L);
        customerDeviceResponse.setDeviceName("Test Device");
        customerDeviceResponse.setDeviceModel("Test Model");
        customerDeviceResponse.setSerialNumber("SN123");
        customerDeviceResponse.setStatus(CustomerDeviceStatus.ACTIVE);
    }

    @Test
    void testGetCustomerPurchasedDevices() {
        // Given
        Long customerId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        List<CustomerDevice> customerDevices = Arrays.asList(customerDevice);
        Page<CustomerDevice> customerDevicePage = new PageImpl<>(customerDevices, pageable, 1);

        when(customerDeviceRepository.findByCustomerId(customerId, pageable))
                .thenReturn(customerDevicePage);

        // When
        Page<CustomerDeviceResponse> result = customerDeviceService.getCustomerPurchasedDevices(
                customerId, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerDeviceRepository).findByCustomerId(customerId, pageable);
    }

    @Test
    void testGetCustomerDeviceById() {
        // Given
        Long customerId = 1L;
        Long deviceId = 1L;

        when(customerDeviceRepository.findById(deviceId))
                .thenReturn(Optional.of(customerDevice));

        // When
        CustomerDeviceResponse result = customerDeviceService.getCustomerDeviceById(customerId, deviceId);

        // Then
        assertNotNull(result);
        assertEquals(deviceId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        verify(customerDeviceRepository).findById(deviceId);
    }

    @Test
    void testGetCustomerDeviceById_NotFound() {
        // Given
        Long customerId = 1L;
        Long deviceId = 1L;

        when(customerDeviceRepository.findById(deviceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            customerDeviceService.getCustomerDeviceById(customerId, deviceId);
        });
        verify(customerDeviceRepository).findById(deviceId);
    }

    @Test
    void testGetCustomerDeviceById_WrongCustomer() {
        // Given
        Long customerId = 1L;
        Long deviceId = 1L;
        CustomerDevice wrongCustomerDevice = CustomerDevice.builder()
                .id(1L)
                .customerId(2L) // Different customer
                .device(device)
                .build();

        when(customerDeviceRepository.findById(deviceId))
                .thenReturn(Optional.of(wrongCustomerDevice));

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            customerDeviceService.getCustomerDeviceById(customerId, deviceId);
        });
        verify(customerDeviceRepository).findById(deviceId);
    }

    @Test
    void testGetCustomerDeviceStatistics() {
        // Given
        Long customerId = 1L;

        when(customerDeviceRepository.countByCustomerId(customerId)).thenReturn(5L);
        when(customerDeviceRepository.countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.ACTIVE)).thenReturn(3L);
        when(customerDeviceRepository.countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.INACTIVE)).thenReturn(1L);
        when(customerDeviceRepository.countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.ERROR)).thenReturn(1L);
        when(customerDeviceRepository.findExpiredWarranties(any(LocalDate.class)))
                .thenReturn(Arrays.asList(customerDevice));

        // When
        CustomerDeviceService.CustomerDeviceStatistics result = customerDeviceService.getCustomerDeviceStatistics(customerId);

        // Then
        assertNotNull(result);
        assertEquals(5L, result.getTotalDevices());
        assertEquals(3L, result.getActiveDevices());
        assertEquals(1L, result.getMaintenanceDevices());
        assertEquals(1L, result.getBrokenDevices());
        assertEquals(1L, result.getExpiredWarrantyDevices());

        verify(customerDeviceRepository).countByCustomerId(customerId);
        verify(customerDeviceRepository).countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.ACTIVE);
        verify(customerDeviceRepository).countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.INACTIVE);
        verify(customerDeviceRepository).countByCustomerIdAndStatus(customerId, CustomerDeviceStatus.ERROR);
        verify(customerDeviceRepository).findExpiredWarranties(any(LocalDate.class));
    }
} 