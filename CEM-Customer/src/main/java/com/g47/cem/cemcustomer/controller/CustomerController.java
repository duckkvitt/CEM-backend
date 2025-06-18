package com.g47.cem.cemcustomer.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemcustomer.dto.request.CreateCustomerRequest;
import com.g47.cem.cemcustomer.dto.response.ApiResponse;
import com.g47.cem.cemcustomer.dto.response.CustomerResponse;
import com.g47.cem.cemcustomer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Customer management
 */
@RestController
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "Customer management APIs")
public class CustomerController {
    
    private final CustomerService customerService;
    
    /**
     * Create a new customer
     */
    @PostMapping
    @Operation(summary = "Create customer", description = "Create a new customer")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Creating customer with email: {} by user: {}", 
                request.getEmail(), authentication.getName());
        
        CustomerResponse customer = customerService.createCustomer(request, authentication.getName());
        
        ApiResponse<CustomerResponse> response = ApiResponse.success(
                customer, 
                "Customer created successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        response.setStatus(HttpStatus.CREATED.value());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get customer by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a customer by their ID")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        log.debug("Fetching customer with ID: {}", id);
        
        CustomerResponse customer = customerService.getCustomerById(id);
        
        ApiResponse<CustomerResponse> response = ApiResponse.success(customer);
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get customer by email
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieve a customer by their email")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByEmail(
            @PathVariable String email,
            HttpServletRequest httpRequest) {
        
        log.debug("Fetching customer with email: {}", email);
        
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        
        ApiResponse<CustomerResponse> response = ApiResponse.success(customer);
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all customers with pagination and search
     */
    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve all customers with pagination and optional filters")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAllCustomers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filter by name")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by email")
            @RequestParam(required = false) String email,
            @Parameter(description = "Filter by phone")
            @RequestParam(required = false) String phone,
            @Parameter(description = "Filter by hidden status")
            @RequestParam(required = false) Boolean isHidden,
            HttpServletRequest httpRequest) {
        
        log.debug("Fetching customers - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> customers;
        if (name != null || email != null || phone != null || isHidden != null) {
            customers = customerService.searchCustomers(name, email, phone, isHidden, pageable);
        } else {
            customers = customerService.getAllCustomers(pageable);
        }
        
        ApiResponse<Page<CustomerResponse>> response = ApiResponse.success(customers);
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get visible customers only
     */
    @GetMapping("/visible")
    @Operation(summary = "Get visible customers", description = "Retrieve only visible (non-hidden) customers")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getVisibleCustomers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        log.debug("Fetching visible customers - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> customers = customerService.getVisibleCustomers(pageable);
        
        ApiResponse<Page<CustomerResponse>> response = ApiResponse.success(customers);
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get hidden customers only
     */
    @GetMapping("/hidden")
    @Operation(summary = "Get hidden customers", description = "Retrieve only hidden customers")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getHiddenCustomers(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpRequest) {
        
        log.debug("Fetching hidden customers - page: {}, size: {}", page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CustomerResponse> customers = customerService.getHiddenCustomers(pageable);
        
        ApiResponse<Page<CustomerResponse>> response = ApiResponse.success(customers);
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Hide a customer
     */
    @PutMapping("/{id}/hide")
    @Operation(summary = "Hide customer", description = "Mark a customer as hidden")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerResponse>> hideCustomer(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        log.info("Hiding customer with ID: {}", id);
        
        CustomerResponse customer = customerService.hideCustomer(id);
        
        ApiResponse<CustomerResponse> response = ApiResponse.success(
                customer, 
                "Customer hidden successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Show a customer (restore from hidden)
     */
    @PutMapping("/{id}/show")
    @Operation(summary = "Show customer", description = "Restore a customer from hidden status")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<CustomerResponse>> showCustomer(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        log.info("Showing customer with ID: {}", id);
        
        CustomerResponse customer = customerService.showCustomer(id);
        
        ApiResponse<CustomerResponse> response = ApiResponse.success(
                customer, 
                "Customer restored successfully"
        );
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get customers by tag
     */
    @GetMapping("/tag/{tag}")
    @Operation(summary = "Get customers by tag", description = "Retrieve customers that have a specific tag")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getCustomersByTag(
            @PathVariable String tag,
            HttpServletRequest httpRequest) {
        
        log.debug("Fetching customers with tag: {}", tag);
        
        List<CustomerResponse> customers = customerService.getCustomersByTag(tag);
        
        ApiResponse<List<CustomerResponse>> response = ApiResponse.success(customers);
        response.setPath(httpRequest.getRequestURI());
        
        return ResponseEntity.ok(response);
    }
}
