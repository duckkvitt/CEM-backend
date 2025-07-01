package com.g47.cem.cemcontract.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.g47.cem.cemcontract.enums.ContractAction;
import com.g47.cem.cemcontract.enums.ContractStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ContractHistory entity for tracking changes and audit trail
 */
@Entity
@Table(name = "contract_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContractHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ContractAction action; // CREATED, UPDATED, SIGNED, HIDDEN, RESTORED
    
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    private ContractStatus oldStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 50)
    private ContractStatus newStatus;
    
    @Column(name = "changed_by", nullable = false, length = 255)
    private String changedBy;
    
    @Column(name = "change_reason", length = 500)
    private String changeReason;
    
    @Column(name = "changed_at", nullable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();
    
    // Helper methods
    
    /**
     * Check if this is a status change
     */
    public boolean isStatusChange() {
        return oldStatus != null && newStatus != null && !oldStatus.equals(newStatus);
    }
    
    /**
     * Check if this represents a signing action
     */
    public boolean isSigningAction() {
        return action == ContractAction.SIGNED || action == ContractAction.DIGITAL_SIGNATURE_ADDED;
    }
    
    /**
     * Get description of the change
     */
    public String getChangeDescription() {
        StringBuilder description = new StringBuilder();
        description.append(action.getVietnameseName());
        
        if (isStatusChange()) {
            description.append(": từ ")
                      .append(oldStatus.getVietnameseName())
                      .append(" thành ")
                      .append(newStatus.getVietnameseName());
        }
        
        if (changeReason != null && !changeReason.trim().isEmpty()) {
            description.append(" - ").append(changeReason);
        }
        
        return description.toString();
    }
} 