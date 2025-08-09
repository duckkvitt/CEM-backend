package com.g47.cem.cemcustomer.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemcustomer.dto.request.CreateCustomerRequest;
import com.g47.cem.cemcustomer.dto.request.UpdateCustomerRequest;
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
        
        // Create customer entity by setting fields manually
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCompanyName(request.getCompanyName());
        customer.setCompanyTaxCode(request.getCompanyTaxCode());
        customer.setCompanyAddress(request.getCompanyAddress());
        customer.setLegalRepresentative(request.getLegalRepresentative());
        customer.setTitle(request.getTitle());
        customer.setIdentityNumber(request.getIdentityNumber());
        customer.setIdentityIssueDate(request.getIdentityIssueDate());
        customer.setIdentityIssuePlace(request.getIdentityIssuePlace());
        customer.setFax(request.getFax());
        customer.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        customer.setIsHidden(request.getIsHidden() != null ? request.getIsHidden() : false);
        customer.setCreatedBy(createdBy);
        
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
                .orElse(null);
        
        if (customer == null) {
            log.warn("Customer not found with email: {}", email);
            return null;
        }
        
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
        
        // Map Java field names to database column names for native query
        Pageable mappedPageable = mapFieldNamesToColumnNames(pageable);
        
        Page<Customer> customers = customerRepository.findCustomersWithFilters(
                name, email, phone, isHidden, mappedPageable);
        return customers.map(this::mapToCustomerResponse);
    }
    
    /**
     * Maps Java field names to database column names for native queries
     */
    private Pageable mapFieldNamesToColumnNames(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            // Default sorting by created_at desc if no sort specified
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                    Sort.by(Sort.Direction.DESC, "created_at"));
        }
        
        List<Sort.Order> mappedOrders = pageable.getSort().stream()
                .map(order -> {
                    String property = order.getProperty();
                    String mappedProperty = mapFieldToColumn(property);
                    return new Sort.Order(order.getDirection(), mappedProperty);
                })
                .collect(java.util.stream.Collectors.toList());
        
        Sort mappedSort = Sort.by(mappedOrders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mappedSort);
    }
    
    /**
     * Maps individual field names to column names
     */
    private String mapFieldToColumn(String fieldName) {
        return switch (fieldName) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            case "isHidden" -> "is_hidden";
            case "companyName" -> "company_name";
            case "companyTaxCode" -> "company_tax_code";
            case "companyAddress" -> "company_address";
            case "legalRepresentative" -> "legal_representative";
            case "identityNumber" -> "identity_number";
            case "identityIssueDate" -> "identity_issue_date";
            case "identityIssuePlace" -> "identity_issue_place";
            default -> fieldName; // Return as-is if no mapping needed
        };
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
     * Update customer
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Customer with email " + request.getEmail() + " already exists", HttpStatus.CONFLICT);
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getCompanyName() != null) {
            customer.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyTaxCode() != null) {
            customer.setCompanyTaxCode(request.getCompanyTaxCode());
        }
        if (request.getCompanyAddress() != null) {
            customer.setCompanyAddress(request.getCompanyAddress());
        }
        if (request.getLegalRepresentative() != null) {
            customer.setLegalRepresentative(request.getLegalRepresentative());
        }
        if (request.getTitle() != null) {
            customer.setTitle(request.getTitle());
        }
        if (request.getIdentityNumber() != null) {
            customer.setIdentityNumber(request.getIdentityNumber());
        }
        if (request.getIdentityIssueDate() != null) {
            customer.setIdentityIssueDate(request.getIdentityIssueDate());
        }
        if (request.getIdentityIssuePlace() != null) {
            customer.setIdentityIssuePlace(request.getIdentityIssuePlace());
        }
        if (request.getFax() != null) {
            customer.setFax(request.getFax());
        }
        if (request.getTags() != null) {
            customer.setTags(request.getTags());
        }
        if (request.getIsHidden() != null) {
            customer.setIsHidden(request.getIsHidden());
        }

        Customer saved = customerRepository.save(customer);
        log.info("Customer with ID: {} updated successfully", id);
        return mapToCustomerResponse(saved);
    }

    /**
     * Convert Customer entity to response DTO
     */
    private CustomerResponse mapToCustomerResponse(Customer customer) {
        return modelMapper.map(customer, CustomerResponse.class);
    }
}