package com.g47.cem.cemauthentication.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all REST endpoints
 * @param <T> Type of data being returned
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private boolean success = true;
    
    private String message;
    
    private T data;
    
    private Object errors;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String path;
    
    private Integer status;
    
    // Success response with data
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .status(200)
                .build();
    }
    
    // Success response with data and message
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .status(200)
                .build();
    }
    
    // Success response with only message
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .status(200)
                .build();
    }
    
    // Error response
    public static <T> ApiResponse<T> error(String message, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .build();
    }
    
    // Error response with errors object
    public static <T> ApiResponse<T> error(String message, Object errors, int status) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .status(status)
                .build();
    }
    
    // Error response with path
    public static <T> ApiResponse<T> error(String message, int status, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .path(path)
                .build();
    }
} 