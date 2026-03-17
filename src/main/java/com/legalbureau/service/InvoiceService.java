package com.legalbureau.service;

import com.legalbureau.entity.CaseService;
import com.legalbureau.entity.Invoice;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.repository.CaseServiceRepository;
import com.legalbureau.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CaseServiceRepository caseServiceRepository;

    @Transactional
    public void generateInvoiceForCase(LegalCase legalCase) {
        if (invoiceRepository.existsByLegalCaseId(legalCase.getId())) {
            return;
        }

        List<CaseService> services = caseServiceRepository.findAllByLegalCaseId(legalCase.getId());

        BigDecimal totalAmount = services.stream()
                .map(CaseService::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Invoice invoice = new Invoice();
        invoice.setLegalCase(legalCase);
        invoice.setTotalAmount(totalAmount);
        invoice.setIsPaid(false);

        invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceByCaseId(Long caseId) {
        return invoiceRepository.findByLegalCaseId(caseId).orElse(null);
    }

    @Transactional
    public void togglePaymentStatus(Long caseId) {
        Invoice invoice = invoiceRepository.findByLegalCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Рахунок для цієї справи не знайдено"));

        invoice.setIsPaid(!invoice.getIsPaid());
        invoiceRepository.save(invoice);
    }
}