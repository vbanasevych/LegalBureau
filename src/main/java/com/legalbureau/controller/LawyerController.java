package com.legalbureau.controller;

import com.legalbureau.entity.CaseService;
import com.legalbureau.entity.Hearing;
import com.legalbureau.entity.User;
import com.legalbureau.entity.enums.CaseResult;
import com.legalbureau.entity.enums.Role;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.security.CustomUserDetails;
import com.legalbureau.service.*;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lawyer")
@RequiredArgsConstructor
public class LawyerController {

    private final LegalCaseService caseService;
    private final CaseServiceManager caseServiceManager;
    private final CaseCategoryService categoryService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final HearingService hearingService;

    @GetMapping("/my-cases")
    public String myCases(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long lawyerId = userDetails.getUser().getId();
        int pageSize = 10;

        Page<LegalCase> casePage = caseService.getFilteredCasesForLawyer(lawyerId, caseNumber, categoryId, status, page, pageSize);

        model.addAttribute("casePage", casePage);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", CaseStatus.values());

        model.addAttribute("searchCaseNumber", caseNumber);
        model.addAttribute("searchCategoryId", categoryId);
        model.addAttribute("searchStatus", status);

        return "lawyer/my-cases";
    }

    @GetMapping("/cases/{id}")
    public String caseDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long lawyerId = userDetails.getUser().getId();
        Role role = userDetails.getUser().getRole();

        LegalCase legalCase = caseService.getCaseDetailsWithPrivacy(id, lawyerId, role);

        model.addAttribute("legalCase", legalCase);
        model.addAttribute("statuses", CaseStatus.values());

        model.addAttribute("services", caseServiceManager.getItemsByCaseId(id));
        model.addAttribute("newServiceItem", new CaseService());

        model.addAttribute("hearings", hearingService.getHearingsByCase(id));

        model.addAttribute("newHearing", new Hearing());
        return "lawyer/case-details";
    }

    @PostMapping("/cases/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam CaseStatus status) {
        caseService.updateStatus(id, status);
        return "redirect:/lawyer/cases/" + id;
    }

    @PostMapping("/cases/{id}/result")
    public String updateResult(@PathVariable Long id,
                               @RequestParam(required = false) CaseResult result,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        Long lawyerId = userDetails.getUser().getId();
        caseService.updateCaseResult(id, result, lawyerId);

        redirectAttributes.addFlashAttribute("success", "Результат справи успішно збережено!");
        return "redirect:/lawyer/cases/" + id;
    }

    @PostMapping("/cases/{id}/services")
    public String addServiceItem(@PathVariable Long id,
                                 @ModelAttribute("newServiceItem") CaseService newServiceItem,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long lawyerId = userDetails.getUser().getId();
        caseServiceManager.addServiceToCase(id, newServiceItem, lawyerId);
        return "redirect:/lawyer/cases/" + id;
    }

    @GetMapping("/cases/{caseId}/services/delete/{itemId}")
    public String deleteServiceItem(@PathVariable Long caseId, @PathVariable Long itemId) {
        caseServiceManager.deleteItem(itemId);
        return "redirect:/lawyer/cases/" + caseId;
    }

    @GetMapping("/clients/create")
    public String showCreateClientForm(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "lawyer/create-client";
    }

    @PostMapping("/clients/create")
    public String createClient(@RequestParam String email,
                               @RequestParam String fullName,
                               @RequestParam String phone,
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            User client = new User();
            client.setEmail(email);
            client.setFullName(fullName);
            client.setPhone(phone);
            client.setPasswordHash("client123");

            userService.createClientByAdmin(client);

            redirectAttributes.addFlashAttribute("success", "Клієнта " + fullName + " успішно створено! Тимчасовий пароль для входу: client123");
            redirectAttributes.addAttribute("clientEmail", email);
            return "redirect:/lawyer/cases/create";

        } catch (DuplicateResourceException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Клієнт з таким email вже існує!");
            return "redirect:/lawyer/clients/create";
        }
    }

    @GetMapping("/cases/create")
    public String showCreateCaseForm(@RequestParam(required = false) String clientEmail, Model model) {
        model.addAttribute("clientEmail", clientEmail);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("legalCase", new LegalCase());
        return "lawyer/create-case";
    }

    @PostMapping("/cases/create")
    public String createCaseByLawyer(@RequestParam String clientEmail,
                                     @ModelAttribute LegalCase legalCase,
                                     @RequestParam Long categoryId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        User client = userService.findByEmail(clientEmail).orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute("error", "Клієнта з поштою " + clientEmail + " не знайдено в базі. Будь ласка, зареєструйте його.");
            redirectAttributes.addAttribute("email", clientEmail);
            return "redirect:/lawyer/clients/create";
        }

        Long lawyerId = userDetails.getUser().getId();

        caseService.createCase(legalCase, client.getId(), lawyerId, categoryId);

        redirectAttributes.addFlashAttribute("success", "Справу успішно відкрито!");
        return "redirect:/lawyer/my-cases";
    }

    @PostMapping("/cases/{id}/hearings")
    public String addHearing(@PathVariable Long id,
                             @ModelAttribute("newHearing") com.legalbureau.entity.Hearing newHearing,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            Long lawyerId = userDetails.getUser().getId();
            hearingService.addHearing(id, newHearing, lawyerId);
            redirectAttributes.addFlashAttribute("success", "Подію успішно додано до розкладу!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/lawyer/cases/" + id;
    }
}