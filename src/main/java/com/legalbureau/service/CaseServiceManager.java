package com.legalbureau.service;

import com.legalbureau.entity.CaseService;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.repository.CaseServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseServiceManager {

    private final CaseServiceRepository itemRepository;
    private final LegalCaseService legalCaseService;

    public List<CaseService> getItemsByCaseId(Long caseId) {
        return itemRepository.findAllByLegalCaseId(caseId);
    }

    public void addServiceToCase(Long caseId, CaseService item, Long lawyerId) {
        LegalCase legalCase = legalCaseService.getCaseDetailsWithPrivacy(caseId, lawyerId, com.legalbureau.entity.enums.Role.LAWYER);

        if (legalCase.getStatus() == CaseStatus.NEW || legalCase.getStatus() == CaseStatus.DECLINED) {
            throw new IllegalArgumentException("Неможливо додати послугу до неактивної справи");
        }

        item.setId(null);
        item.setLegalCase(legalCase);
        item.setDateAdded(LocalDate.now());
        itemRepository.save(item);
    }

    public void deleteItem(Long itemId) {
        itemRepository.deleteById(itemId);
    }
}
