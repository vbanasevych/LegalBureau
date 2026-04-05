package com.legalbureau.controller;

import com.legalbureau.entity.Lawyer;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.entity.enums.Role;
import com.legalbureau.service.*;
import lombok.RequiredArgsConstructor;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.exception.DuplicateResourceException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.legalbureau.security.CustomUserDetails;

import java.util.List;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientController {

    private final LegalCaseService caseService;
    private final LawyerService lawyerService;
    private final CaseCategoryService categoryService;
    private final CaseServiceManager caseServiceManager;
    private final HearingService hearingService;
    private final ExcelService excelService;
    private final InvoiceService invoiceService;
    private final WordService wordService;

    @GetMapping("/my-cases")
    public String myCases(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long clientId = userDetails.getUser().getId();
        int pageSize = 5;

        Page<LegalCase> casePage = caseService.getFilteredCasesForClient(clientId, categoryId, status, page, pageSize);

        model.addAttribute("casePage", casePage);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("statuses", CaseStatus.values());

        model.addAttribute("searchCategoryId", categoryId);
        model.addAttribute("searchStatus", status);

        return "client/my-cases";
    }

    @GetMapping("/cases/{id}")
    public String showCaseDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getUser().getId();
        Role currentUserRole = userDetails.getUser().getRole();

        LegalCase secureCase = caseService.getCaseDetailsWithPrivacy(id, currentUserId, currentUserRole);

        model.addAttribute("legalCase", secureCase);
        model.addAttribute("services", caseServiceManager.getItemsByCaseId(id));
        model.addAttribute("hearings", hearingService.getHearingsByCase(id));

        model.addAttribute("userRole", currentUserRole);

        return "client/case-details";
    }

    @GetMapping("/lawyers")
    public String browseLawyers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageSize = 6;

        Page<Lawyer> lawyerPage = lawyerService.getFilteredLawyers(name, categoryId, page, pageSize);

        model.addAttribute("lawyerPage", lawyerPage);
        model.addAttribute("categories", categoryService.findAll());

        model.addAttribute("searchName", name);
        model.addAttribute("searchCategoryId", categoryId);

        return "client/lawyers";
    }

    @GetMapping("/request")
    public String showRequestForm(@RequestParam(required = false) Long lawyerId, Model model) {
        model.addAttribute("legalCase", new LegalCase());
        model.addAttribute("lawyers", lawyerService.findAll());
        model.addAttribute("selectedLawyerId", lawyerId);

        if (lawyerId != null) {
            Lawyer selectedLawyer = lawyerService.findById(lawyerId);
            if (!selectedLawyer.getSpecializations().isEmpty()) {
                model.addAttribute("categories", selectedLawyer.getSpecializations());
            } else {
                model.addAttribute("categories", categoryService.findAll());
            }
        } else {
            model.addAttribute("categories", categoryService.findAll());
        }

        return "client/request-form";
    }

    @PostMapping("/request")
    public String submitRequest(@ModelAttribute LegalCase legalCase,
                                @RequestParam Long lawyerId,
                                @RequestParam Long categoryId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        try {
            Long clientId = userDetails.getUser().getId();
            caseService.createCase(legalCase, clientId, lawyerId, categoryId);
            return "redirect:/client/my-cases";
        } catch (DuplicateResourceException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("lawyers", lawyerService.findAll());

            Lawyer selectedLawyer = lawyerService.findById(lawyerId);
            if (!selectedLawyer.getSpecializations().isEmpty()) {
                model.addAttribute("categories", selectedLawyer.getSpecializations());
            } else {
                model.addAttribute("categories", categoryService.findAll());
            }

            model.addAttribute("selectedLawyerId", lawyerId);
            return "client/request-form";
        }
    }

    @PostMapping("/cases/{id}/cancel")
    public String cancelCase(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long clientId = userDetails.getUser().getId();
        caseService.cancelCaseByClient(id, clientId);
        return "redirect:/client/my-cases";
    }

    @GetMapping("/cases/export")
    public ResponseEntity<byte[]> exportMyCases(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long clientId = userDetails.getUser().getId();

            List<LegalCase> cases = caseService.getFilteredCasesForClient(clientId, null, null, 0, 1000).getContent();

            byte[] excelData = excelService.exportCasesToExcel(cases);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_cases_client.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cases/{id}/export/excel")
    public ResponseEntity<byte[]> exportSingleCaseClient(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long clientId = userDetails.getUser().getId();
            Role role = userDetails.getUser().getRole();

            LegalCase legalCase = caseService.getCaseDetailsWithPrivacy(id, clientId, role);
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
            Long lawyerId = userDetails.getUser().getId();
            Role role = userDetails.getUser().getRole();

            LegalCase legalCase = caseService.getCaseDetailsWithPrivacy(id, lawyerId, role);

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