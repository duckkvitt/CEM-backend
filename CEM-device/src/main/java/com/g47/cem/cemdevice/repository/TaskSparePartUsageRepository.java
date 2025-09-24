package com.g47.cem.cemdevice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.TaskSparePartUsage;

@Repository
public interface TaskSparePartUsageRepository extends JpaRepository<TaskSparePartUsage, Long> {
    List<TaskSparePartUsage> findByTaskIdOrderByUsedAtDesc(Long taskId);
}


