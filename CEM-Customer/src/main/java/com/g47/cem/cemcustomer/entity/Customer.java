package com.g47.cem.cemcustomer.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer entity representing customer information
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(unique = true, nullable = false, length = 255)
    private String email;
    
    @Column(length = 20, nullable = false)
    private String phone;
    
    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "company_tax_code", length = 50)
    private String companyTaxCode;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Column(name = "legal_representative", nullable = false, length = 255)
    private String legalRepresentative;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "identity_number", nullable = false, length = 50)
    private String identityNumber;

    @Column(name = "identity_issue_date", nullable = false)
    private LocalDate identityIssueDate;

    @Column(name = "identity_issue_place", nullable = false, length = 255)
    private String identityIssuePlace;

    @Column(length = 20)
    private String fax;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> tags = List.of();
    
    @Column(name = "is_hidden", nullable = false)
    @Builder.Default
    private Boolean isHidden = false;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void hide() {
        this.isHidden = true;
    }
    
    public void show() {
        this.isHidden = false;
    }
    
    public boolean isVisible() {
        return !isHidden;
    }
} 