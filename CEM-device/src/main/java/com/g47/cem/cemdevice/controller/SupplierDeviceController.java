package com.g47.cem.cemdevice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.DeviceResponse;
import com.g47.cem.cemdevice.service.SupplierDeviceMappingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/devices/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Supplier Devices", description = "Manage devices provided by suppliers")
public class SupplierDeviceController {

    private final SupplierDeviceMappingService mappingService;

    @GetMapping("/{supplierId}/devices")
    @PreAuthorize("hasAnyAuthority('STAFF','MANAGER')")
    @Operation(summary = "List devices provided by supplier")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> listDevices(@PathVariable Long supplierId) {
        var devices = mappingService.listDevicesBySupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success(devices));
    }

    @PostMapping("/{supplierId}/devices")
    @PreAuthorize("hasAnyAuthority('STAFF','MANAGER')")
    @Operation(summary = "Replace supplier devices with provided set")
    public ResponseEntity<ApiResponse<String>> replaceDevices(@PathVariable Long supplierId, @RequestBody ReplaceRequest request) {
        mappingService.replaceSupplierDevices(supplierId, request.getDeviceIds());
        return ResponseEntity.ok(ApiResponse.success("Updated supplier devices"));
    }

    @DeleteMapping("/{supplierId}/devices/{deviceId}")
    @PreAuthorize("hasAnyAuthority('STAFF','MANAGER')")
    @Operation(summary = "Unlink a device from supplier")
    public ResponseEntity<ApiResponse<String>> unlink(@PathVariable Long supplierId, @PathVariable Long deviceId) {
        mappingService.unlinkDevice(supplierId, deviceId);
        return ResponseEntity.ok(ApiResponse.success("Unlinked device"));
    }

    @Data
    public static class ReplaceRequest {
        @NotEmpty
        private List<Long> deviceIds;
    }
}



