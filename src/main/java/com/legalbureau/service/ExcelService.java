package com.legalbureau.service;

import com.legalbureau.entity.*;
import com.legalbureau.entity.enums.CaseStatus;
import com.legalbureau.exception.InvalidImportException;
import com.legalbureau.exception.ResourceNotFoundException;
import com.legalbureau.repository.CaseCategoryRepository;
import com.legalbureau.repository.LawyerRepository;
import com.legalbureau.repository.LegalCaseRepository;
import com.legalbureau.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final LegalCaseRepository caseRepository;
    private final UserRepository userRepository;
    private final LawyerRepository lawyerRepository;
    private final CaseCategoryRepository categoryRepository;

    public byte[] exportCasesToExcel(List<LegalCase> cases) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Мої справи");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Номер справи");
            headerRow.createCell(1).setCellValue("Клієнт");
            headerRow.createCell(2).setCellValue("Категорія");
            headerRow.createCell(3).setCellValue("Статус");
            headerRow.createCell(4).setCellValue("Опис");

            int rowIdx = 1;
            for (LegalCase c : cases) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getCaseNumber());
                row.createCell(1).setCellValue(c.getClient() != null ? c.getClient().getFullName() : "Видалений");
                row.createCell(2).setCellValue(c.getCategory() != null ? c.getCategory().getName() : "-");
                row.createCell(3).setCellValue(c.getStatus().getDisplayValue());
                row.createCell(4).setCellValue(c.getDescription() != null ? c.getDescription() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(noRollbackFor = InvalidImportException.class)
    public void importCasesFromExcel(MultipartFile file, String clientEmail, Long lawyerId) throws Exception {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Клієнта з email '" + clientEmail + "' не знайдено в базі. Спочатку створіть його."));

        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Адвоката не знайдено"));

        java.util.List<String> unsavedCases = new java.util.ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell caseNumCell = row.getCell(0);
                String caseNumber = "";

                if (caseNumCell != null) {
                    if (caseNumCell.getCellType() == CellType.STRING) {
                        caseNumber = caseNumCell.getStringCellValue().trim();
                    } else if (caseNumCell.getCellType() == CellType.NUMERIC) {
                        caseNumber = String.valueOf((long) caseNumCell.getNumericCellValue());
                    }
                }

                if (caseNumber.isEmpty()) {
                    caseNumber = "CASE-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                }

                Cell categoryCell = row.getCell(1);
                String categoryName = (categoryCell != null && categoryCell.getCellType() == CellType.STRING)
                        ? categoryCell.getStringCellValue().trim() : "Загальна";

                Cell descCell = row.getCell(2);
                String description = (descCell != null && descCell.getCellType() == CellType.STRING)
                        ? descCell.getStringCellValue().trim() : "Імпортовано з Excel";

                if (caseRepository.existsByCaseNumber(caseNumber)) {
                    unsavedCases.add(caseNumber + " (справа з таким номером вже існує)");
                    continue;
                }

                Optional<CaseCategory> existingCat = categoryRepository.findByName(categoryName);
                if (existingCat.isEmpty()) {
                    unsavedCases.add(caseNumber + " (вказано неіснуючу категорію: '" + categoryName + "')");
                    continue;
                }

                CaseCategory category = existingCat.get();

                LegalCase newCase = new LegalCase();
                newCase.setCaseNumber(caseNumber);
                newCase.setCategory(category);
                newCase.setDescription(description);
                newCase.setClient(client);
                newCase.setLawyer(lawyer);
                newCase.setStatus(CaseStatus.NEW);
                newCase.setCreatedAt(LocalDateTime.now());

                caseRepository.save(newCase);
            }
        }

        if (!unsavedCases.isEmpty()) {
            String errorMessage = "Частковий імпорт. Наступні справи не збережено, бо не існує відповідних категорій: "
                    + String.join(", ", unsavedCases);
            throw new InvalidImportException(errorMessage);
        }
    }

    public byte[] exportSingleCaseToExcel(LegalCase legalCase, List<CaseService> services,
                                          List<Hearing> hearings,
                                          Invoice invoice) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(legalCase.getCaseNumber());
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 10000);

            int rowIdx = 0;
            sheet.createRow(rowIdx++).createCell(0).setCellValue("ДОСЬЄ СПРАВИ: " + legalCase.getCaseNumber());
            sheet.createRow(rowIdx++).createCell(0).setCellValue("Створено: " + legalCase.getCreatedAt().toString().substring(0, 10));
            rowIdx++;

            sheet.createRow(rowIdx).createCell(0).setCellValue("Клієнт:");
            sheet.getRow(rowIdx++).createCell(1).setCellValue(legalCase.getClient() != null ? legalCase.getClient().getFullName() : "Немає");

            sheet.createRow(rowIdx).createCell(0).setCellValue("Адвокат:");
            sheet.getRow(rowIdx++).createCell(1).setCellValue(legalCase.getLawyer() != null ? legalCase.getLawyer().getFullName() : "Не призначено");

            sheet.createRow(rowIdx).createCell(0).setCellValue("Категорія:");
            sheet.getRow(rowIdx++).createCell(1).setCellValue(legalCase.getCategory().getName());

            sheet.createRow(rowIdx).createCell(0).setCellValue("Статус:");
            sheet.getRow(rowIdx++).createCell(1).setCellValue(legalCase.getStatus().getDisplayValue());

            if (legalCase.getResult() != null) {
                sheet.createRow(rowIdx).createCell(0).setCellValue("Результат:");
                sheet.getRow(rowIdx++).createCell(1).setCellValue(legalCase.getResult().getDisplayValue());
            }

            sheet.createRow(rowIdx).createCell(0).setCellValue("Опис проблеми:");
            sheet.getRow(rowIdx++).createCell(1).setCellValue(legalCase.getDescription());
            rowIdx++;

            sheet.createRow(rowIdx++).createCell(0).setCellValue("НАДАНІ ПОСЛУГИ:");
            if (services.isEmpty()) {
                sheet.createRow(rowIdx++).createCell(0).setCellValue("Послуг не додано.");
            } else {
                Row sHeader = sheet.createRow(rowIdx++);
                sHeader.createCell(0).setCellValue("Назва");
                sHeader.createCell(1).setCellValue("Ціна (грн)");
                for (com.legalbureau.entity.CaseService s : services) {
                    Row sRow = sheet.createRow(rowIdx++);
                    sRow.createCell(0).setCellValue(s.getName());
                    sRow.createCell(1).setCellValue(s.getPrice().doubleValue());
                }
            }

            if (invoice != null) {
                sheet.createRow(rowIdx).createCell(0).setCellValue("ФІНАЛЬНА СУМА:");
                sheet.getRow(rowIdx++).createCell(1).setCellValue(invoice.getTotalAmount().doubleValue() + " грн (" + (invoice.getIsPaid() ? "ОПЛАЧЕНО" : "НЕ ОПЛАЧЕНО") + ")");
            }
            rowIdx++;

            sheet.createRow(rowIdx++).createCell(0).setCellValue("РОЗКЛАД ПОДІЙ:");
            if (hearings.isEmpty()) {
                sheet.createRow(rowIdx++).createCell(0).setCellValue("Подій не заплановано.");
            } else {
                Row hHeader = sheet.createRow(rowIdx++);
                hHeader.createCell(0).setCellValue("Дата і Час");
                hHeader.createCell(1).setCellValue("Тип та Місце");
                for (com.legalbureau.entity.Hearing h : hearings) {
                    Row hRow = sheet.createRow(rowIdx++);
                    hRow.createCell(0).setCellValue(h.getHearingDate().toString().replace("T", " "));
                    hRow.createCell(1).setCellValue(h.getType().getDisplayValue() + " - " + h.getPlace());
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}