package com.g47.cem.cemdevice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.request.ExportTaskSparePartRequest;
import com.g47.cem.cemdevice.dto.response.TaskSparePartUsageResponse;
import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.integration.SparePartIntegrationService;
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
}


