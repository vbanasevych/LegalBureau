package com.legalbureau.service;

import com.legalbureau.entity.Hearing;
import com.legalbureau.entity.LegalCase;
import com.legalbureau.entity.enums.HearingType;
import com.legalbureau.entity.enums.Role;
import com.legalbureau.repository.HearingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HearingService {

    private final HearingRepository hearingRepository;
    private final LegalCaseService caseService;

    public List<Hearing> getHearingsByCase(Long caseId) {
        return hearingRepository.findAllByLegalCaseIdOrderByHearingDateAsc(caseId);
    }

    public void addHearing(Long caseId, Hearing newHearing, Long lawyerId) {
        if (newHearing.getHearingDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Помилка: Неможливо запланувати подію на минулий час!");
        }
        LegalCase legalCase = caseService.getCaseDetailsWithPrivacy(caseId, lawyerId, Role.LAWYER);

        LocalDateTime newStart = newHearing.getHearingDate();
        LocalDateTime newEnd;

        if (newHearing.getType() == HearingType.COURT) {
            newStart = newHearing.getHearingDate().toLocalDate().atStartOfDay();
            newEnd = newHearing.getHearingDate().toLocalDate().atTime(23, 59, 59);
        } else {
            newEnd = newStart.plusHours(1);
        }

        LocalDateTime startOfDay = newHearing.getHearingDate().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = newHearing.getHearingDate().toLocalDate().atTime(23, 59, 59);

        List<Hearing> dailyHearings = hearingRepository.findHearingsByLawyerAndDate(lawyerId, startOfDay, endOfDay);

        for (Hearing existing : dailyHearings) {
            LocalDateTime exStart = existing.getHearingDate();
            LocalDateTime exEnd;

            if (existing.getType() == HearingType.COURT) {
                exStart = existing.getHearingDate().toLocalDate().atStartOfDay();
                exEnd = existing.getHearingDate().toLocalDate().atTime(23, 59, 59);
            } else {
                exEnd = exStart.plusHours(1);
            }

            if (newStart.isBefore(exEnd) && exStart.isBefore(newEnd)) {
                String timeStr = existing.getType() == HearingType.COURT ? "на весь день" : "о " + existing.getHearingDate().toLocalTime();

                throw new IllegalArgumentException("Накладка часу! У вас вже є "
                        + existing.getType().getDisplayValue()
                        + " по справі " + existing.getLegalCase().getCaseNumber()
                        + " " + timeStr);
            }
        }

        newHearing.setId(null);
        newHearing.setLegalCase(legalCase);
        hearingRepository.save(newHearing);
    }
}