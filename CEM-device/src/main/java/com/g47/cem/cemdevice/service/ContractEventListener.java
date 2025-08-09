package com.g47.cem.cemdevice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.g47.cem.cemdevice.event.ContractActivatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractEventListener {

    private final ContractDeviceLinkService contractDeviceLinkService;

    @EventListener
    public void handleContractActivatedEvent(ContractActivatedEvent event) {
        log.info("Handling ContractActivatedEvent for contract ID: {} and customer ID: {}", 
                event.getContractId(), event.getCustomerId());

        try {
            // Convert event device infos to service format
            List<ContractDeviceLinkService.ContractDeviceInfo> deviceInfos = event.getDeviceInfos().stream()
                    .map(deviceInfo -> new ContractDeviceLinkService.ContractDeviceInfo(
                            deviceInfo.getDeviceId(),
                            deviceInfo.getQuantity(),
                            deviceInfo.getWarrantyMonths()))
                    .collect(Collectors.toList());

            log.info("Linking {} devices to customer {} for contract {}", 
                    deviceInfos.size(), event.getCustomerId(), event.getContractId());

            // Link devices to customer with contract context
            contractDeviceLinkService.linkDevicesFromContract(event.getContractId(), event.getCustomerId(), deviceInfos);

            log.info("Successfully linked {} devices to customer {} for contract {}", 
                    deviceInfos.size(), event.getCustomerId(), event.getContractId());

        } catch (Exception e) {
            log.error("Failed to handle ContractActivatedEvent for contract {} and customer {}: {}", 
                    event.getContractId(), event.getCustomerId(), e.getMessage(), e);
        }
    }
} 