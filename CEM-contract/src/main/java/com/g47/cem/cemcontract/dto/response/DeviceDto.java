package com.g47.cem.cemcontract.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DeviceDto {
    private Long id;
    private String name;
    private String model;
    private String serialNumber;
    private BigDecimal price;
    private String unit;
    private String status;
} 