package com.g47.cem.cemcustomer.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer response DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private List<String> tags;
    private Boolean isHidden;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 