package com.legalbureau.controller;

import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.service.CaseCategoryService;
import com.legalbureau.service.LawyerService;
import com.legalbureau.service.LegalCaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/cases")
@RequiredArgsConstructor
public class AdminLegalCaseController {

    private final LegalCaseService caseService;
    private final LawyerService lawyerService;
    private final CaseCategoryService categoryService;

    @GetMapping
    public String listAllCases(Model model) {
        model.addAttribute("cases", caseService.findAll());
        return "admin/cases/index";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("legalCase", caseService.findById(id));
        model.addAttribute("lawyers", lawyerService.findAll());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", CaseStatus.values());
        return "admin/cases/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCase(@PathVariable Long id,
                             @ModelAttribute LegalCase legalCase,
                             @RequestParam Long categoryId,
                             @RequestParam Long lawyerId) {
        caseService.updateCaseByAdmin(id, legalCase, categoryId, lawyerId);
        return "redirect:/admin/cases";
    }

    @GetMapping("/delete/{id}")
    public String deleteCase(@PathVariable Long id) {
        caseService.deleteCase(id);
        return "redirect:/admin/cases";
    }
}