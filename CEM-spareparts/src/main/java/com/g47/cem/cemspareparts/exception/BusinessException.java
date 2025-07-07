package com.g47.cem.cemspareparts.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String message;
    private final transient Object errors;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
        this.errors = null;
    }

    public BusinessException(HttpStatus status, String message, Object errors) {
        super(message);
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
} 