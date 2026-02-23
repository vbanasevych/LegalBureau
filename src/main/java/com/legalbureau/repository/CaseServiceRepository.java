package com.legalbureau.repository;

import com.legalbureau.entity.CaseService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseServiceRepository extends JpaRepository<CaseService, Long> {
}
