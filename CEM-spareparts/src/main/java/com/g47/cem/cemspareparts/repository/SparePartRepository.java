package com.g47.cem.cemspareparts.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.g47.cem.cemspareparts.entity.SparePart;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long>, JpaSpecificationExecutor<SparePart> {
    Optional<SparePart> findByPartCode(String partCode);
    
    /**
     * Search spare parts with keyword using PostgreSQL-compatible native query.
     * This query uses ::text casting to avoid bytea type issues and ensures proper text handling.
     */
    @Query(value = "SELECT * FROM spare_parts sp WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(sp.part_name::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.part_code::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.description::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.compatible_devices::text) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT COUNT(*) FROM spare_parts sp WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(sp.part_name::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.part_code::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.description::text) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sp.compatible_devices::text) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           nativeQuery = true)
    Page<SparePart> findSparePartsWithKeyword(@Param("keyword") String keyword, Pageable pageable);
} 