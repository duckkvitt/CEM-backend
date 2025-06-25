package com.g47.cem.cemgateway.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "CEM Gateway is running");
        response.put("timestamp", System.currentTimeMillis());
        
        Map<String, String> services = new HashMap<>();
        services.put("auth-service", "http://localhost:8081/api/auth");
        services.put("customer-service", "http://localhost:8082/api/customer");
        services.put("device-service", "http://localhost:8083/api/device");
        response.put("services", services);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "CEM Gateway Service");
        response.put("version", "1.0.0");
        response.put("description", "API Gateway for CEM microservices architecture");
        
        Map<String, String> routes = new HashMap<>();
        routes.put("/api/auth/**", "Authentication Service (port 8081)");
        routes.put("/api/customer/**", "Customer Service (port 8082)");
        routes.put("/api/device/**", "Device Service (port 8083)");
        response.put("routes", routes);
        
        Map<String, String> swaggerUrls = new HashMap<>();
        swaggerUrls.put("auth", "http://localhost:8081/api/auth/swagger-ui.html");
        swaggerUrls.put("customer", "http://localhost:8082/api/customer/swagger-ui.html");
        swaggerUrls.put("device", "http://localhost:8083/api/device/swagger-ui.html");
        response.put("swagger-urls", swaggerUrls);
        
        return ResponseEntity.ok(response);
    }
}