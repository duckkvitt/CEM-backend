package com.g47.cem.cemdevice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.entity.TaskHistory;
import com.g47.cem.cemdevice.enums.TaskStatus;

/**
 * Repository interface for TaskHistory entity
 */
@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    
    /**
     * Find task history by task with pagination
     */
    Page<TaskHistory> findByTask(Task task, Pageable pageable);
    
    /**
     * Find task history by task ID with pagination
     */
    @Query("SELECT th FROM TaskHistory th WHERE th.task.id = :taskId ORDER BY th.createdAt DESC")
    Page<TaskHistory> findByTaskId(@Param("taskId") Long taskId, Pageable pageable);
    
    /**
     * Find task history by task ID ordered by creation date
     */
    @Query("SELECT th FROM TaskHistory th WHERE th.task.id = :taskId ORDER BY th.createdAt DESC")
    List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);
    
    /**
     * Find task history by updated by (user)
     */
    Page<TaskHistory> findByUpdatedBy(String updatedBy, Pageable pageable);
    
    /**
     * Find task history by status
     */
    Page<TaskHistory> findByStatus(TaskStatus status, Pageable pageable);
    
    /**
     * Find task history by user role
     */
    Page<TaskHistory> findByUserRole(String userRole, Pageable pageable);
    
    /**
     * Find latest status update for a task
     */
    @Query("SELECT th FROM TaskHistory th WHERE th.task.id = :taskId ORDER BY th.createdAt DESC LIMIT 1")
    TaskHistory findLatestByTaskId(@Param("taskId") Long taskId);
}
