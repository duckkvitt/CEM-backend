package com.g47.cem.cemspareparts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemspareparts.dto.request.CreateSparePartRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSparePartRequest;
import com.g47.cem.cemspareparts.dto.response.ApiResponse;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartResponse;
import com.g47.cem.cemspareparts.service.SparePartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/spare-parts")
@RequiredArgsConstructor
@Tag(name = "Spare Part Management", description = "APIs for managing spare parts")
public class SparePartController {

    private final SparePartService sparePartService;

    @PostMapping
    @Operation(summary = "Create a new spare part")
    public ResponseEntity<ApiResponse<SparePartResponse>> createSparePart(@Valid @RequestBody CreateSparePartRequest request) {
        SparePartResponse response = sparePartService.createSparePart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Spare part created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get spare part by ID")
    public ResponseEntity<ApiResponse<SparePartResponse>> getSparePartById(@PathVariable Long id) {
        SparePartResponse response = sparePartService.getSparePartById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all spare parts")
    public ResponseEntity<ApiResponse<PagedResponse<SparePartResponse>>> getAllSpareParts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword) {
        PagedResponse<SparePartResponse> response = sparePartService.getAllSpareParts(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing spare part")
    public ResponseEntity<ApiResponse<SparePartResponse>> updateSparePart(@PathVariable Long id, @Valid @RequestBody UpdateSparePartRequest request) {
        SparePartResponse response = sparePartService.updateSparePart(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Spare part updated successfully"));
    }

    @PatchMapping("/{id}/hide")
    @Operation(summary = "Hide a spare part")
    public ResponseEntity<ApiResponse<String>> hideSparePart(@PathVariable Long id) {
        sparePartService.hideSparePart(id);
        return ResponseEntity.ok(ApiResponse.success("Spare part hidden successfully"));
    }
} 