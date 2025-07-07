package com.g47.cem.cemcontract.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureRequestDto {

    @NotBlank(message = "Signature image cannot be blank")
    private String signatureImage;

    @NotBlank(message = "Signer type cannot be blank")
    private String signerType; // "SELLER" or "CUSTOMER"
} 