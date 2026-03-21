package com.legalbureau.controller;

import com.legalbureau.dto.ChartDataDto;
import com.legalbureau.service.LegalCaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/charts")
@RequiredArgsConstructor
public class ChartsApiController {

    private final LegalCaseService caseService;

    @GetMapping("/categories")
    public List<ChartDataDto> getCategoryStats() {
        return caseService.getCategoryStatistics();
    }

    @GetMapping("/results")
    public List<ChartDataDto> getResultStats() {
        return caseService.getResultStatistics();
    }
}