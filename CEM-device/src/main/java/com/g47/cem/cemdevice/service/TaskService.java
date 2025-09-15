package com.g47.cem.cemdevice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemdevice.dto.request.ApproveServiceRequestRequest;
import com.g47.cem.cemdevice.dto.request.AssignTaskRequest;
import com.g47.cem.cemdevice.dto.request.CreateTaskRequest;
import com.g47.cem.cemdevice.dto.request.RejectServiceRequestRequest;
import com.g47.cem.cemdevice.dto.request.TaskActionRequest;
import com.g47.cem.cemdevice.dto.request.UpdateTaskRequest;
import com.g47.cem.cemdevice.dto.request.UpdateTaskStatusRequest;
import com.g47.cem.cemdevice.dto.response.TaskHistoryResponse;
import com.g47.cem.cemdevice.dto.response.TaskResponse;
import com.g47.cem.cemdevice.dto.response.TaskStatisticsResponse;
import com.g47.cem.cemdevice.dto.response.TechnicianWorkScheduleResponse;
import com.g47.cem.cemdevice.entity.CustomerDevice;
import com.g47.cem.cemdevice.entity.ServiceRequest;
import com.g47.cem.cemdevice.entity.ServiceRequestHistory;
import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.entity.TaskHistory;
import com.g47.cem.cemdevice.enums.ServiceRequestStatus;
import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;
import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import com.g47.cem.cemdevice.integration.UserIntegrationService;
import com.g47.cem.cemdevice.repository.CustomerDeviceRepository;
import com.g47.cem.cemdevice.repository.ServiceRequestHistoryRepository;
import com.g47.cem.cemdevice.repository.ServiceRequestRepository;
import com.g47.cem.cemdevice.repository.TaskHistoryRepository;
import com.g47.cem.cemdevice.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class for Task operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestHistoryRepository serviceRequestHistoryRepository;
    private final CustomerDeviceRepository customerDeviceRepository;
    private final UserIntegrationService userIntegrationService;
    private final com.g47.cem.cemdevice.repository.TechnicianProfileRepository technicianProfileRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new task manually (by Support Team)
     */
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, String createdBy) {
        log.debug("Creating new task: {} for customer device: {}", request.getTitle(), request.getCustomerDeviceId());
        
        // Verify the customer device exists and belongs to the customer
        CustomerDevice customerDevice = customerDeviceRepository.findById(request.getCustomerDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("CustomerDevice", "id", request.getCustomerDeviceId()));
        
        if (!customerDevice.getCustomerId().equals(request.getCustomerId())) {
            throw new BusinessException("Device does not belong to the specified customer");
        }
        
        // Generate unique task ID
        String taskId = generateTaskId();
        
        Task task = Task.builder()
                .taskId(taskId)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .status(TaskStatus.PENDING)
                .priority(request.getPriority())
                .customerDevice(customerDevice)
                .customerId(request.getCustomerId())
                .scheduledDate(request.getScheduledDate())
                .estimatedDurationHours(request.getEstimatedDurationHours())
                .serviceLocation(request.getServiceLocation())
                .customerContactInfo(request.getCustomerContactInfo())
                .supportNotes(request.getSupportNotes())
                .createdBy(createdBy)
                .build();
        
        task = taskRepository.save(task);
        
        // Create initial history entry
        createTaskHistory(task, TaskStatus.PENDING, "Task created", createdBy, "SUPPORT_TEAM");
        
        log.info("Task created successfully: {}", taskId);
        return mapToTaskResponse(task);
    }
    
    /**
     * Approve service request and convert to task
     */
    @Transactional
    public TaskResponse approveServiceRequest(Long serviceRequestId, ApproveServiceRequestRequest request, String approvedBy) {
        log.debug("Approving service request: {} and converting to task", serviceRequestId);
        
        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", serviceRequestId));
        
        if (!serviceRequest.isPending()) {
            throw new BusinessException("Service request is not in PENDING status");
        }
        
        // Update service request status
        serviceRequest.setStatus(ServiceRequestStatus.APPROVED);
        serviceRequest.setApprovedBy(approvedBy);
        serviceRequest.setApprovedAt(LocalDateTime.now());
        serviceRequestRepository.save(serviceRequest);
        
        // Create service request history
        ServiceRequestHistory serviceHistory = ServiceRequestHistory.builder()
                .serviceRequest(serviceRequest)
                .status(ServiceRequestStatus.APPROVED)
                .comment("Service request approved and converted to task")
                .updatedBy(approvedBy)
                .build();
        serviceRequestHistoryRepository.save(serviceHistory);
        
        // Convert service request type to task type
        TaskType taskType = serviceRequest.getType() == com.g47.cem.cemdevice.enums.ServiceRequestType.MAINTENANCE 
                          ? TaskType.MAINTENANCE 
                          : TaskType.WARRANTY;
        
        // Create task
        String taskId = generateTaskId();
        Task task = Task.builder()
                .taskId(taskId)
                .title(request.getTaskTitle())
                .description(serviceRequest.getDescription() + 
                           (request.getAdditionalNotes() != null ? "\n\nAdditional Notes: " + request.getAdditionalNotes() : ""))
                .type(request.getTaskType() != null ? request.getTaskType() : taskType)
                .status(TaskStatus.PENDING)
                .priority(request.getPriority())
                .serviceRequestId(serviceRequestId)
                .customerDevice(serviceRequest.getDevice())
                .customerId(serviceRequest.getCustomerId())
                .scheduledDate(request.getScheduledDate())
                .estimatedDurationHours(request.getEstimatedDurationHours())
                .serviceLocation(request.getServiceLocation())
                .customerContactInfo(request.getCustomerContactInfo())
                // No estimated cost in approval flow anymore
                .supportNotes(request.getSupportNotes())
                .createdBy(approvedBy)
                .build();
        
        task = taskRepository.save(task);
        
        // Create task history
        createTaskHistory(task, TaskStatus.PENDING, "Task created from approved service request: " + serviceRequest.getRequestId(), approvedBy, "SUPPORT_TEAM");
        
        log.info("Service request {} approved and converted to task {}", serviceRequest.getRequestId(), taskId);
        return mapToTaskResponse(task);
    }
    
    /**
     * Reject service request
     */
    @Transactional
    

    public void rejectServiceRequest(Long serviceRequestId, RejectServiceRequestRequest request, String rejectedBy) {
        log.debug("Rejecting service request: {}", serviceRequestId);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceRequest", "id", serviceRequestId));

        // Idempotency and state validation
        if (!serviceRequest.isPending()) {
            if (serviceRequest.isRejected()) {
                throw new BusinessException("Service request already rejected");
            }
            throw new BusinessException("Service request is not in PENDING status");
        }

        String trimmedReason = request.getRejectionReason() != null ? request.getRejectionReason().trim() : null;
        if (trimmedReason == null || trimmedReason.length() < 10) {
            throw new BusinessException("Rejection reason must be between 10 and 2000 characters");
        }

        // Update service request status and audit fields
        serviceRequest.setStatus(ServiceRequestStatus.REJECTED);
        serviceRequest.setRejectionReason(trimmedReason);
        serviceRequest.setRejectedBy(rejectedBy);
        serviceRequest.setRejectedAt(LocalDateTime.now());
        serviceRequestRepository.save(serviceRequest);

        // Create history entry directly - simple and reliable
        try {
            ServiceRequestHistory history = ServiceRequestHistory.builder()
                    .serviceRequest(serviceRequest)
                    .status(ServiceRequestStatus.REJECTED)
                    .comment("Service request rejected: " + trimmedReason)
                    .updatedBy(rejectedBy)
                    .build();
            serviceRequestHistoryRepository.save(history);
        } catch (Exception ex) {
            // Log but don't fail the main operation
            log.warn("Failed to create history entry for rejected service request {}: {}", serviceRequestId, ex.getMessage());
        }

        log.info("Service request {} rejected by {}", serviceRequest.getRequestId(), rejectedBy);
    }

    
    /**
     * Assign task to technician (by TechLead)
     */
    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request, String assignedBy) {
        log.debug("Assigning task: {} to technician: {}", taskId, request.getTechnicianId());
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        if (!task.canBeAssigned()) {
            throw new BusinessException("Task cannot be assigned in current status: " + task.getStatus());
        }
        
        // Update task assignment
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedTechnicianId(request.getTechnicianId());
        task.setAssignedBy(assignedBy);
        task.setAssignedAt(LocalDateTime.now());
        task.setScheduledDate(request.getScheduledDate());
        task.setTechleadNotes(request.getTechleadNotes());
        
        task = taskRepository.save(task);
        
        // Create task history
        createTaskHistory(task, TaskStatus.ASSIGNED, 
                "Task assigned to technician (ID: " + request.getTechnicianId() + ")" +
                (request.getTechleadNotes() != null ? " - " + request.getTechleadNotes() : ""), 
                assignedBy, "LEAD_TECH");
        
        log.info("Task {} assigned to technician {} by {}", task.getTaskId(), request.getTechnicianId(), assignedBy);
        return mapToTaskResponse(task);
    }
    
    /**
     * Accept task (by Technician)
     */
    @Transactional
    public TaskResponse acceptTask(Long taskId, TaskActionRequest request, String technicianUsername, Long technicianId) {
        log.debug("Technician {} accepting task: {}", technicianId, taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        if (!task.canBeAcceptedOrRejected()) {
            throw new BusinessException("Task cannot be accepted in current status: " + task.getStatus());
        }
        
        if (!task.getAssignedTechnicianId().equals(technicianId)) {
            throw new BusinessException("Task is not assigned to this technician");
        }
        
        // Update task status
        task.setStatus(TaskStatus.ACCEPTED);
        if (request.getComment() != null) {
            task.setTechnicianNotes(request.getComment());
        }
        
        task = taskRepository.save(task);
        
        // Create task history
        createTaskHistory(task, TaskStatus.ACCEPTED, 
                "Task accepted by technician" + (request.getComment() != null ? " - " + request.getComment() : ""), 
                technicianUsername, "TECHNICIAN");
        
        log.info("Task {} accepted by technician {}", task.getTaskId(), technicianId);
        return mapToTaskResponse(task);
    }
    
    /**
     * Reject task (by Technician)
     */
    @Transactional
    public TaskResponse rejectTask(Long taskId, TaskActionRequest request, String technicianUsername, Long technicianId) {
        log.debug("Technician {} rejecting task: {}", technicianId, taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        if (!task.canBeAcceptedOrRejected()) {
            throw new BusinessException("Task cannot be rejected in current status: " + task.getStatus());
        }
        
        if (!task.getAssignedTechnicianId().equals(technicianId)) {
            throw new BusinessException("Task is not assigned to this technician");
        }
        
        if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new BusinessException("Rejection reason is required");
        }
        
        // Update task status
        task.setStatus(TaskStatus.REJECTED);
        task.setRejectionReason(request.getRejectionReason());
        task.setRejectedBy(technicianUsername);
        task.setRejectedAt(LocalDateTime.now());
        
        task = taskRepository.save(task);
        
        // Create task history
        createTaskHistory(task, TaskStatus.REJECTED, 
                "Task rejected by technician: " + request.getRejectionReason(), 
                technicianUsername, "TECHNICIAN");
        
        log.info("Task {} rejected by technician {}: {}", task.getTaskId(), technicianId, request.getRejectionReason());
        return mapToTaskResponse(task);
    }
    
    /**
     * Start working on task (by Technician)
     */
    @Transactional
    public TaskResponse startTask(Long taskId, TaskActionRequest request, String technicianUsername, Long technicianId) {
        log.debug("Technician {} starting work on task: {}", technicianId, taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        if (!task.canBeWorkedOn()) {
            throw new BusinessException("Task cannot be started in current status: " + task.getStatus());
        }
        
        if (!task.getAssignedTechnicianId().equals(technicianId)) {
            throw new BusinessException("Task is not assigned to this technician");
        }
        
        // Update task status
        task.setStatus(TaskStatus.IN_PROGRESS);
        if (request.getComment() != null) {
            String existingNotes = task.getTechnicianNotes();
            task.setTechnicianNotes(existingNotes != null ? existingNotes + "\n" + request.getComment() : request.getComment());
        }
        
        task = taskRepository.save(task);
        
        // Create task history
        createTaskHistory(task, TaskStatus.IN_PROGRESS, 
                "Work started on task" + (request.getComment() != null ? " - " + request.getComment() : ""), 
                technicianUsername, "TECHNICIAN");
        
        log.info("Work started on task {} by technician {}", task.getTaskId(), technicianId);
        return mapToTaskResponse(task);
    }
    
    /**
     * Complete task (by Technician)
     */
    @Transactional
    public TaskResponse completeTask(Long taskId, TaskActionRequest request, String technicianUsername, Long technicianId) {
        log.debug("Technician {} completing task: {}", technicianId, taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        if (!task.canBeWorkedOn()) {
            throw new BusinessException("Task cannot be completed in current status: " + task.getStatus());
        }
        
        if (!task.getAssignedTechnicianId().equals(technicianId)) {
            throw new BusinessException("Task is not assigned to this technician");
        }
        
        // Update task status
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        if (request.getComment() != null) {
            task.setCompletionNotes(request.getComment());
        }
        
        task = taskRepository.save(task);
        
        // Update related service request if exists
        if (task.getServiceRequestId() != null) {
            ServiceRequest serviceRequest = serviceRequestRepository.findById(task.getServiceRequestId()).orElse(null);
            if (serviceRequest != null && !serviceRequest.isCompleted()) {
                serviceRequest.setStatus(ServiceRequestStatus.COMPLETED);
                serviceRequest.setCompletedAt(LocalDateTime.now());
                serviceRequestRepository.save(serviceRequest);
                
                // Create service request history
                ServiceRequestHistory serviceHistory = ServiceRequestHistory.builder()
                        .serviceRequest(serviceRequest)
                        .status(ServiceRequestStatus.COMPLETED)
                        .comment("Service request completed via task: " + task.getTaskId())
                        .updatedBy(technicianUsername)
                        .build();
                serviceRequestHistoryRepository.save(serviceHistory);
            }
        }
        
        // Create task history
        createTaskHistory(task, TaskStatus.COMPLETED, 
                "Task completed" + (request.getComment() != null ? " - " + request.getComment() : ""), 
                technicianUsername, "TECHNICIAN");
        
        log.info("Task {} completed by technician {}", task.getTaskId(), technicianId);
        return mapToTaskResponse(task);
    }

    /**
     * Update task status (generic flow used by Technician UI)
     */
    @Transactional
    public TaskResponse updateTaskStatusByTechnician(Long taskId, UpdateTaskStatusRequest request, String technicianUsername, Long technicianId) {
        log.debug("Technician {} updating status of task {} to {}", technicianId, taskId, request.getStatus());
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        if (!technicianId.equals(task.getAssignedTechnicianId())) {
            throw new BusinessException("Task is not assigned to this technician");
        }
        TaskStatus target = request.getStatus();
        TaskStatus current = task.getStatus();
        // Allowed transitions for technician
        boolean allowed =
            (current == TaskStatus.ASSIGNED && (target == TaskStatus.ACCEPTED)) ||
            (current == TaskStatus.ACCEPTED && (target == TaskStatus.IN_PROGRESS)) ||
            (current == TaskStatus.IN_PROGRESS && (target == TaskStatus.COMPLETED));
        if (!allowed) {
            throw new BusinessException("Invalid status transition from " + current + " to " + target);
        }
        task.setStatus(target);
        if (target == TaskStatus.IN_PROGRESS) {
            // append technician notes on start
            if (request.getComment() != null) {
                String existingNotes = task.getTechnicianNotes();
                task.setTechnicianNotes(existingNotes != null ? existingNotes + "\n" + request.getComment() : request.getComment());
            }
        }
        if (target == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
            if (request.getComment() != null) {
                task.setCompletionNotes(request.getComment());
            }
        }
        task = taskRepository.save(task);
        createTaskHistory(task, target, "Status updated to " + target + (request.getComment() != null ? ": " + request.getComment() : ""), technicianUsername, "TECHNICIAN");
        return mapToTaskResponse(task);
    }
    
    /**
     * Update task (by Support Team or TechLead)
     */
    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, String updatedBy, String userRole) {
        log.debug("Updating task: {}", taskId);
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        
        boolean statusChanged = false;
        TaskStatus oldStatus = task.getStatus();
        
        // Update fields if provided
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null && request.getStatus() != oldStatus) {
            task.setStatus(request.getStatus());
            statusChanged = true;
        }
        if (request.getScheduledDate() != null) {
            task.setScheduledDate(request.getScheduledDate());
        }
        // Frontend alias mapping: preferredCompletionDate -> scheduledDate
        if (request.getPreferredCompletionDate() != null) {
            task.setScheduledDate(request.getPreferredCompletionDate());
        }
        if (request.getEstimatedDurationHours() != null) {
            task.setEstimatedDurationHours(request.getEstimatedDurationHours());
        }
        if (request.getServiceLocation() != null) {
            task.setServiceLocation(request.getServiceLocation());
        }
        if (request.getCustomerContactInfo() != null) {
            task.setCustomerContactInfo(request.getCustomerContactInfo());
        }
        // Cost fields removed
        if (request.getSupportNotes() != null) {
            task.setSupportNotes(request.getSupportNotes());
        }
        // Frontend alias mapping: staffNotes -> supportNotes
        if (request.getStaffNotes() != null) {
            task.setSupportNotes(request.getStaffNotes());
        }
        if (request.getTechleadNotes() != null) {
            task.setTechleadNotes(request.getTechleadNotes());
        }
        if (request.getTechnicianNotes() != null) {
            task.setTechnicianNotes(request.getTechnicianNotes());
        }
        if (request.getCompletionNotes() != null) {
            task.setCompletionNotes(request.getCompletionNotes());
        }
        if (request.getType() != null) {
            task.setType(request.getType());
        }
        
        task = taskRepository.save(task);
        
        // Create history entry for significant changes
        if (statusChanged) {
            createTaskHistory(task, task.getStatus(), "Task status updated to " + task.getStatus(), updatedBy, userRole);
        }
        
        log.info("Task {} updated by {}", task.getTaskId(), updatedBy);
        return mapToTaskResponse(task);
    }

    /**
     * Delete task by ID (by Support Team or TechLead)
     */
    @Transactional
    public void deleteTask(Long taskId) {
        log.debug("Deleting task: {}", taskId);
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }
        taskRepository.deleteById(taskId);
        log.info("Task {} deleted", taskId);
    }
    
    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return mapToTaskResponse(task);
    }
    
    /**
     * Get task by task ID
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskByTaskId(String taskId) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "taskId", taskId));
        return mapToTaskResponse(task);
    }
    
    /**
     * Get all tasks with pagination
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        Page<Task> tasks = taskRepository.findAll(pageable);
        return tasks.map(this::mapToTaskResponse);
    }
    
    /**
     * Get tasks by status
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByStatus(status, pageable);
        return tasks.map(this::mapToTaskResponse);
    }
    
    /**
     * Get tasks assigned to technician
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByTechnician(Long technicianId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByAssignedTechnicianId(technicianId, pageable);
        return tasks.map(this::mapToTaskResponse);
    }
    
    /**
     * Get all tasks with filters for staff
     */
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasksWithFilters(String search, TaskStatus status, 
            TaskPriority priority, TaskType type, Pageable pageable) {
        
        log.debug("Getting tasks with filters - search: {}, status: {}, priority: {}, type: {}", 
                search, status, priority, type);
        
        // If no filters applied, return all tasks
        if (search == null && status == null && priority == null && type == null) {
            return getAllTasks(pageable);
        }
        
        Page<Task> tasks;
        
        // Apply filters based on what's provided
        if (search != null && !search.trim().isEmpty()) {
            // Search by title, description, or task ID
            tasks = taskRepository.findByKeyword(search.trim(), pageable);
        } else if (status != null && priority != null && type != null) {
            // All three filters
            tasks = taskRepository.findByStatusAndPriorityAndType(status, priority, type, pageable);
        } else if (status != null && priority != null) {
            // Status and priority
            tasks = taskRepository.findByStatusAndPriority(status, priority, pageable);
        } else if (status != null && type != null) {
            // Status and type
            tasks = taskRepository.findByStatusAndType(status, type, pageable);
        } else if (priority != null && type != null) {
            // Priority and type
            tasks = taskRepository.findByPriorityAndType(priority, type, pageable);
        } else if (status != null) {
            // Status only
            tasks = taskRepository.findByStatus(status, pageable);
        } else if (priority != null) {
            // Priority only
            tasks = taskRepository.findByPriority(priority, pageable);
        } else if (type != null) {
            // Type only
            tasks = taskRepository.findByType(type, pageable);
        } else {
            // Fallback to all tasks
            tasks = taskRepository.findAll(pageable);
        }
        
        return tasks.map(this::mapToTaskResponse);
    }
    
    /**
     * Get technician work schedule
     */
    @Transactional(readOnly = true)
    public List<TechnicianWorkScheduleResponse> getTechnicianWorkSchedule(Long technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Task> tasks = taskRepository.findByAssignedTechnicianIdAndScheduledDateBetween(technicianId, startDate, endDate);
        return tasks.stream()
                .map(this::mapToWorkScheduleResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get available technicians for task assignment
     */
    @Transactional
    public List<com.g47.cem.cemdevice.dto.response.TechnicianInfoResponse> getAvailableTechnicians(String bearerToken) {
        log.info("Fetching available technicians from authentication service");
        
        // Get all users with TECHNICIAN role from authentication service
        List<UserIntegrationService.UserDto> technicianUsers = userIntegrationService.getUsersByRole("TECHNICIAN", bearerToken);
        
        if (technicianUsers.isEmpty()) {
            log.warn("No technicians found in authentication service");
            return List.of();
        }
        
        log.info("Found {} technicians from authentication service", technicianUsers.size());
        
        // Convert to TechnicianInfoResponse with real task statistics
        return technicianUsers.stream()
            .map(this::mapUserToTechnicianInfo)
            .collect(Collectors.toList());
    }
    
    /**
     * Initialize technician profiles for all existing technicians
     */
    @Transactional
    public void initializeTechnicianProfiles(String bearerToken) {
        try {
            log.info("Initializing technician profiles for existing users...");
            
            // Get all technicians from authentication service
            List<UserIntegrationService.UserDto> technicians = 
                userIntegrationService.getUsersByRole("TECHNICIAN", bearerToken);
            
            int createdCount = 0;
            int skippedCount = 0;
            
            for (UserIntegrationService.UserDto technician : technicians) {
                Long technicianId = technician.getId();
                
                // Check if profile already exists
                if (technicianProfileRepository.existsByUserId(technicianId)) {
                    log.debug("Profile already exists for technician: {}", technicianId);
                    skippedCount++;
                    continue;
                }
                
                // Create default profile
                com.g47.cem.cemdevice.entity.TechnicianProfile defaultProfile = 
                    createDefaultTechnicianProfile(technician);
                
                try {
                    technicianProfileRepository.save(defaultProfile);
                    log.info("Created profile for technician: {} ({})", 
                            technician.getFullName(), technicianId);
                    createdCount++;
                } catch (Exception e) {
                    log.warn("Failed to create profile for technician {}: {}", 
                            technicianId, e.getMessage());
                }
            }
            
            log.info("Technician profile initialization completed. Created: {}, Skipped: {}", 
                    createdCount, skippedCount);
            
        } catch (Exception e) {
            log.error("Error during technician profile initialization: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create a default technician profile
     */
    private com.g47.cem.cemdevice.entity.TechnicianProfile createDefaultTechnicianProfile(
            UserIntegrationService.UserDto user) {
        
        Long technicianId = user.getId();
        
        // Generate initial values based on user data
        String location = "Main Office"; // Default location
        String skills = "General Maintenance, Equipment Repair";
        String phone = user.getPhone();
        
        // Vary some values based on user ID to make it more realistic
        String[] locations = {"Downtown Office", "Uptown Branch", "West Side Center", "East Side Hub", "North District"};
        String[] skillSets = {
            "Electrical, HVAC, Plumbing",
            "Electronics, Networking, Software", 
            "Mechanical, Hydraulics, Pneumatics",
            "Welding, Fabrication, Assembly",
            "Diagnostics, Repair, Maintenance"
        };
        
        int userIndex = (int) (technicianId % locations.length);
        location = locations[userIndex];
        skills = skillSets[userIndex];
        
        return com.g47.cem.cemdevice.entity.TechnicianProfile.builder()
            .userId(technicianId)
            .phone(phone)
            .location(location)
            .skills(skills)
            .specializations("General Repair, Equipment Maintenance, Safety Inspection")
            .certifications("Basic Safety Certification, Equipment Operation License")
            .experienceYears(Math.max(1, (int) (technicianId % 10) + 1)) // 1-10 years
            .maxConcurrentTasks(6 + (int) (technicianId % 3)) // 6-8 tasks
            .workingHoursStart(java.time.LocalTime.of(8, 0)) // 08:00
            .workingHoursEnd(java.time.LocalTime.of(17, 0))   // 17:00
            .workingDays("MON,TUE,WED,THU,FRI") // Monday to Friday
            .emergencyContactName("Emergency Contact - Please Update")
            .emergencyContactPhone("000-000-0000")
            .notes("Auto-generated profile - please review and update information")
            .isActive(true)
            .build();
    }

    /**
     * Map user data from authentication service to TechnicianInfoResponse with real task statistics
     */
    private com.g47.cem.cemdevice.dto.response.TechnicianInfoResponse mapUserToTechnicianInfo(UserIntegrationService.UserDto user) {
        Long technicianId = user.getId();
        
        // Get real task statistics for this technician from database
        long currentTasks = taskRepository.countByAssignedTechnicianIdAndStatus(technicianId, TaskStatus.ASSIGNED) +
                           taskRepository.countByAssignedTechnicianIdAndStatus(technicianId, TaskStatus.IN_PROGRESS);
        long totalTasks = taskRepository.countByAssignedTechnicianId(technicianId);
        long completedTasks = taskRepository.countByAssignedTechnicianIdAndStatus(technicianId, TaskStatus.COMPLETED);
        
        // Calculate completion rate
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        // Calculate average completion days from completed tasks
        int averageCompletionDays = 3; // Default value
        try {
            // This could be enhanced with actual database query for average completion time
            List<Task> completedTaskList = taskRepository.findAll().stream()
                .filter(task -> technicianId.equals(task.getAssignedTechnicianId()) && task.getStatus() == TaskStatus.COMPLETED)
                .collect(Collectors.toList());
            
            if (!completedTaskList.isEmpty()) {
                // Simple calculation - could be more sophisticated
                averageCompletionDays = Math.max(1, Math.min(10, completedTaskList.size() / 2 + 2));
            }
        } catch (Exception e) {
            log.debug("Could not calculate average completion days for technician {}: {}", technicianId, e.getMessage());
        }
        
        // Get real technician profile data from database
        Optional<com.g47.cem.cemdevice.entity.TechnicianProfile> profileOpt = 
            technicianProfileRepository.findByUserId(technicianId);
        
        String location = "Main Office"; // Default
        String skills = "General Maintenance"; // Default
        String phone = user.getPhone();
        int maxConcurrentTasks = 8; // Default
        
        if (profileOpt.isPresent()) {
            com.g47.cem.cemdevice.entity.TechnicianProfile profile = profileOpt.get();
            location = profile.getLocation() != null ? profile.getLocation() : location;
            skills = profile.getSkills() != null ? profile.getSkills() : skills;
            phone = profile.getPhone() != null ? profile.getPhone() : phone;
            maxConcurrentTasks = profile.getMaxConcurrentTasks() != null ? profile.getMaxConcurrentTasks() : maxConcurrentTasks;
        } else {
            // Create default profile for new technician
            log.info("Creating default profile for technician: {}", technicianId);
            com.g47.cem.cemdevice.entity.TechnicianProfile defaultProfile = 
                com.g47.cem.cemdevice.entity.TechnicianProfile.builder()
                    .userId(technicianId)
                    .phone(phone)
                    .location(location)
                    .skills(skills)
                    .specializations("General Repair, Equipment Maintenance")
                    .certifications("Basic Safety Certification")
                    .experienceYears(1) // Default 1 year experience
                    .maxConcurrentTasks(maxConcurrentTasks)
                    .workingHoursStart(java.time.LocalTime.of(8, 0)) // 08:00
                    .workingHoursEnd(java.time.LocalTime.of(17, 0))   // 17:00
                    .workingDays("MON,TUE,WED,THU,FRI") // Monday to Friday
                    .emergencyContactName("Please update emergency contact")
                    .emergencyContactPhone("Please update emergency phone")
                    .notes("New technician - profile needs to be completed")
                    .isActive(true)
                    .build();
            try {
                technicianProfileRepository.save(defaultProfile);
                log.info("Created default profile for technician {} with working days: {}", 
                        technicianId, defaultProfile.getWorkingDays());
            } catch (Exception e) {
                log.warn("Could not create default profile for technician {}: {}", technicianId, e.getMessage());
            }
        }
        
        // Calculate workload percentage using real max concurrent tasks from profile
        int workloadPercentage = Math.min((int) ((currentTasks * 100) / maxConcurrentTasks), 100);
        
        // Determine availability status based on current workload
        String availabilityStatus;
        double workloadRatio = (double) currentTasks / maxConcurrentTasks;
        if (currentTasks == 0) {
            availabilityStatus = "AVAILABLE";
        } else if (workloadRatio <= 0.5) {
            availabilityStatus = "BUSY";
        } else if (workloadRatio <= 0.8) {
            availabilityStatus = "OVERLOADED";
        } else {
            availabilityStatus = "UNAVAILABLE";
        }
        
        // Calculate average rating based on performance metrics
        double averageRating;
        if (completionRate > 90) {
            averageRating = 4.8;
        } else if (completionRate > 80) {
            averageRating = 4.5;
        } else if (completionRate > 70) {
            averageRating = 4.2;
        } else if (completionRate > 50) {
            averageRating = 4.0;
        } else {
            averageRating = 3.8;
        }
        
        return com.g47.cem.cemdevice.dto.response.TechnicianInfoResponse.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .phone(phone != null ? phone : "+1-555-" + String.format("%04d", technicianId * 10))
            .currentTaskCount(currentTasks)
            .totalTaskCount(totalTasks)
            .completedTaskCount(completedTasks)
            .averageRating(averageRating)
            .isAvailable(!availabilityStatus.equals("UNAVAILABLE"))
            .workloadPercentage(workloadPercentage)
            .availabilityStatus(availabilityStatus)
            .maxConcurrentTasks(maxConcurrentTasks)
            .skills(skills != null ? skills : "General Maintenance")
            .location(location != null ? location : "Main Office")
            .completionRate(completionRate)
            .averageCompletionDays(averageCompletionDays)
            .lastActiveDate(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : 
                           LocalDateTime.now().minusHours(2).toString())
            .build();
    }

    /**
     * Validate if technician exists in authentication service
     */
    public boolean isValidTechnician(Long technicianId, String bearerToken) {
        if (technicianId == null) {
            return false;
        }
        
        try {
            return userIntegrationService.getUserById(technicianId, bearerToken)
                .map(user -> "TECHNICIAN".equals(user.getRole().getName()))
                .orElse(false);
        } catch (Exception e) {
            log.error("Error validating technician with ID: {}", technicianId, e);
            return false;
        }
    }

    /**
     * Get technician information by ID
     */
    public Optional<com.g47.cem.cemdevice.dto.response.TechnicianInfoResponse> getTechnicianById(Long technicianId, String bearerToken) {
        if (technicianId == null) {
            return Optional.empty();
        }
        
        try {
            return userIntegrationService.getUserById(technicianId, bearerToken)
                .filter(user -> "TECHNICIAN".equals(user.getRole().getName()))
                .map(this::mapUserToTechnicianInfo);
        } catch (Exception e) {
            log.error("Error fetching technician with ID: {}", technicianId, e);
            return Optional.empty();
        }
    }

    /**
     * Get task statistics
     */
    @Transactional(readOnly = true)
    public TaskStatisticsResponse getTaskStatistics() {
        Object[] stats = taskRepository.getTaskStatistics();
        if (stats != null && stats.length > 0) {
            Object[] data = (Object[]) stats[0];
            
            long totalTasks = taskRepository.count();
            long pendingTasks = data[0] != null ? ((Number) data[0]).longValue() : 0;
            long assignedTasks = data[1] != null ? ((Number) data[1]).longValue() : 0;
            long acceptedTasks = data[2] != null ? ((Number) data[2]).longValue() : 0;
            long inProgressTasks = data[3] != null ? ((Number) data[3]).longValue() : 0;
            long completedTasks = data[4] != null ? ((Number) data[4]).longValue() : 0;
            long rejectedTasks = data[5] != null ? ((Number) data[5]).longValue() : 0;
            
            long highPriorityTasks = taskRepository.countByPriority(com.g47.cem.cemdevice.enums.TaskPriority.HIGH);
            long criticalPriorityTasks = taskRepository.countByPriority(com.g47.cem.cemdevice.enums.TaskPriority.CRITICAL);
            
            List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDateTime.now());
            long overdueCount = overdueTasks.size();
            
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
            double rejectionRate = totalTasks > 0 ? (double) rejectedTasks / totalTasks * 100 : 0;
            
            return TaskStatisticsResponse.builder()
                    .totalTasks(totalTasks)
                    .pendingTasks(pendingTasks)
                    .assignedTasks(assignedTasks)
                    .acceptedTasks(acceptedTasks)
                    .inProgressTasks(inProgressTasks)
                    .completedTasks(completedTasks)
                    .rejectedTasks(rejectedTasks)
                    .highPriorityTasks(highPriorityTasks)
                    .criticalPriorityTasks(criticalPriorityTasks)
                    .overdueTasks(overdueCount)
                    .completionRate(completionRate)
                    .rejectionRate(rejectionRate)
                    .build();
        }
        
        return TaskStatisticsResponse.builder().build();
    }
    
    // Helper methods
    
    private void createTaskHistory(Task task, TaskStatus status, String comment, String updatedBy, String userRole) {
        TaskHistory history = TaskHistory.builder()
                .task(task)
                .status(status)
                .comment(comment)
                .updatedBy(updatedBy)
                .userRole(userRole)
                .build();
        taskHistoryRepository.save(history);
    }
    
    private String generateTaskId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long count = taskRepository.count() + 1;
        return String.format("TSK-%s-%04d", timestamp, count);
    }
    
    private TaskResponse mapToTaskResponse(Task task) {
        TaskResponse response = modelMapper.map(task, TaskResponse.class);
        
        // Map additional fields
        if (task.getCustomerDevice() != null && task.getCustomerDevice().getDevice() != null) {
            response.setDeviceName(task.getCustomerDevice().getDevice().getName());
            response.setDeviceModel(task.getCustomerDevice().getDevice().getModel());
            response.setSerialNumber(task.getCustomerDevice().getDevice().getSerialNumber());
        }
        
        // Map scheduledDate to preferredCompletionDate for frontend compatibility
        response.setPreferredCompletionDate(task.getScheduledDate());
        
        // Load and map history
        List<TaskHistory> historyList = taskHistoryRepository.findByTaskIdOrderByCreatedAtDesc(task.getId());
        List<TaskHistoryResponse> historyResponses = historyList.stream()
                .map(h -> modelMapper.map(h, TaskHistoryResponse.class))
                .collect(Collectors.toList());
        response.setHistory(historyResponses);
        
        return response;
    }
    
    private TechnicianWorkScheduleResponse mapToWorkScheduleResponse(Task task) {
        return TechnicianWorkScheduleResponse.builder()
                .taskId(task.getId())
                .taskNumber(task.getTaskId())
                .title(task.getTitle())
                .type(task.getType())
                .status(task.getStatus())
                .priority(task.getPriority())
                .scheduledDate(task.getScheduledDate())
                .estimatedDurationHours(task.getEstimatedDurationHours())
                .serviceLocation(task.getServiceLocation())
                .deviceName(task.getCustomerDevice() != null && task.getCustomerDevice().getDevice() != null ? 
                           task.getCustomerDevice().getDevice().getName() : null)
                .deviceModel(task.getCustomerDevice() != null && task.getCustomerDevice().getDevice() != null ? 
                            task.getCustomerDevice().getDevice().getModel() : null)
                .serialNumber(task.getCustomerDevice() != null && task.getCustomerDevice().getDevice() != null ? 
                             task.getCustomerDevice().getDevice().getSerialNumber() : null)
                .description(task.getDescription())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
