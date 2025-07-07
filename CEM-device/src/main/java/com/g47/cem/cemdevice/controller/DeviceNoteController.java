package com.g47.cem.cemdevice.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.request.CreateDeviceNoteRequest;
import com.g47.cem.cemdevice.dto.request.UpdateDeviceNoteRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.DeviceNoteResponse;
import com.g47.cem.cemdevice.service.DeviceNoteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Device Note operations
 */
@RestController
@RequestMapping("/devices/{deviceId}/notes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Device Notes", description = "APIs for managing device notes")
public class DeviceNoteController {
    
    private final DeviceNoteService deviceNoteService;
    
    /**
     * Add a note to a device (Staff only)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('STAFF')")
    @Operation(summary = "Add device note", description = "Adds a note to a device (Staff access required)")
    public ResponseEntity<ApiResponse<DeviceNoteResponse>> addDeviceNote(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Valid @RequestBody CreateDeviceNoteRequest request,
            Principal principal) {
        
        log.info("Adding note to device ID: {} by user: {}", deviceId, principal.getName());
        
        DeviceNoteResponse response = deviceNoteService.createDeviceNote(deviceId, request, principal.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Device note added successfully"));
    }
    
    /**
     * Get device note by ID
     */
    @GetMapping("/{noteId}")
    @PreAuthorize("hasAuthority('STAFF') or hasAuthority('MANAGER') or hasAuthority('SUPPORT_TEAM') or hasAuthority('TECH_LEAD') or hasAuthority('TECHNICIAN')")
    @Operation(summary = "Get device note by ID", description = "Retrieves a specific device note")
    public ResponseEntity<ApiResponse<DeviceNoteResponse>> getDeviceNoteById(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Note ID") @PathVariable Long noteId) {
        
        log.debug("Fetching device note with ID: {} for device: {}", noteId, deviceId);
        
        DeviceNoteResponse response = deviceNoteService.getDeviceNoteById(noteId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all notes for a device
     */
    @GetMapping
    @PreAuthorize("hasAuthority('STAFF') or hasAuthority('MANAGER') or hasAuthority('SUPPORT_TEAM') or hasAuthority('TECH_LEAD') or hasAuthority('TECHNICIAN')")
    @Operation(summary = "Get device notes", description = "Retrieves all notes for a device")
    public ResponseEntity<ApiResponse<Object>> getDeviceNotes(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Use pagination") @RequestParam(defaultValue = "false") boolean paginated,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Fetching notes for device ID: {}", deviceId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Search notes by keyword
            List<DeviceNoteResponse> notes = deviceNoteService.searchDeviceNotes(deviceId, keyword.trim());
            return ResponseEntity.ok(ApiResponse.success(notes, "Device notes retrieved successfully"));
        } else if (paginated) {
            // Get paginated notes
            Page<DeviceNoteResponse> notes = deviceNoteService.getDeviceNotesByDeviceId(deviceId, pageable);
            return ResponseEntity.ok(ApiResponse.success(notes, "Device notes retrieved successfully"));
        } else {
            // Get all notes
            List<DeviceNoteResponse> notes = deviceNoteService.getDeviceNotesByDeviceId(deviceId);
            return ResponseEntity.ok(ApiResponse.success(notes, "Device notes retrieved successfully"));
        }
    }
    
    /**
     * Update device note (Staff only)
     */
    @PutMapping("/{noteId}")
    @PreAuthorize("hasAuthority('STAFF')")
    @Operation(summary = "Update device note", description = "Updates a device note (Staff access required)")
    public ResponseEntity<ApiResponse<DeviceNoteResponse>> updateDeviceNote(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Note ID") @PathVariable Long noteId,
            @Valid @RequestBody UpdateDeviceNoteRequest request,
            Principal principal) {
        
        log.info("Updating device note ID: {} for device: {} by user: {}", noteId, deviceId, principal.getName());
        
        DeviceNoteResponse response = deviceNoteService.updateDeviceNote(noteId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Device note updated successfully"));
    }
    
    /**
     * Delete device note (Staff only)
     */
    @DeleteMapping("/{noteId}")
    @PreAuthorize("hasAuthority('STAFF')")
    @Operation(summary = "Delete device note", description = "Deletes a device note (Staff access required)")
    public ResponseEntity<ApiResponse<String>> deleteDeviceNote(
            @Parameter(description = "Device ID") @PathVariable Long deviceId,
            @Parameter(description = "Note ID") @PathVariable Long noteId,
            Principal principal) {
        
        log.info("Deleting device note ID: {} for device: {} by user: {}", noteId, deviceId, principal.getName());
        
        deviceNoteService.deleteDeviceNote(noteId);
        
        return ResponseEntity.ok(ApiResponse.success("Device note deleted successfully"));
    }
} 