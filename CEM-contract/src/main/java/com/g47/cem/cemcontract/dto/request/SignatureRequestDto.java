package com.g47.cem.cemcontract.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureRequestDto {

    @NotBlank(message = "Signature cannot be blank")
    private String signature;

    private String signerType; // Optional now
} 