package com.g47.cem.cemspareparts.service;

import com.g47.cem.cemspareparts.dto.request.CreateSparePartRequest;
import com.g47.cem.cemspareparts.dto.request.UpdateSparePartRequest;
import com.g47.cem.cemspareparts.dto.response.PagedResponse;
import com.g47.cem.cemspareparts.dto.response.SparePartResponse;
import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.enums.SparePartStatus;
import com.g47.cem.cemspareparts.exception.BusinessException;
import com.g47.cem.cemspareparts.exception.ResourceNotFoundException;
import com.g47.cem.cemspareparts.repository.SparePartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartService {

    private final SparePartRepository sparePartRepository;
    private final ModelMapper modelMapper;


    public SparePartResponse createSparePart(CreateSparePartRequest request) {
        log.info("Creating new spare part with code: {}", request.getPartCode());
        if (sparePartRepository.findByPartCode(request.getPartCode()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Spare part with code " + request.getPartCode() + " already exists.");
        }
        SparePart sparePart = modelMapper.map(request, SparePart.class);
        sparePart.setStatus(SparePartStatus.ACTIVE);
        SparePart savedSparePart = sparePartRepository.save(sparePart);
        
        log.info("Successfully created spare part with ID: {}", savedSparePart.getId());
        return modelMapper.map(savedSparePart, SparePartResponse.class);
    }

    public SparePartResponse getSparePartById(Long id) {
        log.info("Fetching spare part with ID: {}", id);
        SparePart sparePart = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", id));
        return modelMapper.map(sparePart, SparePartResponse.class);
    }

    public PagedResponse<SparePartResponse> getAllSpareParts(int page, int size, String sortBy, String sortDir) {
        return getAllSpareParts(page, size, sortBy, sortDir, null);
    }

    public PagedResponse<SparePartResponse> getAllSpareParts(int page, int size, String sortBy, String sortDir, String keyword) {
        log.info("Fetching all spare parts. Page: {}, Size: {}, SortBy: {}, SortDir: {}, Keyword: {}", 
                page, size, sortBy, sortDir, keyword);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SparePart> sparePartsPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            sparePartsPage = sparePartRepository.findSparePartsWithKeyword(keyword.trim(), pageable);
        } else {
            sparePartsPage = sparePartRepository.findAll(pageable);
        }
        
        List<SparePartResponse> content = sparePartsPage.getContent().stream()
                .map(sparePart -> modelMapper.map(sparePart, SparePartResponse.class))
                .toList();

        return new PagedResponse<>(content, sparePartsPage.getNumber(), sparePartsPage.getSize(),
                sparePartsPage.getTotalElements(), sparePartsPage.getTotalPages(), sparePartsPage.isLast());
    }
    
    public SparePartResponse updateSparePart(Long id, UpdateSparePartRequest request) {
        log.info("Updating spare part with ID: {}", id);
        SparePart sparePart = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", id));
        
        modelMapper.map(request, sparePart);
        
        SparePart updatedSparePart = sparePartRepository.save(sparePart);
        log.info("Successfully updated spare part with ID: {}", updatedSparePart.getId());
        return modelMapper.map(updatedSparePart, SparePartResponse.class);
    }

    public void hideSparePart(Long id) {
        log.info("Hiding spare part with ID: {}", id);
        SparePart sparePart = sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SparePart", "id", id));
        sparePart.setStatus(SparePartStatus.INACTIVE);
        sparePartRepository.save(sparePart);
        log.info("Successfully hid spare part with ID: {}", id);
    }
} 