package com.g47.cem.cemdevice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemdevice.entity.TechnicianProfile;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Long> {
    
    /**
     * Find technician profile by user ID
     */
    Optional<TechnicianProfile> findByUserId(Long userId);
    
    /**
     * Find all active technician profiles
     */
    List<TechnicianProfile> findByIsActiveTrue();
    
    /**
     * Find technician profiles by location
     */
    List<TechnicianProfile> findByLocationAndIsActiveTrue(String location);
    
    /**
     * Find technician profiles by skills containing keyword
     */
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isActive = true AND " +
           "(LOWER(tp.skills) LIKE LOWER(CONCAT('%', :skill, '%')) OR " +
           "LOWER(tp.specializations) LIKE LOWER(CONCAT('%', :skill, '%')))")
    List<TechnicianProfile> findBySkillsContainingAndIsActiveTrue(@Param("skill") String skill);
    
    /**
     * Find technician profiles with available capacity
     */
    @Query("SELECT tp FROM TechnicianProfile tp WHERE tp.isActive = true AND " +
           "tp.maxConcurrentTasks > (SELECT COUNT(t) FROM Task t WHERE t.assignedTechnicianId = tp.userId AND " +
           "t.status IN ('ASSIGNED', 'IN_PROGRESS'))")
    List<TechnicianProfile> findAvailableTechnicians();
    
    /**
     * Check if user exists as technician
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Find technician profiles by multiple user IDs
     */
    List<TechnicianProfile> findByUserIdInAndIsActiveTrue(List<Long> userIds);
}
