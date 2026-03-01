package com.legalbureau.controller;

import com.legalbureau.entity.Lawyer;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.service.CaseCategoryService;
import com.legalbureau.service.LawyerService;
import com.legalbureau.service.LegalCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicController {

    private final LawyerService lawyerService;
    private final LegalCaseService caseService;
    private final CaseCategoryService categoryService;

    @GetMapping("/lawyers")
    public String publicLawyers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Lawyer> lawyerPage = lawyerService.getFilteredLawyers(name, categoryId, page, 6);
        model.addAttribute("lawyerPage", lawyerPage);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("searchName", name);
        model.addAttribute("searchCategoryId", categoryId);
        return "public/lawyers";
    }

    @GetMapping("/cases")
    public String publicCases(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<LegalCase> casePage = caseService.getPublicFilteredCases(categoryId, status, page, 10);
        model.addAttribute("casePage", casePage);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", CaseStatus.values());
        model.addAttribute("searchCategoryId", categoryId);
        model.addAttribute("searchStatus", status);
        return "public/cases";
    }
}