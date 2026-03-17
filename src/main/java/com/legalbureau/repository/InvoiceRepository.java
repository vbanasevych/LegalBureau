package com.legalbureau.repository;

import com.legalbureau.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByLegalCaseId(Long caseId);
    Optional<Invoice> findByLegalCaseId(Long caseId);
}