package com.g47.cem.cemdevice.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

@Entity
@Table(name = "customer_feedbacks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CustomerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_id", nullable = false)
    private Long serviceRequestId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;

    @Column(name = "star_rating", nullable = false)
    private Integer starRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


