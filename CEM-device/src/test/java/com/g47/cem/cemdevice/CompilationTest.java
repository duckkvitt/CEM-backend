package com.g47.cem.cemdevice;

import com.g47.cem.cemdevice.exception.BusinessException;
import com.g47.cem.cemdevice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompilationTest {
    
    @Test
    public void testBusinessExceptionCompilation() {
        BusinessException exception = new BusinessException("Test message");
        assertNotNull(exception);
        assertEquals("Test message", exception.getMessage());
    }
    
    @Test
    public void testResourceNotFoundExceptionCompilation() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test", "id", "123");
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Test not found with id : '123'"));
    }
} 