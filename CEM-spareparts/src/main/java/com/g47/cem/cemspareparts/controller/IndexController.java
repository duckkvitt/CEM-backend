package com.g47.cem.cemspareparts.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple controller to handle root path requests and avoid NoResourceFoundException.
 */
@RestController
public class IndexController {

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("CEM Spare Parts Service is running");
    }
} 