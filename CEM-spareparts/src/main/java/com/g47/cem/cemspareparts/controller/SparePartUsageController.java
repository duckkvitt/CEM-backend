package com.g47.cem.cemspareparts.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemspareparts.dto.request.ExportSparePartForTaskRequest;
import com.g47.cem.cemspareparts.dto.response.ApiResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartUsageResponse;
import com.g47.cem.cemspareparts.service.SparePartUsageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/spare-part-usage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Spare Part Usage", description = "APIs for exporting and listing task spare part usage")
public class SparePartUsageController {

    private final SparePartUsageService usageService;

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasAnyAuthority('STAFF', 'SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'TECHNICIAN')")
    @Operation(summary = "Get usages by task")
    public ResponseEntity<ApiResponse<List<SparePartUsageResponse>>> getUsagesByTask(@PathVariable Long taskId) {
        List<SparePartUsageResponse> data = usageService.getUsagesByTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAnyAuthority('TECHNICIAN')")
    @Operation(summary = "Export spare part for task (decrement stock and record)")
    public ResponseEntity<ApiResponse<SparePartUsageResponse>> export(@Valid @RequestBody ExportSparePartForTaskRequest request, Principal principal) {
        var data = usageService.exportForTask(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}


