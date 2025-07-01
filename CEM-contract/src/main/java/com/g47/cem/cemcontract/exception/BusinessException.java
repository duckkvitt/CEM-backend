package com.g47.cem.cemcontract.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Custom business exception for contract operations
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final HttpStatus status;
    private final String errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.errorCode = "BUSINESS_ERROR";
    }
} 