package com.g47.cem.cemdevice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

/**
 * Entity representing additional profile information for technicians
 */
@Entity
@Table(name = "technician_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TechnicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills; // Comma-separated skills

    @Column(name = "specializations", columnDefinition = "TEXT")
    private String specializations; // Comma-separated specializations

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications; // Comma-separated certifications

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "max_concurrent_tasks")
    @Builder.Default
    private Integer maxConcurrentTasks = 8;

    @Column(name = "working_hours_start")
    private LocalTime workingHoursStart;

    @Column(name = "working_hours_end")
    private LocalTime workingHoursEnd;

    @Column(name = "working_days", length = 20)
    @Builder.Default
    private String workingDays = "MON,TUE,WED,THU,FRI"; // Comma-separated days

    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 50)
    private String emergencyContactPhone;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods
    public String[] getSkillsArray() {
        return skills != null ? skills.split(",") : new String[0];
    }

    public String[] getSpecializationsArray() {
        return specializations != null ? specializations.split(",") : new String[0];
    }

    public String[] getCertificationsArray() {
        return certifications != null ? certifications.split(",") : new String[0];
    }

    public String[] getWorkingDaysArray() {
        return workingDays != null ? workingDays.split(",") : new String[0];
    }
}
