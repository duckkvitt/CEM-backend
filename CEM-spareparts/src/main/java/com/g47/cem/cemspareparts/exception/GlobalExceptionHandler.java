package com.g47.cem.cemspareparts.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.g47.cem.cemspareparts.dto.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed for request {}: {}", request.getRequestURI(), errors);
        ApiResponse<Object> response = ApiResponse.error(
                "Validation failed", errors, HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception for request {}: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(), ex.getErrors(), ex.getStatus().value(), request.getRequestURI());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found for request {}: {}", request.getRequestURI(), ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(), HttpStatus.NOT_FOUND.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation for request {}: {}", request.getRequestURI(), ex.getMessage());
        
        String message = "Data integrity constraint violation. Please check your input.";
        
        // Check for specific constraint violations
        if (ex.getMessage() != null) {
            String errorMsg = ex.getMessage().toLowerCase();
            if (errorMsg.contains("duplicate key") || errorMsg.contains("unique constraint")) {
                message = "Resource already exists with the provided information.";
            } else if (errorMsg.contains("foreign key constraint")) {
                message = "Cannot perform operation: referenced resource does not exist.";
            } else if (errorMsg.contains("check constraint")) {
                message = "Invalid data: value does not meet required constraints.";
            } else if (errorMsg.contains("not null constraint")) {
                message = "Required field is missing or empty.";
            }
        }
        
        ApiResponse<Object> response = ApiResponse.error(
                message, HttpStatus.CONFLICT.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Method argument type mismatch for request {}: {}", request.getRequestURI(), ex.getMessage());
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Not available");
        ApiResponse<Object> response = ApiResponse.error(
                message, HttpStatus.BAD_REQUEST.value(), request.getRequestURI());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 