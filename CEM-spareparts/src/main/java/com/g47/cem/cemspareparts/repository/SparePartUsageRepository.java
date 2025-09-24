package com.g47.cem.cemspareparts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemspareparts.entity.SparePartUsage;

@Repository
public interface SparePartUsageRepository extends JpaRepository<SparePartUsage, Long> {
    List<SparePartUsage> findByTaskIdOrderByUsedAtDesc(Long taskId);
}


