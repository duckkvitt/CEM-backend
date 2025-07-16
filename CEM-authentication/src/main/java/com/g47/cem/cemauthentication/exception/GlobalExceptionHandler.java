package com.g47.cem.cemauthentication.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.g47.cem.cemauthentication.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for all REST controllers
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation failed for request: {} - Errors: {}", request.getRequestURI(), errors);
        
        ApiResponse<Object> response = ApiResponse.error(
                "Validation failed", 
                errors, 
                HttpStatus.BAD_REQUEST.value()
        );
        response.setPath(request.getRequestURI());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        String message = "Authentication failed";
        
        if (ex instanceof BadCredentialsException) {
            message = "Invalid email or password";
        } else if (ex instanceof DisabledException) {
            message = "Account is disabled";
        } else if (ex instanceof LockedException) {
            message = "Account is locked";
        }
        
        log.warn("Authentication failed for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                message, 
                HttpStatus.UNAUTHORIZED.value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Access denied for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                "Access denied", 
                HttpStatus.FORBIDDEN.value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        log.warn("Business exception for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(), 
                ex.getStatus().value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
    
    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        log.warn("Resource not found for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        if (ex.getMessage() != null && ex.getMessage().contains("Role not found")) {
            log.warn("Role not found: {}", ex.getMessage());
        }
        
        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(), 
                HttpStatus.NOT_FOUND.value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle data integrity violations
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        String message = "Data integrity violation";
        if (ex.getMessage() != null && ex.getMessage().contains("unique")) {
            message = "Resource already exists";
        }
        
        log.warn("Data integrity violation for request: {} - {}", request.getRequestURI(), ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                message, 
                HttpStatus.CONFLICT.value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String message = String.format("Invalid value '%s' for parameter '%s'", 
                ex.getValue(), ex.getName());
        
        log.warn("Method argument type mismatch for request: {} - {}", request.getRequestURI(), message);
        
        ApiResponse<Object> response = ApiResponse.error(
                message, 
                HttpStatus.BAD_REQUEST.value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error for request: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred", 
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 