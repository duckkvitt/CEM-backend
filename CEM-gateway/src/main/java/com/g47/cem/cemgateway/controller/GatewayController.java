package com.g47.cem.cemgateway.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/gateway")
@Slf4j
@Tag(name = "Gateway", description = "Gateway management APIs")
public class GatewayController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/health")
    @Operation(summary = "Gateway health check", description = "Check if gateway service is running")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", applicationName);
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Gateway service is running");
        
        log.debug("Gateway health check requested");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Gateway information", description = "Get gateway service information")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", applicationName);
        response.put("version", "1.0.0");
        response.put("description", "CEM API Gateway Service");
        response.put("timestamp", LocalDateTime.now());
        
        Map<String, Object> routes = new HashMap<>();
        routes.put("auth-service", "http://localhost:8081");
        // Add more routes as services are added
        response.put("routes", routes);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Gateway status", description = "Get detailed gateway status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", applicationName);
        response.put("uptime", LocalDateTime.now());
        response.put("status", "HEALTHY");
        
        // Add circuit breaker status
        Map<String, String> circuitBreakers = new HashMap<>();
        circuitBreakers.put("auth-service", "CLOSED");
        response.put("circuitBreakers", circuitBreakers);
        
        return ResponseEntity.ok(response);
    }
} 