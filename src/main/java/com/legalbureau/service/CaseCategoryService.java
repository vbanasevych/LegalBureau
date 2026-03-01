package com.legalbureau.service;

import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.CaseCategory;
import com.legalbureau.repository.CaseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CaseCategoryService {

    private final CaseCategoryRepository repository;

    public List<CaseCategory> findAll() {
        return repository.findAll();
    }

    public CaseCategory findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію не знайдено"));
    }

    public void save(CaseCategory category) {
        if (repository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Категорія з назвою '" + category.getName() + "' вже існує");
        }
        repository.save(category);
    }

    public void update(Long id, CaseCategory updatedData) {
        CaseCategory existing = findById(id);

        if (!existing.getName().equals(updatedData.getName()) && repository.existsByName(updatedData.getName())) {
            throw new DuplicateResourceException("Категорія з назвою '" + updatedData.getName() + "' вже існує");
        }

        existing.setName(updatedData.getName());
        repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Категорію не знайдено");
        }
        repository.deleteById(id);
    }
}