package com.legalbureau.controller;

import com.legalbureau.service.LawyerService;
import com.legalbureau.service.LegalCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final LawyerService lawyerService;
    private final LegalCaseService caseService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("lawyers", lawyerService.findAll());
        model.addAttribute("cases", caseService.findAll());
        return "index";
    }
}
