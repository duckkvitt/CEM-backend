package com.g47.cem.cemspareparts.entity;

import com.g47.cem.cemspareparts.enums.SparePartStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "spare_parts")
@Data
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
} 