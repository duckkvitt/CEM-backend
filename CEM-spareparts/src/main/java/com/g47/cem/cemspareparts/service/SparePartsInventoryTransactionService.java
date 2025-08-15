package com.g47.cem.cemspareparts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g47.cem.cemspareparts.entity.SparePart;
import com.g47.cem.cemspareparts.entity.SparePartsInventoryTransaction;
import com.g47.cem.cemspareparts.repository.SparePartsInventoryTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SparePartsInventoryTransactionService {

    private final SparePartsInventoryTransactionRepository transactionRepository;

    public SparePartsInventoryTransaction createImportTransaction(
            SparePart sparePart, Integer quantity, Integer beforeQuantity,
            Long importRequestId, String reason, String createdBy) {
        String transactionNumber = generateTransactionNumber();
        SparePartsInventoryTransaction tx = SparePartsInventoryTransaction.createImportTransaction(
                sparePart, quantity, beforeQuantity, importRequestId, reason, createdBy, transactionNumber);
        SparePartsInventoryTransaction saved = transactionRepository.save(tx);
        log.info("Created spare-part import transaction {} for part {} qty {}", transactionNumber, sparePart.getId(), quantity);
        return saved;
    }

    public SparePartsInventoryTransaction createExportTransaction(
            SparePart sparePart, Integer quantity, Integer beforeQuantity,
            Long exportRequestId, String reason, String createdBy) {
        String transactionNumber = generateTransactionNumber();
        SparePartsInventoryTransaction tx = SparePartsInventoryTransaction.createExportTransaction(
                sparePart, quantity, beforeQuantity, exportRequestId, reason, createdBy, transactionNumber);
        SparePartsInventoryTransaction saved = transactionRepository.save(tx);
        log.info("Created spare-part export transaction {} for part {} qty {}", transactionNumber, sparePart.getId(), quantity);
        return saved;
    }

    public SparePartsInventoryTransaction createAdjustmentTransaction(
            SparePart sparePart, Integer newQuantity, Integer beforeQuantity,
            String reason, String createdBy) {
        String transactionNumber = generateTransactionNumber();
        SparePartsInventoryTransaction tx = SparePartsInventoryTransaction.createAdjustmentTransaction(
                sparePart, newQuantity, beforeQuantity, reason, createdBy, transactionNumber);
        SparePartsInventoryTransaction saved = transactionRepository.save(tx);
        log.info("Created spare-part adjustment transaction {} for part {} from {} to {}", transactionNumber, sparePart.getId(), beforeQuantity, newQuantity);
        return saved;
    }

    private String generateTransactionNumber() {
        return "SPT-" + System.currentTimeMillis();
    }
}


