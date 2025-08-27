package com.g47.cem.cemdevice.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.response.DeviceResponse;
import com.g47.cem.cemdevice.entity.Device;
import com.g47.cem.cemdevice.entity.SupplierDevice;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.repository.DeviceRepository;
import com.g47.cem.cemdevice.repository.SupplierDeviceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierDeviceMappingService {

    private final SupplierDeviceRepository supplierDeviceRepository;
    private final DeviceRepository deviceRepository;
    private final org.modelmapper.ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<DeviceResponse> listDevicesBySupplier(Long supplierId) {
        return supplierDeviceRepository
                .findDevicesSuppliedBySupplier(supplierId, PageRequest.of(0, 1000))
                .map(SupplierDevice::getDevice)
                .map(d -> modelMapper.map(d, DeviceResponse.class))
                .getContent();
    }

    public void replaceSupplierDevices(Long supplierId, List<Long> deviceIds) {
        log.info("Replacing devices for supplier {} with {} device ids", supplierId, deviceIds.size());
        // Remove existing
        List<SupplierDevice> existing = supplierDeviceRepository.findBySupplierId(supplierId);
        supplierDeviceRepository.deleteAll(existing);

        if (deviceIds == null || deviceIds.isEmpty()) return;

        Set<Long> unique = deviceIds.stream().collect(Collectors.toSet());
        List<Device> devices = deviceRepository.findAllById(unique);
        if (devices.size() != unique.size()) {
            throw new ResourceNotFoundException("Device", "ids", unique);
        }

        List<SupplierDevice> toSave = devices.stream()
                .map(d -> SupplierDevice.builder()
                        .supplierId(supplierId)
                        .device(d)
                        .isPrimarySupplier(false)
                        .build())
                .toList();
        supplierDeviceRepository.saveAll(toSave);
    }

    public void unlinkDevice(Long supplierId, Long deviceId) {
        supplierDeviceRepository.findBySupplierIdAndDeviceId(supplierId, deviceId)
                .ifPresent(supplierDeviceRepository::delete);
    }
}


