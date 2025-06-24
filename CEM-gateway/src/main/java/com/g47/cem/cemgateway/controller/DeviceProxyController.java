package com.g47.cem.cemgateway.controller;

import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/device")
@Tag(name = "Device", description = "Device service APIs proxied through Gateway")
public class DeviceProxyController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String DEVICE_SERVICE_URL = "http://localhost:8083/api/device";

    /* -------------------------------------------------
     * Device CRUD endpoints
     * -------------------------------------------------*/

    @PostMapping("/devices")
    @Operation(summary = "Create Device", description = "Create a new device")
    public ResponseEntity<?> createDevice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Device data", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"name\":\"Device A\",\"model\":\"Model X\"}")))
            @RequestBody Object body,
            HttpServletRequest request) {
        return proxyRequest("/devices", HttpMethod.POST, body, request);
    }

    @GetMapping("/devices/{id}")
    @Operation(summary = "Get Device by ID", description = "Retrieve a device by ID")
    public ResponseEntity<?> getDeviceById(@PathVariable Long id, HttpServletRequest request) {
        return proxyRequest("/devices/" + id, HttpMethod.GET, null, request);
    }

    @GetMapping("/devices")
    @Operation(summary = "Get All Devices", description = "Retrieve devices with optional filters and pagination")
    public ResponseEntity<?> getAllDevices(HttpServletRequest request) {
        String query = request.getQueryString();
        String path = "/devices" + (query != null ? "?" + query : "");
        return proxyRequest(path, HttpMethod.GET, null, request);
    }

    @PutMapping("/devices/{id}")
    @Operation(summary = "Update Device", description = "Update device information")
    public ResponseEntity<?> updateDevice(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated device data", content = @Content(mediaType = "application/json"))
            @RequestBody Object body,
            HttpServletRequest request) {
        return proxyRequest("/devices/" + id, HttpMethod.PUT, body, request);
    }

    @DeleteMapping("/devices/{id}")
    @Operation(summary = "Delete Device", description = "Delete a device by ID")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id, HttpServletRequest request) {
        return proxyRequest("/devices/" + id, HttpMethod.DELETE, null, request);
    }

    /* -------------------------------------------------
     * Device Note endpoints
     * -------------------------------------------------*/

    @PostMapping("/devices/{deviceId}/notes")
    @Operation(summary = "Add Device Note", description = "Add a note to a device")
    public ResponseEntity<?> addDeviceNote(
            @PathVariable Long deviceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Device note", content = @Content(mediaType = "application/json"))
            @RequestBody Object body,
            HttpServletRequest request) {
        return proxyRequest("/devices/" + deviceId + "/notes", HttpMethod.POST, body, request);
    }

    @GetMapping("/devices/{deviceId}/notes/{noteId}")
    @Operation(summary = "Get Device Note", description = "Retrieve a device note by ID")
    public ResponseEntity<?> getDeviceNoteById(@PathVariable Long deviceId, @PathVariable Long noteId, HttpServletRequest request) {
        return proxyRequest("/devices/" + deviceId + "/notes/" + noteId, HttpMethod.GET, null, request);
    }

    @GetMapping("/devices/{deviceId}/notes")
    @Operation(summary = "Get Device Notes", description = "Retrieve notes for a device")
    public ResponseEntity<?> getDeviceNotes(@PathVariable Long deviceId, HttpServletRequest request) {
        String query = request.getQueryString();
        String path = "/devices/" + deviceId + "/notes" + (query != null ? "?" + query : "");
        return proxyRequest(path, HttpMethod.GET, null, request);
    }

    @PutMapping("/devices/{deviceId}/notes/{noteId}")
    @Operation(summary = "Update Device Note", description = "Update a device note")
    public ResponseEntity<?> updateDeviceNote(
            @PathVariable Long deviceId,
            @PathVariable Long noteId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated device note", content = @Content(mediaType = "application/json"))
            @RequestBody Object body,
            HttpServletRequest request) {
        return proxyRequest("/devices/" + deviceId + "/notes/" + noteId, HttpMethod.PUT, body, request);
    }

    @DeleteMapping("/devices/{deviceId}/notes/{noteId}")
    @Operation(summary = "Delete Device Note", description = "Delete a device note by ID")
    public ResponseEntity<?> deleteDeviceNote(@PathVariable Long deviceId, @PathVariable Long noteId, HttpServletRequest request) {
        return proxyRequest("/devices/" + deviceId + "/notes/" + noteId, HttpMethod.DELETE, null, request);
    }

    /* -------------------------------------------------
     * Proxy helper
     * -------------------------------------------------*/
    private ResponseEntity<?> proxyRequest(String path, HttpMethod method, Object body, HttpServletRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host")) {
                    headers.add(headerName, request.getHeader(headerName));
                }
            }

            HttpEntity<?> entity = new HttpEntity<>(body, headers);
            String url = DEVICE_SERVICE_URL + path;
            ResponseEntity<Object> response = restTemplate.exchange(url, method, entity, Object.class);
            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Gateway error: " + e.getMessage() + "\"}");
        }
    }
} 