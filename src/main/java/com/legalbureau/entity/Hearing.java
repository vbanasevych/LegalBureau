package com.legalbureau.entity;

import com.legalbureau.entity.enums.HearingType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "hearings")
@Getter
@Setter
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hearing_date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Future(message = "Консультація/засідання не можуть бути в минулому")
    private LocalDateTime hearingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HearingType type;

    @Column(length = 255)
    private String place;

    @Column(length = 255)
    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_case_id", nullable = false)
    private LegalCase legalCase;
}