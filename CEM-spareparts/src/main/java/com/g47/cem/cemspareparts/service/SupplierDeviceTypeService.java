package com.g47.cem.cemspareparts.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.dto.request.CreateSupplierDeviceTypeRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSupplierDeviceTypeRequest;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SupplierDeviceTypeResponse;
import com.g47.cem.cemspareparts.entity.Supplier;
import com.g47.cem.cemspareparts.entity.SupplierDeviceType;
import com.g47.cem.cemspareparts.exception.BusinessException;
import com.g47.cem.cemspareparts.exception.ResourceNotFoundException;
import com.g47.cem.cemspareparts.repository.SupplierDeviceTypeRepository;
import com.g47.cem.cemspareparts.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupplierDeviceTypeService {

    private final SupplierDeviceTypeRepository supplierDeviceTypeRepository;
    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    public SupplierDeviceTypeResponse createSupplierDeviceType(CreateSupplierDeviceTypeRequest request) {
        log.info("Creating new supplier device type for supplier ID: {} and device type: {}", 
                request.getSupplierId(), request.getDeviceType());
        
        // Check if supplier exists
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", request.getSupplierId()));
        
        // Check if supplier already provides this device type and model combination
        Optional<SupplierDeviceType> existing = supplierDeviceTypeRepository
                .findBySupplierIdAndDeviceTypeAndModel(request.getSupplierId(), request.getDeviceType(), request.getDeviceModel());
        
        if (existing.isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, 
                    "Supplier already provides device type: " + request.getDeviceType() + 
                    (request.getDeviceModel() != null ? " - " + request.getDeviceModel() : ""));
        }
        
        SupplierDeviceType supplierDeviceType = modelMapper.map(request, SupplierDeviceType.class);
        supplierDeviceType.setSupplier(supplier);
        supplierDeviceType.setIsActive(true);
        
        // Update supplier to indicate it supplies devices
        if (!supplier.getSuppliesDevices()) {
            supplier.setSuppliesDevices(true);
            supplierRepository.save(supplier);
        }
        
        SupplierDeviceType savedSupplierDeviceType = supplierDeviceTypeRepository.save(supplierDeviceType);
        
        log.info("Successfully created supplier device type with ID: {}", savedSupplierDeviceType.getId());
        return modelMapper.map(savedSupplierDeviceType, SupplierDeviceTypeResponse.class);
    }

    public SupplierDeviceTypeResponse getSupplierDeviceTypeById(Long id) {
        log.info("Fetching supplier device type with ID: {}", id);
        SupplierDeviceType supplierDeviceType = supplierDeviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierDeviceType", "id", id));
        return modelMapper.map(supplierDeviceType, SupplierDeviceTypeResponse.class);
    }

    public PagedResponse<SupplierDeviceTypeResponse> getAllSupplierDeviceTypes(int page, int size, String sortBy, String sortDir) {
        return getAllSupplierDeviceTypes(page, size, sortBy, sortDir, null, null, null);
    }

    public PagedResponse<SupplierDeviceTypeResponse> getAllSupplierDeviceTypes(int page, int size, String sortBy, String sortDir, 
            Long supplierId, Boolean isActive, String keyword) {
        log.info("Fetching supplier device types. Page: {}, Size: {}, SortBy: {}, SortDir: {}, SupplierId: {}, IsActive: {}, Keyword: {}", 
                page, size, sortBy, sortDir, supplierId, isActive, keyword);
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Safely handle keyword parameter - trim whitespace and check if empty
        String processedKeyword = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            processedKeyword = keyword.trim().toLowerCase();
        }
        
        Page<SupplierDeviceType> supplierDeviceTypesPage;
        try {
            // Try the main JPA query first
            supplierDeviceTypesPage = supplierDeviceTypeRepository
                    .searchSupplierDeviceTypes(supplierId, isActive, processedKeyword, pageable);
        } catch (Exception e) {
            log.warn("Main JPA query failed, falling back to native SQL query. Error: {}", e.getMessage());
            // Fallback to native SQL query if JPA query fails
            supplierDeviceTypesPage = supplierDeviceTypeRepository
                    .searchSupplierDeviceTypesNative(supplierId, isActive, processedKeyword, pageable);
        }
        
        List<SupplierDeviceTypeResponse> content = supplierDeviceTypesPage.getContent().stream()
                .map(supplierDeviceType -> modelMapper.map(supplierDeviceType, SupplierDeviceTypeResponse.class))
                .toList();

        return new PagedResponse<>(content, supplierDeviceTypesPage.getNumber(), supplierDeviceTypesPage.getSize(),
                supplierDeviceTypesPage.getTotalElements(), supplierDeviceTypesPage.getTotalPages(), supplierDeviceTypesPage.isLast());
    }
    
    public SupplierDeviceTypeResponse updateSupplierDeviceType(Long id, UpdateSupplierDeviceTypeRequest request) {
        log.info("Updating supplier device type with ID: {}", id);
        SupplierDeviceType supplierDeviceType = supplierDeviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierDeviceType", "id", id));
        
        // Check if updating device type/model would create a duplicate
        if ((request.getDeviceType() != null && !request.getDeviceType().equals(supplierDeviceType.getDeviceType())) ||
            (request.getDeviceModel() != null && !request.getDeviceModel().equals(supplierDeviceType.getDeviceModel()))) {
            
            Optional<SupplierDeviceType> existing = supplierDeviceTypeRepository
                    .findBySupplierIdAndDeviceTypeAndModel(
                            supplierDeviceType.getSupplier().getId(), 
                            request.getDeviceType() != null ? request.getDeviceType() : supplierDeviceType.getDeviceType(),
                            request.getDeviceModel() != null ? request.getDeviceModel() : supplierDeviceType.getDeviceModel());
            
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new BusinessException(HttpStatus.CONFLICT, 
                        "Supplier already provides device type: " + 
                        (request.getDeviceType() != null ? request.getDeviceType() : supplierDeviceType.getDeviceType()) + 
                        (request.getDeviceModel() != null ? " - " + request.getDeviceModel() : 
                         supplierDeviceType.getDeviceModel() != null ? " - " + supplierDeviceType.getDeviceModel() : ""));
            }
        }
        
        modelMapper.map(request, supplierDeviceType);
        
        SupplierDeviceType updatedSupplierDeviceType = supplierDeviceTypeRepository.save(supplierDeviceType);
        log.info("Successfully updated supplier device type with ID: {}", updatedSupplierDeviceType.getId());
        return modelMapper.map(updatedSupplierDeviceType, SupplierDeviceTypeResponse.class);
    }

    public void deactivateSupplierDeviceType(Long id) {
        log.info("Deactivating supplier device type with ID: {}", id);
        SupplierDeviceType supplierDeviceType = supplierDeviceTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierDeviceType", "id", id));
        
        supplierDeviceType.setIsActive(false);
        supplierDeviceTypeRepository.save(supplierDeviceType);
        
        // Check if supplier still has active device types
        Long activeDeviceTypesCount = supplierDeviceTypeRepository.countActiveDeviceTypesBySupplier(supplierDeviceType.getSupplier().getId());
        if (activeDeviceTypesCount == 0) {
            Supplier supplier = supplierDeviceType.getSupplier();
            supplier.setSuppliesDevices(false);
            supplierRepository.save(supplier);
        }
        
        log.info("Successfully deactivated supplier device type with ID: {}", id);
    }

    public List<SupplierDeviceTypeResponse> getActiveDeviceTypesBySupplier(Long supplierId) {
        log.info("Fetching active device types for supplier ID: {}", supplierId);
        List<SupplierDeviceType> supplierDeviceTypes = supplierDeviceTypeRepository
                .findActiveBySupplierIdOrderByDeviceType(supplierId);
        
        return supplierDeviceTypes.stream()
                .map(supplierDeviceType -> modelMapper.map(supplierDeviceType, SupplierDeviceTypeResponse.class))
                .toList();
    }

    public List<SupplierDeviceTypeResponse> getSuppliersForDeviceType(String deviceType, Integer requestedQuantity) {
        log.info("Finding suppliers for device type: {} with minimum quantity: {}", deviceType, requestedQuantity);
        List<SupplierDeviceType> supplierDeviceTypes = supplierDeviceTypeRepository
                .findSuppliersForDeviceTypeWithMinimumQuantity(deviceType, requestedQuantity);
        
        return supplierDeviceTypes.stream()
                .map(supplierDeviceType -> modelMapper.map(supplierDeviceType, SupplierDeviceTypeResponse.class))
                .toList();
    }

    public List<String> getDistinctActiveDeviceTypes() {
        log.info("Fetching distinct active device types");
        return supplierDeviceTypeRepository.findDistinctActiveDeviceTypes();
    }

    public Object[] getSupplierDeviceTypeStatistics() {
        log.info("Fetching supplier device type statistics");
        return supplierDeviceTypeRepository.getSupplierDeviceTypeStatistics();
    }
}


