package com.legalbureau.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.Lawyer;
import com.legalbureau.entity.enums.Role;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.exception.ResourceNotFoundException;
import com.legalbureau.repository.CaseCategoryRepository;
import com.legalbureau.repository.LawyerRepository;
import com.legalbureau.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LawyerService {

    private final LawyerRepository lawyerRepository;
    private final UserRepository userRepository;
    private final LegalCaseService legalCaseService;
    private final CaseCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public Lawyer findById(Long id) {
        return lawyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Адвоката не знайдено"));
    }

    public List<Lawyer> findAll() {
        return lawyerRepository.findAll();
    }

    @Transactional
    public void createLawyer(Lawyer lawyer, List<Long> categoryIds) {
        validatePhoneNumber(lawyer.getPhone());
        if (userRepository.existsByEmail(lawyer.getEmail())) {
            throw new DuplicateResourceException("Email " + lawyer.getEmail() + " вже зайнятий");
        }

        lawyer.setRole(Role.LAWYER);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            lawyer.setSpecializations(new HashSet<>(categoryRepository.findAllById(categoryIds)));
        }

        lawyer.setPasswordHash(passwordEncoder.encode(lawyer.getPasswordHash()));
        lawyerRepository.save(lawyer);
    }

    @Transactional
    public void updateLawyer(Long id, Lawyer updatedData, List<Long> categoryIds) {
        validatePhoneNumber(updatedData.getPhone());
        Lawyer existing = lawyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Адвоката не знайдено"));

        existing.setFullName(updatedData.getFullName());
        existing.setCity(updatedData.getCity());
        existing.setHourlyRate(updatedData.getHourlyRate());

        existing.getSpecializations().clear();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            existing.getSpecializations().addAll(categoryRepository.findAllById(categoryIds));
        }

        lawyerRepository.save(existing);
    }

    @Transactional
    public void deleteLawyer(Long id) {
        if (!lawyerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Адвоката не знайдено");
        }
        legalCaseService.unlinkLawyerFromCases(id);

        lawyerRepository.deleteById(id);
    }

    public Page<Lawyer> getFilteredLawyers(String name, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return lawyerRepository.searchAndFilter(name, categoryId, pageable);
    }

    private void validatePhoneNumber(String phone) {
        if (phone == null || !phone.matches("^\\+380-\\d{3}-\\d{3}-\\d{3}$")) {
            throw new IllegalArgumentException("Невірний формат телефону! Використовуйте: +380-XXX-XXX-XXX");
        }
    }
}