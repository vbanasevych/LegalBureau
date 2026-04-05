package com.legalbureau.controller;

import com.legalbureau.entity.CaseService;
import com.legalbureau.entity.Hearing;
import com.legalbureau.entity.User;
import com.legalbureau.entity.enums.CaseResult;
import com.legalbureau.entity.enums.Role;
import com.legalbureau.exception.DuplicateResourceException;
import com.legalbureau.exception.InvalidImportException;
import com.legalbureau.exception.ResourceNotFoundException;
import com.legalbureau.security.CustomUserDetails;
import com.legalbureau.service.*;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.CaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/lawyer")
@RequiredArgsConstructor
public class LawyerController {

    private final LegalCaseService caseService;
    private final CaseServiceManager caseServiceManager;
    private final CaseCategoryService categoryService;
    private final UserService userService;
    private final HearingService hearingService;
    private final InvoiceService invoiceService;
    private final ExcelService excelService;
    private final WordService wordService;

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

        model.addAttribute("invoice", invoiceService.getInvoiceByCaseId(id));

        return "lawyer/case-details";
    }

    @PostMapping("/cases/{id}/invoice/toggle-payment")
    public String toggleInvoicePayment(@PathVariable Long id) {
        invoiceService.togglePaymentStatus(id);
        return "redirect:/lawyer/cases/" + id;
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
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            Long lawyerId = userDetails.getUser().getId();
            caseServiceManager.addServiceToCase(id, newServiceItem, lawyerId);
            redirectAttributes.addFlashAttribute("success", "Послугу успішно додано!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

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

    @GetMapping("/cases/export")
    public ResponseEntity<byte[]> exportMyCases(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long lawyerId = userDetails.getUser().getId();
            List<LegalCase> cases = caseService.getFilteredCasesForLawyer(lawyerId, null, null, null, 0, 1000).getContent();

            byte[] excelData = excelService.exportCasesToExcel(cases);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_cases.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cases/import")
    public String importCases(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                              @RequestParam("clientEmail") String clientEmail,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            Long lawyerId = userDetails.getUser().getId();
            String filename = file.getOriginalFilename();

            if (filename != null && filename.endsWith(".docx")) {
                wordService.importCasesFromWord(file, clientEmail, lawyerId);
            } else if (filename != null && filename.endsWith(".xlsx")) {
                excelService.importCasesFromExcel(file, clientEmail, lawyerId);
            } else {
                throw new IllegalArgumentException("Непідтримуваний формат файлу. Використовуйте .xlsx або .docx");
            }

            redirectAttributes.addFlashAttribute("success", "Справи успішно імпортовано!");
            return "redirect:/lawyer/my-cases";

        } catch (ResourceNotFoundException e) {
            if (e.getMessage().contains("не знайдено")) {
                redirectAttributes.addFlashAttribute("error", "Клієнта з поштою " + clientEmail + " не знайдено. Будь ласка, зареєструйте його перед імпортом справ.");
                redirectAttributes.addAttribute("email", clientEmail);
                return "redirect:/lawyer/clients/create";
            }
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lawyer/my-cases";

        } catch (InvalidImportException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lawyer/my-cases";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lawyer/my-cases";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка читання файлу. Переконайтеся, що файл має правильну структуру.");
            return "redirect:/lawyer/my-cases";
        }
    }

    @GetMapping("/cases/{id}/export/excel")
    public ResponseEntity<byte[]> exportSingleCaseLawyer(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long lawyerId = userDetails.getUser().getId();
            Role role = userDetails.getUser().getRole();

            LegalCase legalCase = caseService.getCaseDetailsWithPrivacy(id, lawyerId, role);
            var services = caseServiceManager.getItemsByCaseId(id);
            var hearings = hearingService.getHearingsByCase(id);
            var invoice = invoiceService.getInvoiceByCaseId(id);

            byte[] excelData = excelService.exportSingleCaseToExcel(legalCase, services, hearings, invoice);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Case_" + legalCase.getCaseNumber() + ".xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cases/{id}/export/word")
    public ResponseEntity<byte[]> exportSingleCaseWord(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long clientId = userDetails.getUser().getId();
            Role role = userDetails.getUser().getRole();

            LegalCase legalCase = caseService.getCaseDetailsWithPrivacy(id, clientId, role);

            byte[] wordData = wordService.exportSingleCaseToWord(legalCase);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Case_" + legalCase.getCaseNumber() + ".docx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(wordData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}