package com.legalbureau.repository;

import com.legalbureau.entity.Hearing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HearingRepository extends JpaRepository<Hearing, Long> {

    @Query("SELECT h FROM Hearing h WHERE h.legalCase.lawyer.id = :lawyerId ORDER BY h.hearingDate ASC")
    List<Hearing> findAllByLawyerIdOrderByHearingDateAsc(@Param("lawyerId") Long lawyerId);

    List<Hearing> findAllByLegalCaseIdOrderByHearingDateAsc(Long legalCaseId);

    @Query("SELECT h FROM Hearing h JOIN h.legalCase lc WHERE lc.lawyer.id = :lawyerId " +
            "AND h.hearingDate >= :startOfDay AND h.hearingDate <= :endOfDay")
    List<Hearing> findHearingsByLawyerAndDate(@Param("lawyerId") Long lawyerId,
                                              @Param("startOfDay") java.time.LocalDateTime startOfDay,
                                              @Param("endOfDay") java.time.LocalDateTime endOfDay);
}