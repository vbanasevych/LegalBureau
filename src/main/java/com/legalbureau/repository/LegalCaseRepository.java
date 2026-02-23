package com.legalbureau.repository;


import com.legalbureau.entity.LegalCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {
}
