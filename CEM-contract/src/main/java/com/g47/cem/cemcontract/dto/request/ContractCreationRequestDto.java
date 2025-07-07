package com.g47.cem.cemcontract.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContractCreationRequestDto {

    @NotNull
    private LocalDate documentDate;

    @NotNull @Valid
    private PartyDetails seller;

    @NotNull @Valid
    private PartyDetails buyer;

    @NotEmpty @Valid
    private List<ContractItem> items;

    @NotEmpty @Valid
    private List<DeliveryDetail> deliveryDetails;
    
    private String paymentTerms;

    private String notes;

    @Data
    public static class PartyDetails {
        @NotBlank
        private String companyName;
        private String businessCode;
        @NotBlank
        private String address;
        @NotBlank
        private String legalRepresentative;
        private String position;
        private String idCardNumber;
        private String phone;
        private String fax;
        @Email
        private String email;
        private Long entityId; // To link to an existing Customer or internal user/company
    }

    @Data
    public static class ContractItem {
        @NotBlank
        private String name;
        @NotBlank
        private String unit;
        @NotNull
        private Double quantity;
        @NotNull
        private BigDecimal unitPrice;
        private String notes;
    }

    @Data
    public static class DeliveryDetail {
        @NotBlank
        private String itemName;
        @NotBlank
        private String unit;
        @NotNull
        private Double quantity;
        @NotNull
        private LocalDate deliveryDate;
        @NotBlank
        private String deliveryLocation;
        private String notes;
    }
} 