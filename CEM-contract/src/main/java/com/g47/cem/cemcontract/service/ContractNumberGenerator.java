package com.g47.cem.cemcontract.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.Year;
import jakarta.persistence.EntityManager;

@Service
@RequiredArgsConstructor
public class ContractNumberGenerator {

    private final EntityManager entityManager;

    public String generate() {
        Object result = entityManager.createNativeQuery("SELECT nextval('contract_number_seq')").getSingleResult();
        Long sequenceValue = ((Number) result).longValue();
        int currentYear = Year.now().getValue();
        // Formats the number as HD-YYYY-00000N
        return String.format("HD-%d-%06d", currentYear, sequenceValue);
    }
} 