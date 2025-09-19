package com.g47.cem.cemdevice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.g47.cem.cemdevice.entity.Task;
import com.g47.cem.cemdevice.enums.TaskPriority;
import com.g47.cem.cemdevice.enums.TaskStatus;
import com.g47.cem.cemdevice.enums.TaskType;
import com.g47.cem.cemdevice.repository.TaskHistoryRepository;
import com.g47.cem.cemdevice.repository.TaskRepository;
import com.g47.cem.cemdevice.dto.request.TaskActionRequest;

@ExtendWith(MockitoExtension.class)
class TaskServiceRejectionTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskActionRequest rejectRequest;

    @BeforeEach
    void setUp() {
        // Create a test task in ASSIGNED status with previous status PENDING
        testTask = Task.builder()
                .id(1L)
                .taskId("TASK-001")
                .title("Test Task")
                .description("Test Description")
                .type(TaskType.MAINTENANCE)
                .status(TaskStatus.ASSIGNED)
                .priority(TaskPriority.NORMAL)
                .assignedTechnicianId(123L)
                .assignedBy("techlead")
                .assignedAt(LocalDateTime.now())
                .previousStatus(TaskStatus.PENDING) // Set previous status
                .createdBy("support")
                .build();

        rejectRequest = new TaskActionRequest();
        rejectRequest.setRejectionReason("Not suitable for my skills");
    }

    @Test
    void testRejectTask_ShouldReturnToPreviousStatus() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        var result = taskService.rejectTask(1L, rejectRequest, "technician", 123L);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.PENDING, testTask.getStatus()); // Should return to previous status
        assertNull(testTask.getAssignedTechnicianId()); // Assignment should be cleared
        assertNull(testTask.getAssignedBy()); // Assignment info should be cleared
        assertNull(testTask.getAssignedAt()); // Assignment time should be cleared
        assertNull(testTask.getPreviousStatus()); // Previous status should be cleared
        assertEquals("Not suitable for my skills", testTask.getRejectionReason());
        assertEquals("technician", testTask.getRejectedBy());
        assertNotNull(testTask.getRejectedAt());

        // Verify task was saved
        verify(taskRepository, times(1)).save(testTask);
        
        // Verify history was created
        verify(taskHistoryRepository, times(1)).save(any());
    }

    @Test
    void testRejectTask_WithNullPreviousStatus_ShouldDefaultToPending() {
        // Arrange
        testTask.setPreviousStatus(null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        var result = taskService.rejectTask(1L, rejectRequest, "technician", 123L);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.PENDING, testTask.getStatus()); // Should default to PENDING
        assertNull(testTask.getAssignedTechnicianId()); // Assignment should be cleared
    }

    @Test
    void testRejectTask_WithDifferentPreviousStatus_ShouldReturnToThatStatus() {
        // Arrange
        testTask.setPreviousStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // Act
        var result = taskService.rejectTask(1L, rejectRequest, "technician", 123L);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.IN_PROGRESS, testTask.getStatus()); // Should return to IN_PROGRESS
        assertNull(testTask.getAssignedTechnicianId()); // Assignment should be cleared
    }
}
