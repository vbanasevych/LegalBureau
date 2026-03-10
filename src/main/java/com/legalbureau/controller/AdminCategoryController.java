package com.legalbureau.controller;

import com.legalbureau.entity.CaseCategory;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.service.CaseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CaseCategoryService categoryService;

    @GetMapping
    public String listCategories(@RequestParam(required = false) String search,
                                 @RequestParam(defaultValue = "0") int page,
                                 Model model) {
        model.addAttribute("categoryPage", categoryService.getFilteredCategories(search, page, 10));
        model.addAttribute("searchQuery", search);
        return "admin/categories/index";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam String name, RedirectAttributes redirectAttributes) {
        try {
            CaseCategory category = new CaseCategory();
            category.setName(name);
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("success", "Категорію успішно додано!");
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, org.springframework.ui.Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "admin/categories/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id, @ModelAttribute CaseCategory category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.update(id, category);
            redirectAttributes.addFlashAttribute("success", "Категорію успішно оновлено!");
            return "redirect:/admin/categories";
        } catch (DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/categories/" + id + "/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Категорію видалено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не вдалося видалити категорію. Можливо, до неї прив'язані справи або адвокати.");
        }
        return "redirect:/admin/categories";
    }
}