package com.legalbureau.repository;

import com.legalbureau.entity.CaseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseCategoryRepository extends JpaRepository<CaseCategory, Long> {
}
