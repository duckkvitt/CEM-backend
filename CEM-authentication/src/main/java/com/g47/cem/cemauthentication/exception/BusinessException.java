package com.g47.cem.cemauthentication.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Business logic exception
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final HttpStatus status;
    
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
} 