package com.g47.cem.cemspareparts.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.dto.request.ExportSparePartForTaskRequest;
import com.g47.cem.cemspareparts.dto.response.SparePartUsageResponse;
import com.g47.cem.cemspareparts.entity.SparePartUsage;
import com.g47.cem.cemspareparts.exception.BusinessException;
import org.springframework.http.HttpStatus;
import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.repository.SparePartInventoryRepository;
import com.g47.cem.cemspareparts.repository.SparePartRepository;
import com.g47.cem.cemspareparts.repository.SparePartUsageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SparePartUsageService {

    private final SparePartInventoryRepository inventoryRepository;
    private final SparePartRepository sparePartRepository;
    private final SparePartUsageRepository usageRepository;

    @Transactional(readOnly = true)
    public List<SparePartUsageResponse> getUsagesByTask(Long taskId) {
        return usageRepository.findByTaskIdOrderByUsedAtDesc(taskId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public SparePartUsageResponse exportForTask(ExportSparePartForTaskRequest request, String createdBy) {
        var inventory = inventoryRepository.findBySparePartId(request.getSparePartId())
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Inventory not found for spare part: " + request.getSparePartId()));
        if (inventory.getQuantityInStock() < request.getQuantity()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Insufficient stock for spare part. Available: " + inventory.getQuantityInStock());
        }
        // decrement stock
        inventory.removeStock(request.getQuantity());
        inventoryRepository.save(inventory);

        SparePart part = sparePartRepository.findById(request.getSparePartId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Spare part not found: " + request.getSparePartId()));

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        SparePartUsage usage = SparePartUsage.builder()
            .taskId(request.getTaskId())
            .sparePartId(request.getSparePartId())
            .sparePartName(part.getPartName())
            .sparePartCode(part.getPartCode())
            .quantityUsed(request.getQuantity())
            .unitPrice(unitPrice)
            .totalCost(total)
            .notes(request.getNotes())
            .createdBy(createdBy)
            .build();

        usage = usageRepository.save(usage);
        log.info("Exported spare part {} x{} for task {}. New stock: {}", request.getSparePartId(), request.getQuantity(), request.getTaskId(), inventory.getQuantityInStock());
        return toResponse(usage);
    }

    private SparePartUsageResponse toResponse(SparePartUsage usage) {
        return SparePartUsageResponse.builder()
            .id(usage.getId())
            .taskId(usage.getTaskId())
            .sparePartId(usage.getSparePartId())
            .sparePartName(usage.getSparePartName())
            .sparePartCode(usage.getSparePartCode())
            .quantityUsed(usage.getQuantityUsed())
            .unitPrice(usage.getUnitPrice())
            .totalCost(usage.getTotalCost())
            .notes(usage.getNotes())
            .usedAt(usage.getUsedAt())
            .createdBy(usage.getCreatedBy())
            .build();
    }
}


