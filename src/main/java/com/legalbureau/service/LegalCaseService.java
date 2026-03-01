package com.legalbureau.service;


import com.legalbureau.entity.enums.Role;
import com.legalbureau.exception.DuplicateResourceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.CaseCategory;
import com.legalbureau.entity.Lawyer;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.User;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.exception.ResourceNotFoundException;
import com.legalbureau.repository.CaseCategoryRepository;
import com.legalbureau.repository.LawyerRepository;
import com.legalbureau.repository.LegalCaseRepository;
import com.legalbureau.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LegalCaseService {

    private final LegalCaseRepository caseRepository;
    private final UserRepository userRepository;
    private final LawyerRepository lawyerRepository;
    private final CaseCategoryRepository categoryRepository;

    public List<LegalCase> findAll() {
        return caseRepository.findAll();
    }

    public LegalCase findById(Long id) {
        return caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Справу не знайдено"));
    }

    public void createCase(LegalCase legalCase, Long clientId, Long lawyerId, Long categoryId) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Клієнта не знайдено"));
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Адвоката не знайдено"));
        CaseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію не знайдено"));

        if (legalCase.getCaseNumber() == null || legalCase.getCaseNumber().isEmpty()) {
            legalCase.setCaseNumber("CASE-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        } else if (caseRepository.existsByCaseNumber(legalCase.getCaseNumber())) {
            throw new DuplicateResourceException("Справа з таким номером вже існує");
        }

        legalCase.setClient(client);
        legalCase.setLawyer(lawyer);
        legalCase.setCategory(category);
        legalCase.setStatus(CaseStatus.NEW);
        legalCase.setCreatedAt(LocalDateTime.now());

        caseRepository.save(legalCase);
    }

    public void updateStatus(Long caseId, CaseStatus newStatus) {
        LegalCase legalCase = findById(caseId);
        legalCase.setStatus(newStatus);

        if (newStatus == CaseStatus.COMPLETED || newStatus == CaseStatus.DECLINED) {
            legalCase.setFinishedAt(LocalDateTime.now());
        }

        legalCase.setEditedAt(LocalDateTime.now());
        caseRepository.save(legalCase);
    }

    public LegalCase getCaseDetailsWithPrivacy(Long caseId, Long currentUserId, Role currentUserRole) {
        LegalCase legalCase = findById(caseId);

        boolean isOwner = legalCase.getClient().getId().equals(currentUserId);
        boolean isAssignedLawyer = legalCase.getLawyer() != null && legalCase.getLawyer().getId().equals(currentUserId);

        if (!isOwner && !isAssignedLawyer && currentUserRole != Role.ADMIN) {
            legalCase.setDescription("Опис приховано з міркувань конфіденційності.");
        }

        return legalCase;
    }

    public void updateCaseByAdmin(Long id, LegalCase updatedData, Long categoryId, Long lawyerId) {
        LegalCase existing = findById(id);

        CaseCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію не знайдено"));
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Адвоката не знайдено"));

        existing.setCategory(category);
        existing.setLawyer(lawyer);
        existing.setStatus(updatedData.getStatus());
        existing.setDescription(updatedData.getDescription());
        existing.setEditedAt(LocalDateTime.now());

        caseRepository.save(existing);
    }

    @Transactional
    public void deleteCase(Long id) {
        if (!caseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Справу не знайдено");
        }
        caseRepository.deleteById(id);
    }

    public Page<LegalCase> getPublicFilteredCases(Long categoryId, CaseStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return caseRepository.findAllFilteredPublic(categoryId, status, pageable);
    }

    public Page<LegalCase> getFilteredCasesForClient(Long clientId, Long categoryId, CaseStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return caseRepository.findFilteredByClientId(clientId, categoryId, status, pageable);
    }

    public Page<LegalCase> getFilteredCasesForLawyer(Long lawyerId, String caseNumber, Long categoryId, CaseStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        String safeSearchStr = (caseNumber == null || caseNumber.trim().isEmpty())
                ? null
                : "%" + caseNumber.trim().toLowerCase() + "%";

        return caseRepository.findFilteredByLawyerId(lawyerId, safeSearchStr, categoryId, status, pageable);
    }

    @Transactional
    public void cancelCaseByClient(Long caseId, Long clientId) {
        LegalCase legalCase = getCaseDetailsWithPrivacy(caseId, clientId, com.legalbureau.entity.enums.Role.CLIENT);

        if (legalCase.getStatus() != com.legalbureau.entity.enums.CaseStatus.NEW) {
            throw new IllegalStateException("Ви можете скасувати лише нові запити, які ще не прийняті в роботу.");
        }

        caseRepository.delete(legalCase);
    }

    @Transactional
    public void unlinkLawyerFromCases(Long lawyerId) {
        List<LegalCase> cases = caseRepository.findAllByLawyerId(lawyerId);
        for (LegalCase c : cases) {
            c.setLawyer(null);

            if (c.getStatus() == com.legalbureau.entity.enums.CaseStatus.IN_PROGRESS ||
                    c.getStatus() == com.legalbureau.entity.enums.CaseStatus.ACCEPTED) {
                c.setStatus(com.legalbureau.entity.enums.CaseStatus.NEW);
            }
            caseRepository.save(c);
        }
        caseRepository.saveAll(cases);
    }

    @Transactional
    public void unlinkClientFromCases(Long clientId) {
        List<LegalCase> cases = caseRepository.findAllByClientId(clientId);
        for (LegalCase c : cases) {
            c.setClient(null);
        }
        caseRepository.saveAll(cases);
    }
}
