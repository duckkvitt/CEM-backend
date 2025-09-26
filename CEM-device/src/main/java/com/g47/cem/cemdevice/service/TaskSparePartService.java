package com.g47.cem.cemdevice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.request.ExportTaskSparePartRequest;
import com.g47.cem.cemdevice.dto.response.TaskSparePartUsageResponse;
import com.g47.cem.cemdevice.entity.InventoryTransaction;
import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.enums.InventoryItemType;
import com.g47.cem.cemdevice.enums.InventoryTransactionType;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService;
import com.g47.cem.cemdevice.repository.InventoryTransactionRepository;
import com.g47.cem.cemdevice.repository.TaskRepository;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService.TaskSparePartUsageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSparePartService {

    private final TaskRepository taskRepository;
    private final SparePartIntegrationService sparePartIntegrationService;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Transactional(readOnly = true)
    public List<TaskSparePartUsageResponse> getTaskSpareParts(Long taskId, String bearerToken) {
        // Delegate to spareparts service to read usages with auth
        List<TaskSparePartUsageDto> usages = sparePartIntegrationService.getTaskUsages(taskId, bearerToken);
        return usages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TaskSparePartUsageResponse exportSparePart(ExportTaskSparePartRequest request, String createdBy, String bearerToken) {
        Task task = taskRepository.findById(request.getTaskId())
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.getTaskId()));

        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO;
        var usageDtoOpt = sparePartIntegrationService.exportSparePartForTask(task.getId(), request.getSparePartId(), request.getQuantityUsed(), request.getNotes(), bearerToken, unitPrice);
        var usageDto = usageDtoOpt.orElseThrow(() -> new BusinessException("Failed to export spare part for task"));
        
        // Create inventory transaction for the export
        createInventoryTransactionForSparePartExport(usageDto, task, createdBy);
        
        return toResponse(usageDto);
    }

    private TaskSparePartUsageResponse toResponse(TaskSparePartUsageDto usage) {
        return TaskSparePartUsageResponse.builder()
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
    
    /**
     * Create inventory transaction for spare part export
     */
    private void createInventoryTransactionForSparePartExport(TaskSparePartUsageDto usageDto, Task task, String createdBy) {
        try {
            InventoryTransaction transaction = InventoryTransaction.builder()
                .transactionNumber(generateTransactionNumber())
                .transactionType(InventoryTransactionType.EXPORT)
                .itemType(InventoryItemType.SPARE_PART)
                .itemId(usageDto.getSparePartId())
                .itemName(usageDto.getSparePartName())
                .quantity(usageDto.getQuantityUsed())
                .unitPrice(usageDto.getUnitPrice())
                .totalAmount(usageDto.getTotalCost())
                .referenceNumber(task.getTaskId())
                .referenceType("Task")
                .referenceId(task.getId())
                .transactionReason("Spare part export for task")
                .notes(usageDto.getNotes())
                .createdBy(createdBy)
                .build();
            
            inventoryTransactionRepository.save(transaction);
            log.info("Created inventory transaction for spare part export: {} x{} for task {}", 
                usageDto.getSparePartName(), usageDto.getQuantityUsed(), task.getId());
        } catch (Exception e) {
            log.error("Failed to create inventory transaction for spare part export: {}", e.getMessage(), e);
            // Don't throw exception here to avoid breaking the main export flow
        }
    }
    
    private String generateTransactionNumber() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}


