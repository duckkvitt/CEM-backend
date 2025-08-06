package com.g47.cem.cemdevice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Fallback controller to handle incorrect API path requests
 */
@RestController
@Slf4j
public class ServiceRequestFallbackController {
    
    /**
     * Fallback endpoint for incorrect API calls to /service-requests
     */
    @RequestMapping("/service-requests")
    public ResponseEntity<ApiResponse<String>> handleIncorrectPath() {
        log.warn("Incorrect API path accessed: /service-requests. Use /api/service-requests instead.");
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header("Location", "/api/service-requests")
                .body(ApiResponse.error("Please use the correct API path: /api/service-requests", 
                        HttpStatus.MOVED_PERMANENTLY.value()));
    }
} 