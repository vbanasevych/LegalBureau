package com.legalbureau.service;

import com.legalbureau.entity.CaseCategory;
import com.legalbureau.entity.Lawyer;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.User;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.exception.InvalidImportException;
import com.legalbureau.exception.ResourceNotFoundException;
import com.legalbureau.repository.CaseCategoryRepository;
import com.legalbureau.repository.LawyerRepository;
import com.legalbureau.repository.LegalCaseRepository;
import com.legalbureau.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {

    private final LegalCaseRepository caseRepository;
    private final UserRepository userRepository;
    private final LawyerRepository lawyerRepository;
    private final CaseCategoryRepository categoryRepository;

    public byte[] exportSingleCaseToWord(LegalCase legalCase) throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("ОФІЦІЙНЕ ДОСЬЄ СПРАВИ");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();

            XWPFParagraph subtitle = document.createParagraph();
            subtitle.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun subtitleRun = subtitle.createRun();
            subtitleRun.setText("№ " + legalCase.getCaseNumber() + " | Статус: " + legalCase.getStatus().getDisplayValue());
            subtitleRun.setItalic(true);
            subtitleRun.setFontSize(12);
            subtitleRun.addBreak();
            subtitleRun.addBreak();

            addInfoLine(document, "Клієнт:", legalCase.getClient() != null ? legalCase.getClient().getFullName() : "Немає даних");
            addInfoLine(document, "Адвокат:", legalCase.getLawyer() != null ? legalCase.getLawyer().getFullName() : "Не призначено");
            addInfoLine(document, "Категорія:", legalCase.getCategory().getName());
            addInfoLine(document, "Дата відкриття:", legalCase.getCreatedAt().toString().substring(0, 10));

            if (legalCase.getResult() != null) {
                addInfoLine(document, "Результат:", legalCase.getResult().getDisplayValue());
            }

            XWPFParagraph descTitle = document.createParagraph();
            XWPFRun descTitleRun = descTitle.createRun();
            descTitleRun.addBreak();
            descTitleRun.setText("Опис звернення:");
            descTitleRun.setBold(true);
            descTitleRun.setFontSize(12);

            XWPFParagraph descBody = document.createParagraph();
            XWPFRun descBodyRun = descBody.createRun();
            descBodyRun.setText(legalCase.getDescription());
            descBodyRun.setFontSize(12);

            document.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(noRollbackFor = InvalidImportException.class)
    public void importCasesFromWord(MultipartFile file, String clientEmail, Long lawyerId) throws Exception {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Клієнта з email '" + clientEmail + "' не знайдено в базі."));

        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Адвоката не знайдено"));

        List<String> unsavedCases = new ArrayList<>();

        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {

            String caseNumber = "";
            String categoryName = "";
            StringBuilder description = new StringBuilder();

            boolean isParsingDescription = false;
            boolean hasData = false;

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();

                if (text.equals("---") || text.equals("***")) {
                    if (hasData) {
                        saveParsedCase(caseNumber, categoryName, description.toString(), client, lawyer, unsavedCases);
                        caseNumber = ""; categoryName = ""; description = new StringBuilder();
                        isParsingDescription = false; hasData = false;
                    }
                    continue;
                }

                if (text.toLowerCase().startsWith("номер:")) {
                    caseNumber = text.substring(6).trim();
                    hasData = true; isParsingDescription = false;
                } else if (text.toLowerCase().startsWith("категорія:")) {
                    categoryName = text.substring(10).trim();
                    hasData = true; isParsingDescription = false;
                } else if (text.toLowerCase().startsWith("опис:")) {
                    description.append(text.substring(5).trim());
                    hasData = true; isParsingDescription = true;
                } else if (isParsingDescription && !text.isEmpty()) {
                    description.append("\n").append(text);
                }
            }

            if (hasData) {
                saveParsedCase(caseNumber, categoryName, description.toString(), client, lawyer, unsavedCases);
            }
        }

        if (!unsavedCases.isEmpty()) {
            String errorMessage = "Частковий імпорт. Наступні справи не збережено, бо не існує відповідних категорій: "
                    + String.join(", ", unsavedCases);
            throw new InvalidImportException(errorMessage);
        }
    }

    private void saveParsedCase(String caseNumber, String categoryName, String description, User client, Lawyer lawyer, List<String> unsavedCases) {
        if (caseNumber.isEmpty()) {
            caseNumber = "CASE-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
        if (categoryName.isEmpty()) categoryName = "Загальна";
        if (description.isEmpty()) description = "Імпортовано з Word";

        if (caseRepository.existsByCaseNumber(caseNumber)) return;

        Optional<CaseCategory> existingCat = categoryRepository.findByName(categoryName);
        if (existingCat.isEmpty()) {
            unsavedCases.add(caseNumber + " (категорія '" + categoryName + "')");
            return;
        }

        LegalCase newCase = new LegalCase();
        newCase.setCaseNumber(caseNumber);
        newCase.setCategory(existingCat.get());
        newCase.setDescription(description.trim());
        newCase.setClient(client);
        newCase.setLawyer(lawyer);
        newCase.setStatus(CaseStatus.NEW);
        newCase.setCreatedAt(LocalDateTime.now());

        caseRepository.save(newCase);
    }

    private void addInfoLine(XWPFDocument document, String label, String value) {
        XWPFParagraph p = document.createParagraph();
        XWPFRun r1 = p.createRun();
        r1.setText(label + " ");
        r1.setBold(true);

        XWPFRun r2 = p.createRun();
        r2.setText(value);
    }

}