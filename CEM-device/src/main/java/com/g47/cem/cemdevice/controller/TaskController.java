package com.g47.cem.cemdevice.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.g47.cem.cemdevice.dto.request.ApproveServiceRequestRequest;
import com.g47.cem.cemdevice.dto.request.AssignTaskRequest;
import com.g47.cem.cemdevice.dto.request.CreateTaskRequest;
import com.g47.cem.cemdevice.dto.request.RejectServiceRequestRequest;
import com.g47.cem.cemdevice.dto.request.TaskActionRequest;
import com.g47.cem.cemdevice.dto.request.UpdateTaskRequest;
import com.g47.cem.cemdevice.dto.request.UpdateTaskStatusRequest;
import com.g47.cem.cemdevice.dto.response.ApiResponse;
import com.g47.cem.cemdevice.dto.response.TaskResponse;
import com.g47.cem.cemdevice.dto.response.TaskStatisticsResponse;
import com.g47.cem.cemdevice.dto.response.TechnicianInfoResponse;
import com.g47.cem.cemdevice.dto.response.TechnicianWorkScheduleResponse;
import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;
import com.g47.cem.cemdevice.service.TaskService;
import com.g47.cem.cemdevice.service.TaskSparePartService;
import com.g47.cem.cemdevice.dto.request.ExportTaskSparePartRequest;
import com.g47.cem.cemdevice.dto.response.TaskSparePartUsageResponse;
import com.g47.cem.cemdevice.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for task management operations
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management operations")
public class TaskController {
    
    private final TaskService taskService;
    private final TaskSparePartService taskSparePartService;
    private final JwtUtil jwtUtil;
    
    // ========== Support Team Endpoints ==========
    
    /**
     * Create a new task manually (Support Team)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('SUPPORT_TEAM')")
    @Operation(summary = "Create a new task", description = "Create a new task manually (Support Team only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        
        log.info("Creating new task for user: {}", authentication.getName());
        
        TaskResponse response = taskService.createTask(request, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task created successfully"));
    }
    
    /**
     * Approve service request and convert to task (Support Team)
     */
    @PostMapping("/service-requests/{serviceRequestId}/approve")
    @PreAuthorize("hasAuthority('SUPPORT_TEAM')")
    @Operation(summary = "Approve service request", description = "Approve service request and convert to task (Support Team only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> approveServiceRequest(
            @PathVariable Long serviceRequestId,
            @Valid @RequestBody ApproveServiceRequestRequest request,
            Authentication authentication) {
        
        log.info("Approving service request {} by user: {}", serviceRequestId, authentication.getName());
        
        TaskResponse response = taskService.approveServiceRequest(serviceRequestId, request, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service request approved and converted to task"));
    }
    
    /**
     * Reject service request (Support Team)
     */
    @PostMapping("/service-requests/{serviceRequestId}/reject")
    @PreAuthorize("hasAuthority('SUPPORT_TEAM')")
    @Operation(summary = "Reject service request", description = "Reject service request (Support Team only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> rejectServiceRequest(
            @PathVariable Long serviceRequestId,
            @Valid @RequestBody RejectServiceRequestRequest request,
            Authentication authentication) {
        
        log.info("Rejecting service request {} by user: {}", serviceRequestId, authentication.getName());
        
        taskService.rejectServiceRequest(serviceRequestId, request, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(null, "Service request rejected"));
    }
    
    /**
     * Get all tasks (Support Team and TechLead)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get all tasks", description = "Get all tasks with pagination (Support Team, TechLead, Manager, Admin)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {
        
        String mappedSortBy = mapSortField(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TaskResponse> response = status != null 
                ? taskService.getTasksByStatus(TaskStatus.valueOf(status.toUpperCase()), pageable)
                : taskService.getAllTasks(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks retrieved successfully"));
    }

    /**
     * Get all tasks for staff with advanced filtering (Support Team and TechLead)
     */
    @GetMapping("/staff")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get all tasks for staff", description = "Get all tasks with advanced filtering for staff (Support Team, TechLead, Manager, Admin)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasksForStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String type) {
        
        log.debug("Fetching tasks for staff with filters - search: {}, status: {}, priority: {}, type: {}", 
                search, status, priority, type);
        
        String mappedSortBy = mapSortField(sortBy);
        Sort sort = sortOrder.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        TaskStatus taskStatus = status != null ? TaskStatus.valueOf(status.toUpperCase()) : null;
        TaskPriority taskPriority = priority != null ? TaskPriority.valueOf(priority.toUpperCase()) : null;
        TaskType taskType = type != null ? TaskType.valueOf(type.toUpperCase()) : null;
        
        Page<TaskResponse> response = taskService.getAllTasksWithFilters(
                search, taskStatus, taskPriority, taskType, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks retrieved successfully"));
    }
    
    /**
     * Update task (Support Team and TechLead)
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH')")
    @Operation(summary = "Update task", description = "Update task details (Support Team and TechLead)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {
        
        log.info("Updating task {} by user: {}", taskId, authentication.getName());
        
        String userRole = extractUserRole(authentication);
        TaskResponse response = taskService.updateTask(taskId, request, authentication.getName(), userRole);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task updated successfully"));
    }
    
    /**
     * Delete task (Support Team and TechLead)
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH')")
    @Operation(summary = "Delete task", description = "Delete task by ID (Support Team and TechLead)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            Authentication authentication) {
        log.info("Deleting task {} by user: {}", taskId, authentication.getName());
        taskService.deleteTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null, "Task deleted successfully"));
    }
    
    // ========== TechLead Endpoints ==========
    
    /**
     * Get available technicians for task assignment (TechLead)
     */
    @GetMapping("/technicians")
    @PreAuthorize("hasAnyAuthority('LEAD_TECH', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get available technicians", description = "Get list of technicians available for task assignment")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<TechnicianInfoResponse>>> getTechniciansForAssignment(
            HttpServletRequest request) {
        
        log.info("Fetching available technicians for task assignment");
        
        // Extract bearer token from request header
        String bearerToken = request.getHeader("Authorization");
        
        List<TechnicianInfoResponse> technicians = taskService.getAvailableTechnicians(bearerToken);
        
        return ResponseEntity.ok(ApiResponse.success(technicians, "Technicians retrieved successfully"));
    }
    
    /**
     * Initialize technician profiles for all existing technicians (TechLead/Admin)
     */
    @PostMapping("/technicians/initialize-profiles")
    @PreAuthorize("hasAnyAuthority('LEAD_TECH', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Initialize technician profiles", 
               description = "Create default profiles for all technicians who don't have profiles yet")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Map<String, String>>> initializeTechnicianProfiles(
            HttpServletRequest request) {
        
        log.info("Initializing technician profiles");
        
        // Extract bearer token from request header
        String bearerToken = request.getHeader("Authorization");
        
        try {
            taskService.initializeTechnicianProfiles(bearerToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Technician profiles initialization completed successfully");
            response.put("status", "success");
            
            return ResponseEntity.ok(ApiResponse.success(response, "Profiles initialized successfully"));
        } catch (Exception e) {
            log.error("Error initializing technician profiles: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to initialize technician profiles: " + e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.status(500).body(ApiResponse.error("Initialization failed", response, 500));
        }
    }
    
    /**
     * Assign task to technician (TechLead)
     */
    @PostMapping("/{taskId}/assign")
    @PreAuthorize("hasAuthority('LEAD_TECH')")
    @Operation(summary = "Assign task to technician", description = "Assign task to technician (TechLead only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {
        
        log.info("Assigning task {} to technician {} by user: {}", taskId, request.getTechnicianId(), authentication.getName());
        
        TaskResponse response = taskService.assignTask(taskId, request, authentication.getName());
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task assigned successfully"));
    }
    
    /**
     * Get tasks assigned by TechLead
     */
    @GetMapping("/assigned-by-me")
    @PreAuthorize("hasAuthority('LEAD_TECH')")
    @Operation(summary = "Get tasks assigned by me", description = "Get tasks assigned by current TechLead")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasksAssignedByMe(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "assignedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        String mappedSortBy = mapSortField(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // This would need to be implemented in the service
        Page<TaskResponse> response = taskService.getAllTasks(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks retrieved successfully"));
    }
    
    // ========== Technician Endpoints ==========
    
    /**
     * Get tasks assigned to current technician
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Get my assigned tasks", description = "Get tasks assigned to current technician")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "assignedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        Long technicianId = extractTechnicianId(authentication);
        
        // Map frontend sortBy parameters to backend entity fields
        String mappedSortBy = mapSortField(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(mappedSortBy).descending() : Sort.by(mappedSortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // For now, ignore status filter in getMyTasks (can be enhanced later)
        Page<TaskResponse> response = taskService.getTasksByTechnician(technicianId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Tasks retrieved successfully"));
    }
    
    /**
     * Accept task (Technician)
     */
    @PostMapping("/{taskId}/accept")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Accept assigned task", description = "Accept assigned task (Technician only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> acceptTask(
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskActionRequest request,
            Authentication authentication) {
        
        Long technicianId = extractTechnicianId(authentication);
        log.info("Technician {} accepting task {}", technicianId, taskId);
        
        TaskActionRequest actionRequest = request != null ? request : new TaskActionRequest();
        TaskResponse response = taskService.acceptTask(taskId, actionRequest, authentication.getName(), technicianId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task accepted successfully"));
    }
    
    /**
     * Reject task (Technician)
     */
    @PostMapping("/{taskId}/reject")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Reject assigned task", description = "Reject assigned task (Technician only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> rejectTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskActionRequest request,
            Authentication authentication) {
        
        Long technicianId = extractTechnicianId(authentication);
        log.info("Technician {} rejecting task {}", technicianId, taskId);
        
        TaskResponse response = taskService.rejectTask(taskId, request, authentication.getName(), technicianId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task rejected and returned to previous status for reassignment"));
    }
    
    /**
     * Start working on task (Technician)
     */
    @PostMapping("/{taskId}/start")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Start working on task", description = "Start working on task (Technician only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> startTask(
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskActionRequest request,
            Authentication authentication) {
        
        Long technicianId = extractTechnicianId(authentication);
        log.info("Technician {} starting work on task {}", technicianId, taskId);
        
        TaskActionRequest actionRequest = request != null ? request : new TaskActionRequest();
        TaskResponse response = taskService.startTask(taskId, actionRequest, authentication.getName(), technicianId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task started successfully"));
    }
    
    /**
     * Complete task (Technician)
     */
    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Complete task", description = "Complete task (Technician only)")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> completeTask(
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskActionRequest request,
            Authentication authentication) {
        
        Long technicianId = extractTechnicianId(authentication);
        log.info("Technician {} completing task {}", technicianId, taskId);
        
        TaskActionRequest actionRequest = request != null ? request : new TaskActionRequest();
        TaskResponse response = taskService.completeTask(taskId, actionRequest, authentication.getName(), technicianId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task completed successfully"));
    }
    
    /**
     * Update task status (Technician)
     */
    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Update task status", description = "Update task status by Technician")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        Long technicianId = extractTechnicianId(authentication);
        TaskResponse response = taskService.updateTaskStatusByTechnician(taskId, request, authentication.getName(), technicianId);
        return ResponseEntity.ok(ApiResponse.success(response, "Task status updated successfully"));
    }
    
    /**
     * Get technician work schedule
     */
    @GetMapping("/my-schedule")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Get my work schedule", description = "Get technician work schedule")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<TechnicianWorkScheduleResponse>>> getMyWorkSchedule(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {
        
        Long technicianId = extractTechnicianId(authentication);
        
        LocalDateTime start = startDate != null ? parseDateTime(startDate) : LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime end = endDate != null ? parseDateTime(endDate) : start.plusDays(30);
        
        List<TechnicianWorkScheduleResponse> response = taskService.getTechnicianWorkSchedule(technicianId, start, end);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Work schedule retrieved successfully"));
    }
    
    // ========== Common Endpoints ==========
    
    /**
     * Get task by ID
     */
    @GetMapping("/{taskId:\\d+}")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get task by ID", description = "Get task details by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long taskId,
            Authentication authentication) {
        
        log.debug("Fetching task with ID: {} for user: {}", taskId, authentication.getName());
        
        // Check if technician is trying to access their own task
        String userRole = extractUserRole(authentication);
        if ("TECHNICIAN".equals(userRole)) {
            Long technicianId = extractTechnicianId(authentication);
            TaskResponse task = taskService.getTaskById(taskId);
            if (!technicianId.equals(task.getAssignedTechnicianId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied to this task", 403));
            }
        }
        
        TaskResponse response = taskService.getTaskById(taskId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task retrieved successfully"));
    }

    /**
     * Get spare parts used in a task (Technician and above)
     */
    @GetMapping("/{taskId}/spare-parts")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get task spare parts usage", description = "List spare parts used for a task")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<TaskSparePartUsageResponse>>> getTaskSpareParts(
            @PathVariable Long taskId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        // Restrict technicians to their own tasks
        String userRole = extractUserRole(authentication);
        if ("TECHNICIAN".equals(userRole)) {
            Long technicianId = extractTechnicianId(authentication);
            TaskResponse task = taskService.getTaskById(taskId);
            if (!technicianId.equals(task.getAssignedTechnicianId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied to this task", 403));
            }
        }
        String bearer = httpRequest.getHeader("Authorization");
        List<TaskSparePartUsageResponse> usages = taskSparePartService.getTaskSpareParts(taskId, bearer);
        return ResponseEntity.ok(ApiResponse.success(usages, "Task spare parts retrieved successfully"));
    }

    /**
     * Export a spare part for a task (Technician)
     */
    @PostMapping("/{taskId}/spare-parts/export")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Export spare part for task", description = "Deduct inventory and record usage")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskSparePartUsageResponse>> exportSparePart(
            @PathVariable Long taskId,
            @Valid @RequestBody ExportTaskSparePartRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        Long technicianId = extractTechnicianId(authentication);
        TaskResponse task = taskService.getTaskById(taskId);
        if (!technicianId.equals(task.getAssignedTechnicianId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied to this task", 403));
        }
        // Ensure path variable and body taskId align
        request.setTaskId(taskId);

        String bearer = httpRequest.getHeader("Authorization");
        TaskSparePartUsageResponse usage = taskSparePartService.exportSparePart(request, authentication.getName(), bearer);
        return ResponseEntity.ok(ApiResponse.success(usage, "Spare part exported successfully"));
    }

    /**
     * Alias for technician work schedule to match existing frontend route
     */
    @GetMapping("/technician-schedule")
    @PreAuthorize("hasAuthority('TECHNICIAN')")
    @Operation(summary = "Get my work schedule (alias)", description = "Alias for technician work schedule to keep backward compatibility")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<TechnicianWorkScheduleResponse>>> getMyWorkScheduleAlias(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Authentication authentication) {
        return getMyWorkSchedule(startDate, endDate, authentication);
    }
    
    /**
     * Get task by task ID
     */
    @GetMapping("/by-task-id/{taskId}")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'TECHNICIAN', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get task by task ID", description = "Get task details by task ID")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskByTaskId(
            @PathVariable String taskId,
            Authentication authentication) {
        
        log.debug("Fetching task with task ID: {} for user: {}", taskId, authentication.getName());
        
        TaskResponse response = taskService.getTaskByTaskId(taskId);
        
        // Check if technician is trying to access their own task
        String userRole = extractUserRole(authentication);
        if ("TECHNICIAN".equals(userRole)) {
            Long technicianId = extractTechnicianId(authentication);
            if (!technicianId.equals(response.getAssignedTechnicianId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied to this task", 403));
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task retrieved successfully"));
    }
    
    /**
     * Get task statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyAuthority('SUPPORT_TEAM', 'LEAD_TECH', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get task statistics", description = "Get task statistics for dashboard")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<TaskStatisticsResponse>> getTaskStatistics() {
        
        TaskStatisticsResponse response = taskService.getTaskStatistics();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Task statistics retrieved successfully"));
    }
    
    // ========== Helper Methods ==========
    
    private Long extractTechnicianId(Authentication authentication) {
        // Extract technician ID from JWT token
        if (authentication.getCredentials() instanceof String token) {
            return jwtUtil.extractUserId(token);
        }
        // If not available in credentials, try to get from principal
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Could not extract technician ID from authentication");
        }
    }
    
    private String extractUserRole(Authentication authentication) {
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            return authentication.getAuthorities().iterator().next().getAuthority();
        }
        return null;
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }
    
    /**
     * Map frontend sort field names to backend entity field names
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null) {
            return "assignedAt";
        }
        
        // Map frontend field names to backend entity field names
        return switch (sortBy) {
            case "preferredCompletionDate" -> "scheduledDate";
            default -> sortBy;
        };
    }
}
