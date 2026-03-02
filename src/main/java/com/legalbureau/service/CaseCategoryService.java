package com.legalbureau.service;

import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.CaseCategory;
import com.legalbureau.repository.CaseCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<CaseCategory> getFilteredCategories(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeSearch = (search == null || search.trim().isEmpty()) ? null : "%" + search.trim().toLowerCase() + "%";
        return repository.findFilteredCategories(safeSearch, pageable);
    }

    public void save(CaseCategory category) {
        if (repository.existsByName(category.getName())) {
            throw new DuplicateResourceException("Категорія з назвою '" + category.getName() + "' вже існує");
        }
        repository.save(category);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Категорію не знайдено");
        }
        repository.deleteById(id);
    }
}