package com.g47.cem.cemgateway.controller;

import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/customer")
@Tag(name = "Customer", description = "Customer service APIs proxied through Gateway")
public class CustomerProxyController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String CUSTOMER_SERVICE_URL = "http://localhost:8082/api/customer";

    /* -------------------------------------------------
     * Customer CRUD & Search endpoints
     * -------------------------------------------------*/

    @PostMapping("/v1/customers")
    @Operation(summary = "Create Customer", description = "Create a new customer")
    public ResponseEntity<?> createCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Customer data", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"email\":\"customer@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\"}")))
            @RequestBody Object body,
            HttpServletRequest request) {
        return proxyRequest("/v1/customers", HttpMethod.POST, body, request);
    }

    @GetMapping("/v1/customers/{id}")
    @Operation(summary = "Get Customer by ID", description = "Retrieve a customer by ID")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id, HttpServletRequest request) {
        return proxyRequest("/v1/customers/" + id, HttpMethod.GET, null, request);
    }

    @GetMapping("/v1/customers/email/{email}")
    @Operation(summary = "Get Customer by Email", description = "Retrieve a customer by email")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email, HttpServletRequest request) {
        return proxyRequest("/v1/customers/email/" + email, HttpMethod.GET, null, request);
    }

    @GetMapping("/v1/customers")
    @Operation(summary = "Get All Customers", description = "Retrieve customers with optional filters and pagination")
    public ResponseEntity<?> getAllCustomers(HttpServletRequest request) {
        String query = request.getQueryString();
        String path = "/v1/customers" + (query != null ? "?" + query : "");
        return proxyRequest(path, HttpMethod.GET, null, request);
    }

    @GetMapping("/v1/customers/visible")
    @Operation(summary = "Get Visible Customers", description = "Retrieve visible customers")
    public ResponseEntity<?> getVisibleCustomers(HttpServletRequest request) {
        String query = request.getQueryString();
        String path = "/v1/customers/visible" + (query != null ? "?" + query : "");
        return proxyRequest(path, HttpMethod.GET, null, request);
    }

    @GetMapping("/v1/customers/hidden")
    @Operation(summary = "Get Hidden Customers", description = "Retrieve hidden customers")
    public ResponseEntity<?> getHiddenCustomers(HttpServletRequest request) {
        String query = request.getQueryString();
        String path = "/v1/customers/hidden" + (query != null ? "?" + query : "");
        return proxyRequest(path, HttpMethod.GET, null, request);
    }

    @PutMapping("/v1/customers/{id}/hide")
    @Operation(summary = "Hide Customer", description = "Mark customer as hidden")
    public ResponseEntity<?> hideCustomer(@PathVariable Long id, HttpServletRequest request) {
        return proxyRequest("/v1/customers/" + id + "/hide", HttpMethod.PUT, null, request);
    }

    @PutMapping("/v1/customers/{id}/show")
    @Operation(summary = "Show Customer", description = "Restore a hidden customer")
    public ResponseEntity<?> showCustomer(@PathVariable Long id, HttpServletRequest request) {
        return proxyRequest("/v1/customers/" + id + "/show", HttpMethod.PUT, null, request);
    }

    @GetMapping("/v1/customers/tag/{tag}")
    @Operation(summary = "Get Customers by Tag", description = "Retrieve customers by tag")
    public ResponseEntity<?> getCustomersByTag(@PathVariable String tag, HttpServletRequest request) {
        String query = request.getQueryString();
        String path = "/v1/customers/tag/" + tag + (query != null ? "?" + query : "");
        return proxyRequest(path, HttpMethod.GET, null, request);
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
            String url = CUSTOMER_SERVICE_URL + path;
            ResponseEntity<Object> response = restTemplate.exchange(url, method, entity, Object.class);
            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Gateway error: " + e.getMessage() + "\"}");
        }
    }
} 