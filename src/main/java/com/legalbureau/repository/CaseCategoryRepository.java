package com.legalbureau.repository;

import com.legalbureau.entity.CaseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseCategoryRepository extends JpaRepository<CaseCategory, Long> {
    boolean existsByName(String name);

    @Query("SELECT c FROM CaseCategory c WHERE :search IS NULL OR LOWER(c.name) LIKE :search ORDER BY c.id DESC")
    Page<CaseCategory> findFilteredCategories(@Param("search") String search, Pageable pageable);
}
