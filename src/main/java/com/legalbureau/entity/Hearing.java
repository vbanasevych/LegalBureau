package com.legalbureau.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.legalbureau.entity.enums.HearingResult;
import java.time.LocalDateTime;

@Entity
@Table(name = "hearings")
@Getter
@Setter
public class Hearing extends BaseEntity {

    @Column(name = "hearing_date", nullable = false)
    private LocalDateTime hearingDate;

    private String place;

    @Enumerated(EnumType.STRING)
    private HearingResult result;

    @ManyToOne
    @JoinColumn(name = "legal_case_id", nullable = false)
    private LegalCase legalCase;
}
