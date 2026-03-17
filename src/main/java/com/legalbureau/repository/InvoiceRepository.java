package com.legalbureau.repository;

import com.legalbureau.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByLegalCaseId(Long caseId);
    Optional<Invoice> findByLegalCaseId(Long caseId);
}