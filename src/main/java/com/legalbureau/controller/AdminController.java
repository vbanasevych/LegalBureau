package com.legalbureau.controller;

import com.legalbureau.entity.User;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.service.LegalCaseService;
import com.legalbureau.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final LegalCaseService legalCaseService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/clients")
    public String listClients(@RequestParam(required = false) String search,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        model.addAttribute("clientPage", userService.getFilteredClients(search, page, 10));
        model.addAttribute("searchQuery", search);
        return "admin/clients/index";
    }

    @GetMapping("/clients/create")
    public String showCreateClientForm(Model model) {
        model.addAttribute("client", new User());
        return "admin/clients/form";
    }

    @PostMapping("/clients/create")
    public String createClient(@ModelAttribute("client") User client, RedirectAttributes redirectAttributes) {
        try {
            userService.createClientByAdmin(client);
            redirectAttributes.addFlashAttribute("success", "Клієнта успішно створено.");
        } catch (DuplicateResourceException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clients/create";
        }
        return "redirect:/admin/clients";
    }

    @GetMapping("/clients/edit/{id}")
    public String showEditClientForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", userService.findById(id));
        return "admin/clients/form";
    }

    @PostMapping("/clients/edit/{id}")
    public String updateClient(@PathVariable Long id, @ModelAttribute("client") User clientData, RedirectAttributes redirectAttributes) {
        try {
            userService.updateClientByAdmin(id, clientData);
            redirectAttributes.addFlashAttribute("success", "Дані клієнта оновлено.");
        } catch (DuplicateResourceException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/clients/edit/" + id;
        }
        return "redirect:/admin/clients";
    }

    @GetMapping("/clients/delete/{id}")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            legalCaseService.unlinkClientFromCases(id);
            userService.deleteUser(id);

            redirectAttributes.addFlashAttribute("success", "Справи клієнта відв'язано. Видалення клієнтів рекомендується робити через блокування.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Не вдалося відв'язати справи.");
        }
        return "redirect:/admin/clients";
    }

    @PostMapping("/clients/toggle-status/{id}")
    public String toggleClientStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.toggleUserStatus(id);
        String action = user.isActive() ? "розблоковано" : "заблоковано";
        redirectAttributes.addFlashAttribute("success", "Клієнта " + user.getFullName() + " успішно " + action + ".");
        return "redirect:/admin/clients";
    }
}