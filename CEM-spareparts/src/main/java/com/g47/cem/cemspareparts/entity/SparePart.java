package com.g47.cem.cemspareparts.entity;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.g47.cem.cemspareparts.enums.SparePartStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "spare_parts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "part_code", nullable = false, unique = true)
    private String partCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "compatible_devices")
    private String compatibleDevices;

    @Column(name = "unit_of_measurement", nullable = false)
    private String unitOfMeasurement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SparePartStatus status = SparePartStatus.ACTIVE;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToMany(mappedBy = "spareParts", fetch = FetchType.LAZY)
    private Set<Supplier> suppliers;
    
    /**
     * Custom equals method that only uses ID to avoid circular dependencies with collections.
     * This prevents ConcurrentModificationException during collection operations.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SparePart sparePart = (SparePart) obj;
        return id != null && id.equals(sparePart.id);
    }
    
    /**
     * Custom hashCode method that only uses ID to avoid circular dependencies with collections.
     * This prevents ConcurrentModificationException during collection operations.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    /**
     * Custom toString method that excludes collections to avoid circular dependencies.
     */
    @Override
    public String toString() {
        return "SparePart{" +
                "id=" + id +
                ", partName='" + partName + '\'' +
                ", partCode='" + partCode + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
} 