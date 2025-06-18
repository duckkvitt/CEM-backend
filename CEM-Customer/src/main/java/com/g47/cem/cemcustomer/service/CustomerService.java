package com.g47.cem.cemcustomer.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemcustomer.dto.request.CreateCustomerRequest;
import com.g47.cem.cemcustomer.dto.response.CustomerResponse;
import com.g47.cem.cemcustomer.entity.Customer;
import com.g47.cem.cemcustomer.exception.BusinessException;
import com.g47.cem.cemcustomer.exception.ResourceNotFoundException;
import com.g47.cem.cemcustomer.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Customer business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new customer
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, String createdBy) {
        log.info("Creating new customer with email: {}", request.getEmail());
        
        // Check if customer with email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Customer with email " + request.getEmail() + " already exists", 
                                      HttpStatus.CONFLICT);
        }
        
        // Create customer entity
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .isHidden(request.getIsHidden() != null ? request.getIsHidden() : false)
                .createdBy(createdBy)
                .build();
        
        // Save customer
        Customer savedCustomer = customerRepository.save(customer);
        
        log.info("Successfully created customer with ID: {} and email: {}", 
                savedCustomer.getId(), savedCustomer.getEmail());
        
        return mapToCustomerResponse(savedCustomer);
    }
    
    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.debug("Fetching customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        return mapToCustomerResponse(customer);
    }
    
    /**
     * Get customer by email
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        
        return mapToCustomerResponse(customer);
    }
    
    /**
     * Get all customers with pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        log.debug("Fetching all customers with pagination: {}", pageable);
        
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(this::mapToCustomerResponse);
    }
    
    /**
     * Get visible customers (not hidden)
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getVisibleCustomers(Pageable pageable) {
        log.debug("Fetching visible customers with pagination: {}", pageable);
        
        Page<Customer> customers = customerRepository.findAllVisible(pageable);
        return customers.map(this::mapToCustomerResponse);
    }
    
    /**
     * Get hidden customers
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getHiddenCustomers(Pageable pageable) {
        log.debug("Fetching hidden customers with pagination: {}", pageable);
        
        Page<Customer> customers = customerRepository.findAllHidden(pageable);
        return customers.map(this::mapToCustomerResponse);
    }
    
    /**
     * Search customers with filters
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(String name, String email, String phone, 
                                                 Boolean isHidden, Pageable pageable) {
        log.debug("Searching customers with filters - name: {}, email: {}, phone: {}, isHidden: {}", 
                name, email, phone, isHidden);
        
        Page<Customer> customers = customerRepository.findCustomersWithFilters(
                name, email, phone, isHidden, pageable);
        return customers.map(this::mapToCustomerResponse);
    }
    
    /**
     * Hide customer
     */
    @Transactional
    public CustomerResponse hideCustomer(Long id) {
        log.info("Hiding customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        customer.hide();
        Customer savedCustomer = customerRepository.save(customer);
        
        log.info("Successfully hidden customer with ID: {}", id);
        return mapToCustomerResponse(savedCustomer);
    }
    
    /**
     * Show customer (restore from hidden)
     */
    @Transactional
    public CustomerResponse showCustomer(Long id) {
        log.info("Showing customer with ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        customer.show();
        Customer savedCustomer = customerRepository.save(customer);
        
        log.info("Successfully showed customer with ID: {}", id);
        return mapToCustomerResponse(savedCustomer);
    }
    
    /**
     * Get customers by tag
     */
    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomersByTag(String tag) {
        log.debug("Fetching customers with tag: {}", tag);
        
        List<Customer> customers = customerRepository.findByTag(tag);
        return customers.stream()
                .map(this::mapToCustomerResponse)
                .toList();
    }
    
    /**
     * Map Customer entity to CustomerResponse DTO
     */
    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return modelMapper.map(customer, CustomerResponse.class);
    }
} 