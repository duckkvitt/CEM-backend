package com.g47.cem.cemdevice.integration;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for integrating with Spare Parts service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SparePartIntegrationService {

    private final RestTemplate restTemplate;

    @Value("${app.spareparts.service.url:http://localhost:8082}")
    private String sparePartsServiceUrl;

    /**
     * Get spare part by ID
     */
    public Optional<SparePartDto> getSparePartById(Long sparePartId, String bearerToken) {
        try {
            log.debug("Fetching spare part with ID: {} from spare parts service", sparePartId);

            String url = sparePartsServiceUrl + "/spare-parts/" + sparePartId;

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<SparePartDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<SparePartDto>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                return Optional.of(response.getBody().getData());
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching spare part with ID: {}", sparePartId, e);
            return Optional.empty();
        }
    }

    /**
     * Get all spare parts
     */
    public List<SparePartDto> getAllSpareParts(String bearerToken) {
        try {
            log.debug("Fetching all spare parts from spare parts service");

            String url = UriComponentsBuilder.fromUriString(sparePartsServiceUrl + "/spare-parts")
                    .queryParam("size", "1000") // Get all spare parts
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<PageResponse<SparePartDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<PageResponse<SparePartDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                List<SparePartDto> spareParts = response.getBody().getData().getContent();
                log.debug("Successfully fetched {} spare parts", spareParts != null ? spareParts.size() : 0);
                return spareParts != null ? spareParts : List.of();
            } else {
                log.warn("Failed to fetch spare parts - Response body: {}", response.getBody());
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error fetching spare parts", e);
            return List.of();
        }
    }

    /**
     * Get suppliers that provide spare parts
     */
    public List<SupplierDto> getSuppliersForSpareParts(String bearerToken) {
        try {
            log.debug("Fetching suppliers from spare parts service");

            String url = UriComponentsBuilder.fromUriString(sparePartsServiceUrl + "/suppliers")
                    .queryParam("size", "1000") // Get all suppliers
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<PageResponse<SupplierDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<PageResponse<SupplierDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                List<SupplierDto> suppliers = response.getBody().getData().getContent();
                log.debug("Successfully fetched {} suppliers", suppliers != null ? suppliers.size() : 0);
                return suppliers != null ? suppliers : List.of();
            } else {
                log.warn("Failed to fetch suppliers - Response body: {}", response.getBody());
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error fetching suppliers", e);
            return List.of();
        }
    }

    /**
     * Get suppliers that provide devices
     */
    public List<SupplierDeviceTypeDto> getSuppliersForDevices(String bearerToken) {
        try {
            log.debug("Fetching supplier device types from spare parts service");

            String url = UriComponentsBuilder.fromUriString(sparePartsServiceUrl + "/supplier-device-types")
                    .queryParam("isActive", "true")
                    .queryParam("size", "1000") // Get all active supplier device types
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<PageResponse<SupplierDeviceTypeDto>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<PageResponse<SupplierDeviceTypeDto>>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess() && response.getBody().getData() != null) {
                List<SupplierDeviceTypeDto> supplierDeviceTypes = response.getBody().getData().getContent();
                log.debug("Successfully fetched {} supplier device types", supplierDeviceTypes != null ? supplierDeviceTypes.size() : 0);
                return supplierDeviceTypes != null ? supplierDeviceTypes : List.of();
            } else {
                log.warn("Failed to fetch supplier device types - Response body: {}", response.getBody());
                return List.of();
            }

        } catch (Exception e) {
            log.error("Error fetching supplier device types", e);
            return List.of();
        }
    }
    
    /**
     * Add stock to spare part inventory
     */
    public boolean addStockToSparePart(Long sparePartId, Integer quantity, String notes, String bearerToken) {
        try {
            log.debug("Adding {} units to spare part inventory for spare part ID: {}", quantity, sparePartId);

            String url = UriComponentsBuilder.fromUriString(sparePartsServiceUrl + "/api/v1/spare-part-inventory/" + sparePartId + "/add-stock")
                    .queryParam("quantity", quantity)
                    .queryParam("notes", notes)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Object>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                log.debug("Successfully added {} units to spare part inventory for spare part ID: {}", quantity, sparePartId);
                return true;
            } else {
                log.warn("Failed to add stock to spare part inventory - Response body: {}", response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Error adding stock to spare part inventory for spare part ID: {}", sparePartId, e);
            return false;
        }
    }
    
    /**
     * Remove stock from spare part inventory
     */
    public boolean removeStockFromSparePart(Long sparePartId, Integer quantity, String notes, String bearerToken) {
        try {
            log.debug("Removing {} units from spare part inventory for spare part ID: {}", quantity, sparePartId);

            String url = UriComponentsBuilder.fromUriString(sparePartsServiceUrl + "/api/v1/spare-part-inventory/" + sparePartId + "/remove-stock")
                    .queryParam("quantity", quantity)
                    .queryParam("notes", notes)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Object>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                log.debug("Successfully removed {} units from spare part inventory for spare part ID: {}", quantity, sparePartId);
                return true;
            } else {
                log.warn("Failed to remove stock from spare part inventory - Response body: {}", response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Error removing stock from spare part inventory for spare part ID: {}", sparePartId, e);
            return false;
        }
    }
    
    /**
     * Check if spare part has sufficient stock
     */
    public boolean hasSufficientStock(Long sparePartId, Integer requiredQuantity, String bearerToken) {
        try {
            log.debug("Checking if spare part ID: {} has sufficient stock for quantity: {}", sparePartId, requiredQuantity);

            String url = UriComponentsBuilder.fromUriString(sparePartsServiceUrl + "/api/v1/spare-part-inventory/" + sparePartId + "/has-sufficient-stock")
                    .queryParam("requiredQuantity", requiredQuantity)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            if (bearerToken != null && !bearerToken.isEmpty()) {
                headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
            );

            if (response.getBody() != null && response.getBody().isSuccess()) {
                Boolean hasSufficient = response.getBody().getData();
                log.debug("Spare part ID: {} has sufficient stock: {}", sparePartId, hasSufficient);
                return hasSufficient != null && hasSufficient;
            } else {
                log.warn("Failed to check stock availability - Response body: {}", response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Error checking stock availability for spare part ID: {}", sparePartId, e);
            return false;
        }
    }

    // DTOs for API responses
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageResponse<T> {
        private List<T> content;
        private int totalPages;
        private long totalElements;
        private int size;
        private int number;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SparePartDto {
        private Long id;
        private String partName;
        private String partCode;
        private String description;
        private String compatibleDevices;
        private String unitOfMeasurement;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupplierDto {
        private Long id;
        private String companyName;
        private String contactPerson;
        private String email;
        private String phone;
        private String address;
        private Boolean suppliesDevices;
        private Boolean suppliesSpareParts;
        private String status;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupplierDeviceTypeDto {
        private Long id;
        private Long supplierId;
        private String supplierCompanyName;
        private String deviceType;
        private String deviceModel;
        private String unitPrice;
        private Integer minimumOrderQuantity;
        private Integer leadTimeDays;
        private String notes;
        private Boolean isActive;
    }
}
