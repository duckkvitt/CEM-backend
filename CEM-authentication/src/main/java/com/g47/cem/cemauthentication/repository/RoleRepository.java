package com.g47.cem.cemauthentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemauthentication.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) = LOWER(:name)")
    Optional<Role> findByName(@Param("name") String name);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE LOWER(r.name) = LOWER(:name)")
    boolean existsByName(@Param("name") String name);
} 