package com.g47.cem.cemcontract.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String companyName;
    private String contactName;
    private String address;
    private String phone;
    private String email;
    private String businessCode;
    private String taxCode;
}
