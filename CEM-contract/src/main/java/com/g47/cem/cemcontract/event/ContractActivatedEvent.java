package com.g47.cem.cemcontract.event;

import java.util.List;

import com.g47.cem.cemcontract.entity.Contract;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a contract becomes active
 */
@Getter
public class ContractActivatedEvent extends ApplicationEvent {
    
    private final Contract contract;
    private final List<DeviceInfo> deviceInfos;
    
    public ContractActivatedEvent(Object source, Contract contract, List<DeviceInfo> deviceInfos) {
        super(source);
        this.contract = contract;
        this.deviceInfos = deviceInfos;
    }
    
    /**
     * Device information for linking
     */
    public static class DeviceInfo {
        private Long deviceId;
        private Integer quantity;
        private Integer warrantyMonths;
        
        public DeviceInfo() {}
        
        public DeviceInfo(Long deviceId, Integer quantity, Integer warrantyMonths) {
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