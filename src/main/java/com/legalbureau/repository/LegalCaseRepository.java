package com.legalbureau.repository;


import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {
    boolean existsByCaseNumber(String caseNumber);

    List<LegalCase> findAllByClientId(Long clientId);

    List<LegalCase> findAllByLawyerId(Long lawyerId);

    @Query("SELECT c FROM LegalCase c WHERE " +
            "(:categoryId IS NULL OR c.category.id = :categoryId) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<LegalCase> findAllFilteredPublic(@Param("categoryId") Long categoryId,
                                          @Param("status") com.legalbureau.entity.enums.CaseStatus status,
                                          Pageable pageable);

    @Query("SELECT c FROM LegalCase c WHERE c.lawyer.id = :lawyerId " +
            "AND (:caseNumber IS NULL OR LOWER(c.caseNumber) LIKE :caseNumber) " +
            "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<LegalCase> findFilteredByLawyerId(@Param("lawyerId") Long lawyerId,
                                           @Param("caseNumber") String caseNumber,
                                           @Param("categoryId") Long categoryId,
                                           @Param("status") com.legalbureau.entity.enums.CaseStatus status,
                                           Pageable pageable);

    @Query("SELECT c FROM LegalCase c WHERE c.client.id = :clientId " +
            "AND (:categoryId IS NULL OR c.category.id = :categoryId) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "ORDER BY c.createdAt DESC")
    Page<LegalCase> findFilteredByClientId(@Param("clientId") Long clientId,
                                           @Param("categoryId") Long categoryId,
                                           @Param("status") CaseStatus status,
                                           Pageable pageable);
}
