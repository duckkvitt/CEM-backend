package com.g47.cem.cemdevice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;

/**
 * Repository interface for Task entity
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find task by task ID
     */
    Optional<Task> findByTaskId(String taskId);
    
    /**
     * Find tasks by status with pagination
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    
    /**
     * Find tasks by assigned technician ID with pagination
     */
    Page<Task> findByAssignedTechnicianId(Long technicianId, Pageable pageable);
    
    /**
     * Find tasks by assigned technician ID and status with pagination
     */
    Page<Task> findByAssignedTechnicianIdAndStatus(Long technicianId, TaskStatus status, Pageable pageable);
    
    /**
     * Find tasks by customer ID with pagination
     */
    Page<Task> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Find tasks by customer device ID with pagination
     */
    Page<Task> findByCustomerDeviceId(Long customerDeviceId, Pageable pageable);
    
    /**
     * Find tasks by service request ID
     */
    List<Task> findByServiceRequestId(Long serviceRequestId);
    
    /**
     * Find latest task by service request ID
     */
    Optional<Task> findFirstByServiceRequestIdOrderByCreatedAtDesc(Long serviceRequestId);
    
    /**
     * Find tasks by type with pagination
     */
    Page<Task> findByType(TaskType type, Pageable pageable);
    
    /**
     * Find tasks by priority with pagination
     */
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
    
    /**
     * Find tasks by status and priority with pagination
     */
    Page<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority, Pageable pageable);
    
    /**
     * Find tasks by status and type with pagination
     */
    Page<Task> findByStatusAndType(TaskStatus status, TaskType type, Pageable pageable);
    
    /**
     * Find tasks by priority and type with pagination
     */
    Page<Task> findByPriorityAndType(TaskPriority priority, TaskType type, Pageable pageable);
    
    /**
     * Find tasks by status, priority and type with pagination
     */
    Page<Task> findByStatusAndPriorityAndType(TaskStatus status, TaskPriority priority, TaskType type, Pageable pageable);
    
    /**
     * Find tasks by assigned by (TechLead) with pagination
     */
    Page<Task> findByAssignedBy(String assignedBy, Pageable pageable);
    
    /**
     * Find tasks scheduled for a specific date range
     */
    @Query("SELECT t FROM Task t WHERE t.scheduledDate BETWEEN :startDate AND :endDate")
    List<Task> findByScheduledDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find tasks assigned to a technician scheduled for a specific date range
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTechnicianId = :technicianId AND t.scheduledDate BETWEEN :startDate AND :endDate")
    List<Task> findByAssignedTechnicianIdAndScheduledDateBetween(
        @Param("technicianId") Long technicianId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find pending tasks not assigned yet
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.assignedTechnicianId IS NULL")
    Page<Task> findPendingTasksNotAssigned(Pageable pageable);
    
    /**
     * Find overdue tasks (scheduled date passed but not completed)
     */
    @Query("SELECT t FROM Task t WHERE t.scheduledDate < :currentDate AND t.status NOT IN ('COMPLETED', 'REJECTED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);
    
    /**
     * Count tasks by assigned technician ID and status
     */
    long countByAssignedTechnicianIdAndStatus(Long technicianId, TaskStatus status);
    
    /**
     * Count tasks by priority
     */
    long countByPriority(TaskPriority priority);
    
    /**
     * Count tasks assigned to a technician
     */
    long countByAssignedTechnicianId(Long technicianId);
    
    /**
     * Find tasks with keyword search (title, description, task ID, customer device info)
     */
    @Query("SELECT t FROM Task t WHERE " +
           "(t.title LIKE %:keyword% OR t.description LIKE %:keyword% OR t.taskId LIKE %:keyword% OR " +
           "t.customerDevice.device.name LIKE %:keyword% OR t.customerDevice.device.model LIKE %:keyword%)")
    Page<Task> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find tasks for a specific technician with keyword search
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTechnicianId = :technicianId AND " +
           "(t.title LIKE %:keyword% OR t.description LIKE %:keyword% OR t.taskId LIKE %:keyword% OR " +
           "t.customerDevice.device.name LIKE %:keyword% OR t.customerDevice.device.model LIKE %:keyword%)")
    Page<Task> findByAssignedTechnicianIdAndKeyword(@Param("technicianId") Long technicianId, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find tasks available for assignment (PENDING status, no assigned technician)
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' AND t.assignedTechnicianId IS NULL ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findTasksAvailableForAssignment();
    
    /**
     * Find tasks by multiple statuses
     */
    @Query("SELECT t FROM Task t WHERE t.status IN :statuses")
    Page<Task> findByStatusIn(@Param("statuses") List<TaskStatus> statuses, Pageable pageable);
    
    /**
     * Find tasks assigned to technician by multiple statuses
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTechnicianId = :technicianId AND t.status IN :statuses")
    Page<Task> findByAssignedTechnicianIdAndStatusIn(@Param("technicianId") Long technicianId, @Param("statuses") List<TaskStatus> statuses, Pageable pageable);
    
    /**
     * Get task statistics for dashboard
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN t.status = 'PENDING' THEN 1 END) as pendingCount, " +
           "COUNT(CASE WHEN t.status = 'ASSIGNED' THEN 1 END) as assignedCount, " +
           "COUNT(CASE WHEN t.status = 'ACCEPTED' THEN 1 END) as acceptedCount, " +
           "COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) as inProgressCount, " +
           "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completedCount, " +
           "COUNT(CASE WHEN t.status = 'REJECTED' THEN 1 END) as rejectedCount " +
           "FROM Task t")
    Object[] getTaskStatistics();
}
