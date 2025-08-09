package com.g47.cem.cemspareparts.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.dto.request.CreateSupplierRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSupplierRequest;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartResponse;
import com.g47.cem.cemspareparts.dto.response.SupplierResponse;
import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.Supplier;
import com.g47.cem.cemspareparts.enums.SupplierStatus;
import com.g47.cem.cemspareparts.exception.BusinessException;
import com.g47.cem.cemspareparts.exception.ResourceNotFoundException;
import com.g47.cem.cemspareparts.repository.SparePartRepository;
import com.g47.cem.cemspareparts.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SparePartRepository sparePartRepository;
    private final ModelMapper modelMapper;

    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        log.info("Creating new supplier with company name: {}", request.getCompanyName());
        
        // Check if supplier with same company name already exists
        if (supplierRepository.findByCompanyName(request.getCompanyName()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Supplier with company name " + request.getCompanyName() + " already exists.");
        }
        
        // Check if supplier with same email already exists
        if (supplierRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Supplier with email " + request.getEmail() + " already exists.");
        }
        
        Supplier supplier = modelMapper.map(request, Supplier.class);
        supplier.setStatus(SupplierStatus.ACTIVE);
        
        // Set spare parts if provided - use safe collection manipulation
        if (request.getSparePartIds() != null && !request.getSparePartIds().isEmpty()) {
            updateSupplierSpareParts(supplier, request.getSparePartIds());
        }
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        
        // Reload with spare parts to prevent ConcurrentModificationException when mapping
        Supplier supplierWithSpareParts = loadSupplierWithSpareParts(savedSupplier);
        
        log.info("Successfully created supplier with ID: {}", savedSupplier.getId());
        return mapToSupplierResponse(supplierWithSpareParts);
    }

    public SupplierResponse getSupplierById(Long id) {
        log.info("Fetching supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findByIdWithSpareParts(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return mapToSupplierResponse(supplier);
    }

    public PagedResponse<SupplierResponse> getAllSuppliers(int page, int size, String sortBy, String sortDir, String keyword, SupplierStatus status) {
        log.info("Fetching suppliers. Page: {}, Size: {}, SortBy: {}, SortDir: {}, Keyword: {}, Status: {}", 
                page, size, sortBy, sortDir, keyword, status);
        
        // Convert SupplierStatus enum to String for query
        String statusParam = status != null ? status.name() : null;
        
        try {
            // Use native query approach with proper PostgreSQL text casting
            Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Supplier> suppliersPage = supplierRepository.findSuppliersWithFilters(keyword, statusParam, pageable);
            
            // Load suppliers with their spare parts eagerly to prevent ConcurrentModificationException
            List<Supplier> suppliersWithSpareParts = loadSuppliersWithSpareParts(suppliersPage.getContent());
            
            List<SupplierResponse> content = suppliersWithSpareParts.stream()
                    .map(this::mapToSupplierResponse)
                    .toList();

            return new PagedResponse<>(content, suppliersPage.getNumber(), suppliersPage.getSize(),
                    suppliersPage.getTotalElements(), suppliersPage.getTotalPages(), suppliersPage.isLast());
        } catch (Exception e) {
            log.warn("Native query pagination failed, falling back to manual pagination: {}", e.getMessage());
            
            // Fallback: Use manual pagination with eager loading to avoid all issues
            return getAllSuppliersWithManualPagination(page, size, sortBy, sortDir, keyword, statusParam);
        }
    }
    
    /**
     * Loads suppliers with their spare parts eagerly to prevent ConcurrentModificationException.
     * This method reloads suppliers from native queries with proper relationship fetching.
     */
    private List<Supplier> loadSuppliersWithSpareParts(List<Supplier> suppliers) {
        if (suppliers == null || suppliers.isEmpty()) {
            return suppliers;
        }
        
        List<Long> supplierIds = suppliers.stream()
                .map(Supplier::getId)
                .toList();
        
        return supplierRepository.findByIdsWithSpareParts(supplierIds);
    }
    
    /**
     * Loads a single supplier with spare parts eagerly.
     */
    private Supplier loadSupplierWithSpareParts(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        
        return supplierRepository.findByIdWithSpareParts(supplier.getId())
                .orElse(supplier);
    }
    
    /**
     * Safely updates supplier's spare parts collection to prevent ConcurrentModificationException.
     * This method creates a completely new collection without reusing existing Hibernate-managed collections.
     */
    private void updateSupplierSpareParts(Supplier supplier, Set<Long> sparePartIds) {
        // Clear existing spare parts collection safely
        if (supplier.getSpareParts() != null) {
            supplier.getSpareParts().clear();
        }
        
        // Create a completely new HashSet to avoid any Hibernate collection issues
        Set<SparePart> newSpareParts = new HashSet<>();
        
        if (sparePartIds != null && !sparePartIds.isEmpty()) {
            for (Long sparePartId : sparePartIds) {
                SparePart sparePart = sparePartRepository.findById(sparePartId)
                    .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", sparePartId));
                
                // Add the spare part to our new collection
                newSpareParts.add(sparePart);
            }
        }
        
        // Set the completely new collection
        supplier.setSpareParts(newSpareParts);
    }
    
    /**
     * Fallback method for manual pagination when native queries fail.
     * This method prevents ConcurrentModificationException by using eager loading.
     */
    private PagedResponse<SupplierResponse> getAllSuppliersWithManualPagination(int page, int size, String sortBy, String sortDir, String keyword, String statusParam) {
        log.info("Using manual pagination for suppliers to avoid lazy loading issues");
        
        // Get total count first
        Long totalElements = supplierRepository.countSuppliersWithFilters(keyword, statusParam);
        
        // Get all suppliers with eager loading (spareParts are fetched immediately)
        List<Supplier> allSuppliers = supplierRepository.findSuppliersWithFiltersEager(keyword, statusParam);
        
        // Apply manual sorting
        allSuppliers.sort((s1, s2) -> {
            int comparison = switch (sortBy.toLowerCase()) {
                case "companyname" -> s1.getCompanyName().compareToIgnoreCase(s2.getCompanyName());
                case "contactperson" -> s1.getContactPerson().compareToIgnoreCase(s2.getContactPerson());
                case "email" -> s1.getEmail().compareToIgnoreCase(s2.getEmail());
                case "status" -> s1.getStatus().compareTo(s2.getStatus());
                case "createdat" -> s1.getCreatedAt().compareTo(s2.getCreatedAt());
                default -> s1.getId().compareTo(s2.getId()); // "id"
            };
            return sortDir.equalsIgnoreCase("desc") ? -comparison : comparison;
        });
        
        // Apply manual pagination
        int start = page * size;
        int end = Math.min(start + size, allSuppliers.size());
        
        List<Supplier> pagedSuppliers = start >= allSuppliers.size() ? 
                List.of() : allSuppliers.subList(start, end);
        
        List<SupplierResponse> content = pagedSuppliers.stream()
                .map(this::mapToSupplierResponse)
                .toList();
        
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = page >= totalPages - 1;
        
        return new PagedResponse<>(content, page, size, totalElements, totalPages, isLast);
    }
    
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        log.info("Updating supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        
        // Check if updating company name to one that already exists (excluding current supplier)
        if (request.getCompanyName() != null && !request.getCompanyName().equals(supplier.getCompanyName())) {
            if (supplierRepository.findByCompanyName(request.getCompanyName()).isPresent()) {
                throw new BusinessException(HttpStatus.CONFLICT, "Supplier with company name " + request.getCompanyName() + " already exists.");
            }
        }
        
        // Check if updating email to one that already exists (excluding current supplier)
        if (request.getEmail() != null && !request.getEmail().equals(supplier.getEmail())) {
            if (supplierRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BusinessException(HttpStatus.CONFLICT, "Supplier with email " + request.getEmail() + " already exists.");
            }
        }
        
        modelMapper.map(request, supplier);
        
        // Update spare parts if provided - use safe collection manipulation
        if (request.getSparePartIds() != null) {
            updateSupplierSpareParts(supplier, request.getSparePartIds());
        }
        
        Supplier updatedSupplier = supplierRepository.save(supplier);
        
        // Reload with spare parts to prevent ConcurrentModificationException when mapping
        Supplier supplierWithSpareParts = loadSupplierWithSpareParts(updatedSupplier);
        
        log.info("Successfully updated supplier with ID: {}", updatedSupplier.getId());
        return mapToSupplierResponse(supplierWithSpareParts);
    }

    public void deactivateSupplier(Long id) {
        log.info("Deactivating supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        supplier.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(supplier);
        log.info("Successfully deactivated supplier with ID: {}", id);
    }

    public void activateSupplier(Long id) {
        log.info("Activating supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        supplier.setStatus(SupplierStatus.ACTIVE);
        supplierRepository.save(supplier);
        log.info("Successfully activated supplier with ID: {}", id);
    }

    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with ID: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        supplierRepository.delete(supplier);
        log.info("Successfully deleted supplier with ID: {}", id);
    }

    public List<SupplierResponse> getSuppliersBySparePartId(Long sparePartId) {
        log.info("Fetching suppliers for spare part ID: {}", sparePartId);
        // Validate that spare part exists
        sparePartRepository.findById(sparePartId)
            .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", sparePartId));
        
        // Repository method already loads spare parts eagerly to prevent ConcurrentModificationException
        List<Supplier> suppliers = supplierRepository.findActiveSuppliersBySparePartId(sparePartId);
        
        return suppliers.stream()
                .map(this::mapToSupplierResponse)
                .toList();
    }
    
    /**
     * Custom mapping method to convert Supplier entity to SupplierResponse DTO.
     * This completely avoids ModelMapper for Supplier entity to prevent Hibernate collection issues.
     * Enhanced with safe collection handling to prevent ConcurrentModificationException.
     */
    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        
        // Manual mapping of all basic fields to avoid ModelMapper collection issues
        response.setId(supplier.getId());
        response.setCompanyName(supplier.getCompanyName());
        response.setContactPerson(supplier.getContactPerson());
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setFax(supplier.getFax());
        response.setAddress(supplier.getAddress());
        response.setTaxCode(supplier.getTaxCode());
        response.setBusinessLicense(supplier.getBusinessLicense());
        response.setWebsite(supplier.getWebsite());
        response.setDescription(supplier.getDescription());
        response.setStatus(supplier.getStatus());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());
        
        // Safely map spare parts with multiple layers of protection against concurrent modification
        response.setSpareParts(mapSparePartsToResponse(supplier));
        
        return response;
    }
    
    /**
     * Safely maps SpareParts collection to avoid ConcurrentModificationException.
     * Uses defensive copying and safe iteration patterns.
     */
    private Set<SparePartResponse> mapSparePartsToResponse(Supplier supplier) {
        try {
            Set<SparePart> spareParts = supplier.getSpareParts();
            
            // Return empty set if collection is null or empty
            if (spareParts == null || spareParts.isEmpty()) {
                return new HashSet<>();
            }
            
            // Create a defensive copy to avoid concurrent modification issues
            // This is especially important when dealing with Hibernate-managed collections
            Set<SparePart> safeSparePartsCopy = new HashSet<>(spareParts);
            
            return safeSparePartsCopy.stream()
                    .filter(sparePart -> sparePart != null) // Additional null safety
                    .map(this::mapSparePartToResponse)
                    .collect(Collectors.toSet());
                    
        } catch (Exception e) {
            log.warn("Error mapping spare parts for supplier {}: {}. Returning empty set.", 
                    supplier.getId(), e.getMessage());
            // Return empty set instead of failing the entire operation
            return new HashSet<>();
        }
    }
    
    /**
     * Maps a single SparePart to SparePartResponse.
     * Extracted for better maintainability and error handling.
     */
    private SparePartResponse mapSparePartToResponse(SparePart sparePart) {
        SparePartResponse sparePartResponse = new SparePartResponse();
        sparePartResponse.setId(sparePart.getId());
        sparePartResponse.setPartName(sparePart.getPartName());
        sparePartResponse.setPartCode(sparePart.getPartCode());
        sparePartResponse.setDescription(sparePart.getDescription());
        sparePartResponse.setCompatibleDevices(sparePart.getCompatibleDevices());
        sparePartResponse.setUnitOfMeasurement(sparePart.getUnitOfMeasurement());
        sparePartResponse.setStatus(sparePart.getStatus());
        sparePartResponse.setCreatedAt(sparePart.getCreatedAt());
        sparePartResponse.setUpdatedAt(sparePart.getUpdatedAt());
        return sparePartResponse;
    }
}