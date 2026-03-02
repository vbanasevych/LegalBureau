package com.legalbureau.controller;

import com.legalbureau.entity.User;
import com.legalbureau.service.UserService;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.Lawyer;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.service.CaseCategoryService;
import com.legalbureau.service.LawyerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/lawyers")
@RequiredArgsConstructor
public class AdminLawyerController {

    private final LawyerService lawyerService;
    private final CaseCategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String listLawyers(@RequestParam(required = false) String name,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        model.addAttribute("lawyerPage", lawyerService.getFilteredLawyers(name, categoryId, page, 10));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("searchName", name);
        model.addAttribute("searchCategoryId", categoryId);
        return "admin/lawyers/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("lawyer", new Lawyer());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/lawyers/create";
    }

    @PostMapping("/create")
    public String createLawyer(@ModelAttribute Lawyer lawyer,
                               @RequestParam(required = false) List<Long> categoryIds,
                               Model model) {
        try {
            lawyerService.createLawyer(lawyer, categoryIds);
            return "redirect:/admin/lawyers";
        } catch (DuplicateResourceException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "admin/lawyers/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("lawyer", lawyerService.findAll().stream().filter(l -> l.getId().equals(id)).findFirst().orElseThrow());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/lawyers/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateLawyer(@PathVariable Long id,
                               @ModelAttribute Lawyer lawyer,
                               @RequestParam(required = false) List<Long> categoryIds) {
        lawyerService.updateLawyer(id, lawyer, categoryIds);
        return "redirect:/admin/lawyers";
    }

    @GetMapping("/delete/{id}")
    public String deleteLawyer(@PathVariable Long id) {
        lawyerService.deleteLawyer(id);
        return "redirect:/admin/lawyers";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleLawyerStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User lawyerUser = userService.toggleUserStatus(id);

        String action = lawyerUser.isActive() ? "розблоковано" : "заблоковано";
        redirectAttributes.addFlashAttribute("success", "Адвоката " + lawyerUser.getFullName() + " успішно " + action + ".");
        return "redirect:/admin/lawyers";
    }
}
