package com.legalbureau.repository;

import com.legalbureau.entity.CaseService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CaseServiceRepository extends JpaRepository<CaseService, Long> {
    List<CaseService> findAllByLegalCaseId(Long caseId);

    @Query("SELECT COALESCE(SUM(s.price), 0) FROM CaseService s WHERE s.legalCase.id = :caseId")
    BigDecimal sumPriceByLegalCaseId(@Param("caseId") Long caseId);

}
